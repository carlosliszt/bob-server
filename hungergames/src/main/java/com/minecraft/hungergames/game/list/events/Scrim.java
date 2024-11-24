package com.minecraft.hungergames.game.list.events;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.listener.SoupListener;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.command.ScrimCommand;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.list.Event;
import com.minecraft.hungergames.game.structure.BonusFeast;
import com.minecraft.hungergames.game.structure.Feast;
import com.minecraft.hungergames.game.structure.FinalBattle;
import com.minecraft.hungergames.game.structure.Minifeast;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.*;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.explication.Explication;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.Loadable;
import com.minecraft.hungergames.util.selector.Items;
import com.minecraft.hungergames.util.stats.StatsApplier;
import com.minecraft.hungergames.util.templatekit.scrim.KitType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Loadable
public class Scrim extends Event {

    private final Explication KITSET = new Explication(5, "Sejam bem-vindos a §eScrim§f!",
            "Todos irão iniciar o jogo com um kit de itens.",
            "Vocês terão §e1 minuto§f de invencibilidade para se organizarem.",
            "É §cproibido§f interferir na luta de jogadores que não fazem parte do seu time.",
            "É §cproibido§f aguardar que uma luta seja finalizada com o intuito de atacar os vencedores.",
            "Não é permitido fazer spawn traps.",
            "Aos §e30 minutos §fde partida, vocês serão teleportados para uma Arena Final.",
            "Alguns kits estarão desativados durante todo o evento.",
            "Dúvidas, reclamações e revisões de kick devem ser solicitados em nosso §9Discord§f!",
            "Iniciaremos em alguns instantes! Bom jogo e boa sorte.");

    private final Explication MINE = new Explication(5, "Sejam bem-vindos a §eScrim§f!",
            "O evento será minerado, ou seja, você deve coletar seus itens para lutar.",
            "Vocês terão §e10 minutos§f de invencibilidade para coletar recursos.",
            "É §cproibido§f aguardar que uma luta seja finalizada com o intuito de atacar os vencedores.",
            "Não é permitido fazer spawn traps.",
            "Aos §e30 minutos §fde partida, vocês serão teleportados para uma Arena Final.",
            "Aos §e40 minutos §fde partida, será aplicado efeito de §7Wither II §fem todos os jogadores.",
            "Alguns kits estarão desativados durante todo o evento.",
            "Dúvidas, reclamações e revisões de kick devem ser solicitados em nosso §9Discord§f!",
            "Iniciaremos em alguns instantes! Bom jogo e boa sorte.");

    public Scrim(HungerGames hungerGames) {
        super(hungerGames);
        setName("Scrim");
        setDisplay("SCRIM");
        setupVariables();
        Constants.setServerType(ServerType.SCRIM);
        getPlugin().getBukkitFrame().registerAdapter(KitType.class, KitType::parse);
        getPlugin().getAccountLoader().addColumns(Columns.SCRIM_KILLS, Columns.SCRIM_DEATHS, Columns.SCRIM_GAMES, Columns.SCRIM_MAX_GAME_KILLS, Columns.SCRIM_RANK, Columns.SCRIM_RANK_EXP);
        getPlugin().getBukkitFrame().registerCommands(new ScrimCommand());
    }

    @Override
    public void load() {
        getPlugin().setRankingFactory(new RankingFactory(RankingTarget.SCRIM));
        super.load();
        setupKits();
    }

    @Variable(name = "scrim.type")
    public KitType type = KitType.COCOA;
    @Variable(name = "scrim.apply_kit")
    public boolean applyKit = true;
    @Variable(name = "scrim.auto_explication")
    public boolean explicate = true;
    @Variable(name = "scrim.wait_time")
    public int waitTime = 150;
    @Variable(name = "scrim.exp_multiplier")
    public double multiplier = 1.0;
    @Variable(name = "scrim.anti_clean_system")
    public boolean antiClean = true;

    @Override
    public void handleSidebar(User user) {
        Game game = this;

        GameStage stage = game.getStage();
        String time = format(game.getTime());
        String count = getCount();

        Account account = user.getAccount();
        GameScoreboard scoreboard = user.getScoreboard();

        Language language = account.getLanguage();
        Ranking ranking = account.getRanking();

        EventMode mode = getEventMode();

        if (scoreboard == null) //Preventing NPE
            return;

        scoreboard.updateTitle("§b§l" + game.getName().toUpperCase());

        List<String> scores = new ArrayList<>();
        scores.add(" ");

        if (language == Language.PORTUGUESE) {
            if (explicate && !getExplication().isStarted())
                scores.add("§fAguardando: §7" + time);
            else if (stage == GameStage.WAITING)
                scores.add("§fIniciando em: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvencível por: §7" + time);
            else
                scores.add("§fTempo: §7" + time);

            scores.add("§fJogadores: §7" + count);
        } else {
            if (explicate && !getExplication().isStarted())
                scores.add("§fWaiting: §7" + time);
            else if (stage == GameStage.WAITING)
                scores.add("§fStarting in: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvincible for: §7" + time);
            else
                scores.add("§fGame Time: §7" + time);

            scores.add("§fPlayers: §7" + count);
        }

        if (!user.isAlive()) {
            scores.add(" ");
            scores.add(user.isVanish() ? "§cMODO VANISH" : "§7MODO ESPECTADOR");
        } else {
            Iterator<Kit> iterator = user.getKitList().stream().filter(kit -> !kit.isNone()).iterator();
            int kitCount = 0;

            if (iterator.hasNext())
                scores.add(" ");

            while (iterator.hasNext()) {

                Kit kit = iterator.next();

                if (iterator.hasNext() || kitCount != 0) {
                    kitCount++;
                    scores.add("§fKit " + kitCount + ": §a" + kit.getDisplayName());
                } else
                    scores.add("§fKit: §a" + kit.getDisplayName());
            }

            if (stage == GameStage.PLAYING || stage == GameStage.VICTORY)
                scores.add("§fKills: §a" + user.getKills());
        }

        scores.add("");

        if (mode != EventMode.NONE) {
            if (language == Language.PORTUGUESE)
                scores.add("§fModo: §a" + language.translate(mode.getKey()));
            else
                scores.add("§fType: §a" + language.translate(mode.getKey()));
        }

        scores.add("§fRanking: " + ranking.getColor() + ranking.getName());
        scores.add("");

        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));
        scoreboard.updateLines(scores);
    }

    @Override
    public void start() {
        getExplication().setStarted(true);
        getExplication().setDone(true);
        getVariable("scrim.auto_explication").setActive(false);
        getVariable("scrim.type").setActive(false);

        if (applyKit && type == KitType.MUSHROOM) {

            Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

            while (iterator.hasNext()) {
                Recipe recipe = iterator.next();

                if (recipe.getResult().getType() == Material.MUSHROOM_SOUP) {

                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    List<Material> ingredients = shapelessRecipe.getIngredientList().stream().map(ItemStack::getType).collect(Collectors.toList());

                    if (ingredients.contains(Material.INK_SACK) || ingredients.contains(Material.CACTUS))
                        iterator.remove();
                }
            }
        }

        super.start();
    }

    @Override
    public void execute() {
        int time = getTime();
        GameStage gameStage = getStage();

        Explication explication = getExplication();

        boolean broadcast = time != 0 && time % 30 == 0 || time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time <= 5;

        if (gameStage == GameStage.WAITING) {

            boolean mayExplicate = explicate && !explication.isStarted();

            if (mayExplicate)
                broadcast = false;

            if (getPlugin().getWhitelist().isActive()) {
                setTime(waitTime);
                return;
            }

            if (broadcast)
                broadcast("hg.starting.broadcast", gameStage, time);

            if (time == 15 && !mayExplicate) {
                if (getGame().getVariables().isTeleportAll())
                    teleport(getGame().getVariables().getSpawnpoint(), 8);
            }

            if (time == 0) {

                if (mayExplicate) {
                    setTime(59);
                    explication.setStarted(true);
                    getVariable("scrim.auto_explication").setActive(false);
                    explication.run();
                    return;
                }

                sync(this::start);
                return;
            }
            setTime(time - 1);
        } else if (gameStage == GameStage.INVINCIBILITY) {

            if (broadcast)
                broadcast("hg.invincibility.wears_off", gameStage, time);

            if (time == 0) {
                sync(() -> {

                    new SoupListener(getPlugin());

                    setTime(getVariables().getInvincibility());
                    setStage(GameStage.PLAYING);
                    broadcast("hg.invincibility.end");

                    Bukkit.getOnlinePlayers().forEach(player -> refreshTablist(Account.fetch(player.getUniqueId())));

                });
                return;
            }

            setTime(time - 1);
        } else if (gameStage == GameStage.PLAYING) {
            if (getVariables().isFeastSpawn() && time == getVariables().getFeast()) {

                sync(() -> {
                    if (getFeast().isSpawned()) {
                        setFeast(new Feast(getPlugin(), 300).findLocation(150));
                    }

                    getFeast().countdown();
                });

            } else if (getVariables().isExtraFeastSpawn() && time == getVariables().getExtraFeast()) {

                sync(() -> new BonusFeast(getHungerGames()).parseLocation(270, 380).build().spawn(true));

            } else if (getVariables().isMinifeastSpawn() && time % getVariables().getMiniFeastDelay() == 0) {
                sync(() -> Minifeast.fetch().findLocation(getGame().getVariables().getWorldSize() - 30).prepare().generate().alertExplorers().then(Minifeast::broadcast));
            }

            if (getVariables().isFinalArenaSpawn() && time == getVariables().getFinalArena()) {

                sync(() -> {
                    FinalBattle finalBattle = new FinalBattle(new Location(getGame().getWorld(), 0, 10, 0));
                    finalBattle.setDisableKits(isArenaDisableKits());
                    finalBattle.prepare();
                    finalBattle.teleport();
                });
            } else if (getVariables().isMostEndGameMechanic() && time == getVariables().getMostEndGame()) {

                List<User> users = new ArrayList<>(getPlugin().getUserStorage().getAliveUsers());
                users.sort((a, b) -> Integer.compare(b.getKills(), a.getKills()));

                if (users.isEmpty()) // wtf?
                    Bukkit.shutdown();
                else if (users.size() > 1) {
                    for (int i = 1; i < users.size(); i++) {
                        new LivingUserDieEvent(users.get(i), null, false, DieCause.DIE, new ArrayList<>(), users.get(i).getPlayer().getLocation()).fire();
                    }
                } else {
                    win(Collections.singleton(users.get(0)));
                }
                users.clear();
            }
            setTime(time + 1);
            checkWin();
        }
    }

    private void setupVariables() {
        getVariables().setInvincibility(60);
        getVariables().setCountStats(true);
        getVariables().setSpawnRange(15);
        getVariables().setMostEndGameMechanic(false);
        getVariables().setMinimumPlayers(0);
        getVariables().setInvincibility(60);
        getVariables().setFinalArenaSpawn(false);
        getVariables().setExtraFeastSpawn(false);
        getVariables().setFinalArena(1800);
        setEventMode(EventMode.SOLO);
        setReduceTime(false);
        setArenaDisableKits(false);
        stats();

        getVariable("chat").setValue(false);
    }

    public void setupKits() {
        getKits(' ', "Urgal Stomper Demoman Endermage Launcher Phantom Switcher Fisherman Tank Timelord Achilles AntiTower Madman Poseidon Checkpoint Jumper Werewolf Blacksmith").forEach(kit -> kit.setActive(false, false));

        Blink blink = ((Blink) getKit("Blink"));

        blink.setCombatCooldown(true);
        blink.setMaxUses(3);

        Ninja ninja = ((Ninja) getKit("Ninja"));

        ninja.setCombatCooldown(true);
        ninja.setDuration(0.75);

        Monk monk = ((Monk) getKit("Monk"));
        monk.setCooldown(24);

        Anchor anchor = ((Anchor) getKit("Anchor"));
        anchor.setActive(true, false);

        Toxic toxic = ((Toxic) getKit("Toxic"));
        toxic.setPoisonAmplifier(3);
    }

    @Override
    public void prepare(Player p) {
        if (!applyKit)
            super.prepare(p);
        else {

            User user = User.fetch(p.getUniqueId());
            Account account = user.getAccount();
            p.setExp(0);
            p.setLevel(0);
            p.spigot().setCollidesWithEntities(true);
            for (PotionEffect potionEffect : p.getActivePotionEffects())
                p.removePotionEffect(potionEffect.getType());
            if (((CraftPlayer) p).getHandle().playerConnection != null)
                p.setItemOnCursor(null);
            p.setFireTicks(0);
            p.setSaturation(3.2F);
            p.getInventory().clear();
            if (!(p.getOpenInventory().getTopInventory().getHolder() instanceof Selector.Holder))
                p.getOpenInventory().getTopInventory().clear();
            p.getInventory().setArmorContents(null);

            if (user.isAlive()) {
                type.apply(p);
                for (Kit kit : user.getKits())
                    kit.grant(p);
            } else {
                p.setAllowFlight(true);
                p.setFlying(true);
                Items.SPECTATOR.apply(user);
                if (!account.hasPermission(Rank.PARTNER_PLUS))
                    p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
            }
            p.updateInventory();
            handleSidebar(user);
        }
    }

    @Override
    public void die(User user) {
        if (!applyKit)
            super.die(user);
        else {
            if (!user.isOnline())
                return;

            Account account = user.getAccount();
            Player player = user.getPlayer();

            player.setHealth(20);

            if (((CraftPlayer) player).getHandle().playerConnection != null)
                player.setItemOnCursor(null);

            run(() -> {

                if (player.getPassenger() != null)
                    player.getPassenger().leaveVehicle();

                player.setExp(0);
                player.setLevel(0);
                player.setFoodLevel(20);
                player.setSprinting(true);
                for (PotionEffect potionEffect : player.getActivePotionEffects())
                    player.removePotionEffect(potionEffect.getType());
                player.setFireTicks(0);
                player.setSaturation(3.2F);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.setVelocity(new Vector());
                user.getCombatTag().addTag(null, -1);

                if (!user.isAlive()) {

                    if (account.hasPermission(Rank.VIP))
                        player.teleport(player.getLocation().clone().add(0, 0.2, 0));
                    else {
                        player.teleport(new Location(player.getWorld(), 100, 1.3, 100));
                        player.getInventory().setHeldItemSlot(4);
                    }
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.spigot().setCollidesWithEntities(false);

                    Items.SPECTATOR.apply(user);

                    player.updateInventory();

                    user.getVictims().clear();

                    if (account.hasPermission(Rank.PARTNER_PLUS)) {
                        if (!user.isVanish())
                            Vanish.getInstance().setVanished(player, account.getRank());
                    } else
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);

                } else {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.spigot().setCollidesWithEntities(true);

                    player.getOpenInventory().getTopInventory().clear();

                    type.apply(player);

                    if (account.hasPermission(Rank.PRO)) {
                        for (Kit kit : user.getKits())
                            kit.grant(player);
                    }

                    player.updateInventory();
                }

                Bukkit.getOnlinePlayers().forEach(this::refreshVisibility);
            }, 1L);
        }
    }

    protected void stats() {
        StatsApplier.WIN.setApplier(user -> {

            if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
                return;

            Account account = user.getAccount();
            user.giveCoins(120.0);

            RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.addInt((int) (200 * multiplier), rankingFactory.getTarget().getExperience());
                rankingFactory.verify(account);
                user.getPlayer().sendMessage("§b+200 XP");
            }

            account.addInt(1, Columns.SCRIM_WINS);
            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getEngine(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
        });

        StatsApplier.KILL.setApplier(user -> {

            if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
                return;

            Account account = user.getAccount();
            user.giveCoins(40.0);

            RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.addInt((int) (32 * multiplier), rankingFactory.getTarget().getExperience());
                rankingFactory.verify(account);
                user.getPlayer().sendMessage("§b+" + (32 * multiplier) + " XP");
            }

            account.addInt(1, Columns.SCRIM_KILLS);

            Data data = account.getData(Columns.SCRIM_MAX_GAME_KILLS);

            if (data.getAsInt() < user.getKills())
                data.setData(user.getKills());
        });

        StatsApplier.DEATH.setApplier(user -> {

            if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
                return;

            Account account = user.getAccount();
            account.addInt(1, Columns.SCRIM_DEATHS);
            account.removeInt(5, Columns.HG_COINS);

            RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.removeInt(8, rankingFactory.getTarget().getExperience());
                rankingFactory.verify(account);
                user.getPlayer().sendMessage("§4-8 XP");
            }

            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getEngine(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
        });
    }

    @EventHandler
    public void onPlayerGatherDiamond(CraftItemEvent event) {

        Player player = (Player) event.getWhoClicked();
        User user = User.fetch(player.getUniqueId());

        if (!user.getCombatTag().isTagged() && !((CraftPlayer) player).getHandle().inBlock())
            return;

        for (ItemStack itemStack : event.getInventory().getContents()) {
            if (itemStack.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                player.sendMessage("§cVocê não pode juntar espadas encantadas em combate.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCheckClean(EntityDamageByEntityEvent event) {
        if (!antiClean)
            return;

        if (!event.isBothPlayers())
            return;

        User damaged = User.fetch(event.getEntity().getUniqueId());
        User damager = User.fetch(event.getDamager().getUniqueId());

        if (damager.getScrimSettings().isCleanTime()) {
            damager.getScrimSettings().incrementHits();

            if (damager.getScrimSettings().getHits() >= 3) {
                damager.getScrimSettings().resetClean();
            }
        }

        if (damaged.getScrimSettings().isCleanTime()) {
            damager.getPlayer().sendMessage("§cAguarde " + DateUtils.formatDifference(damaged.getScrimSettings().getCleanTime(), damager.getAccount().getLanguage(), DateUtils.Style.NORMAL) + " para entrar em combate com " + damaged.getPlayer().getName() + ".");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAntiClean(LivingUserDieEvent event) {
        if (!antiClean)
            return;

        if (event.hasKiller() && event.getKiller().isAlive()) {
            event.getKiller().getPlayer().setNoDamageTicks(160);
        }
    }

    public Explication getExplication() {
        return applyKit ? KITSET : MINE;
    }

}
