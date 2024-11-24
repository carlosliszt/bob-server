/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.listener.SoupListener;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.variable.object.VariableValidation;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.ranking.RankingHandler;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.handler.GameExecutor;
import com.minecraft.hungergames.game.handler.listener.*;
import com.minecraft.hungergames.game.object.RecoveryMode;
import com.minecraft.hungergames.game.structure.BonusFeast;
import com.minecraft.hungergames.game.structure.Feast;
import com.minecraft.hungergames.game.structure.FinalBattle;
import com.minecraft.hungergames.game.structure.Minifeast;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.KitStorage;
import com.minecraft.hungergames.user.kits.list.*;
import com.minecraft.hungergames.user.listener.SpectatorListener;
import com.minecraft.hungergames.user.listener.UserDeath;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.arena.FileArena;
import com.minecraft.hungergames.util.bo3.BO3;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.firework.FireworkAPI;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.GameType;
import com.minecraft.hungergames.util.game.GameVariables;
import com.minecraft.hungergames.util.leaderboard.LeaderboardPreset;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import com.minecraft.hungergames.util.selector.Items;
import com.minecraft.hungergames.util.stats.StatsApplier;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Game extends GameExecutor implements VariableStorage, Listener, Assistance, BukkitInterface {

    private List<Listener> registeredListeners = new ArrayList<>();
    private final HungerGames hungerGames;
    @Variable(name = "game_name")
    private String name;
    private GameStage stage;
    private GameType type;
    private int time;
    private transient int room;
    private GameVariables variables;
    private Feast feast;
    private RecoveryMode recoveryMode;
    private final Set<LeaderboardPreset> leaderboardPresets;
    private String display;

    public Game(HungerGames hungerGames) {
        super(hungerGames);
        this.hungerGames = hungerGames;
        this.stage = GameStage.WAITING;
        this.type = GameType.valueOf(hungerGames.getConfig().getString("game.type"));
        this.time = 300;
        this.display = "HG";
        this.room = hungerGames.getConfig().getInt("game.identifier");
        this.recoveryMode = new RecoveryMode();
        this.variables = new GameVariables();
        this.leaderboardPresets = new HashSet<>();

        FileArena.getLoadedArenas().clear();

        Location spawnPoint = new Location(getWorld(), 0.5, hungerGames.getSpawn().getLocation().getY() + 1, 0.5, 0, 0);

        getVariables().setDefaultSpawnpoint(spawnPoint);
        getVariables().setSpawnpoint(spawnPoint.clone());
        getVariables().setSpawnRange(0);

        registerListener(new VictoryListener(), new SpectatorListener(), new UserDeath(), new RoomListener(), new InvincibilityListener(), new PlayingListener(), new WaitingListener(), this);
    }

    public void load() {
        loadVariables();
        this.feast = new Feast(hungerGames, 300).findLocation(130);
        hungerGames.getListenerLoader().handle(stage);
        registerRankingHandler();

        if (getPlugin().getRankingFactory() != null) {

            double y = getVariables().getSpawnpoint().clone().subtract(0, 1, 0).add(0, 2.8, 0).getY();

            RankingTarget rankingTarget = getPlugin().getRankingFactory().getTarget();

            String mode = Constants.getServerType().getName().toUpperCase();

            async(() -> {
                getLeaderboardPresets().add(new LeaderboardPreset(new Location(getWorld(), -7.5, y, 8.5), new Leaderboard(rankingTarget.getWins(), LeaderboardUpdate.NEVER, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query(), "§e§lTOP 100 §b§l" + mode + " WINS §7(%s/%s)"));
                getLeaderboardPresets().add(new LeaderboardPreset(new Location(getWorld(), 8.5, y, 8.5), new Leaderboard(rankingTarget.getExperience(), LeaderboardUpdate.NEVER, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query(), "§e§lTOP 100 §b§l" + mode + " EXP §7(%s/%s)"));
            });
        }

        defineKitsRotation();

        try {
            getVariable("max_players").setDefaultRank(Rank.SECONDARY_MOD);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to change local variables default values", e);
        }
    }

    public void defineKitsRotation() {
        final KitStorage kitStorage = getHungerGames().getKitStorage();

        kitStorage.getKit(Thor.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Trader.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Worm.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Vampire.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Ironman.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Cookiemonster.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Cultivator.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(AutoBowl.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Flash.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Berserker.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Endermage.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Redstoner.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Lumberjack.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Werewolf.class).setPermission(Rank.MEMBER);
        kitStorage.getKit(Surprise.class).setPermission(Rank.MEMBER);

        kitStorage.getKit(Grappler.class).setPermission(Rank.VIP);
        kitStorage.getKit(Jumper.class).setPermission(Rank.VIP);
        kitStorage.getKit(Magic.class).setPermission(Rank.VIP);
        kitStorage.getKit(Boxer.class).setPermission(Rank.VIP);
        kitStorage.getKit(Stomper.class).setPermission(Rank.VIP);
        kitStorage.getKit(Cannibal.class).setPermission(Rank.VIP);
        kitStorage.getKit(Rider.class).setPermission(Rank.VIP);
        kitStorage.getKit(Specialist.class).setPermission(Rank.VIP);
        kitStorage.getKit(Fisherman.class).setPermission(Rank.VIP);
        kitStorage.getKit(Fireman.class).setPermission(Rank.VIP);
        kitStorage.getKit(Gladiator.class).setPermission(Rank.VIP);
    }

    public void unload() {
        getLeaderboardPresets().forEach(c -> c.getLeaderboard().destroy());
        getLeaderboardPresets().clear();
        getRegisteredListeners().forEach(HandlerList::unregisterAll);
        getPlugin().getVariableLoader().getVariables().removeIf(c -> c.getVariableStorage() == this || c.getVariableStorage() == getVariables());
    }

    public void prepare(Player p) {
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
            p.getInventory().addItem(new ItemFactory(Material.COMPASS).setDescription("§7Encontrar jogadores").setName("§aBússola").getStack());
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

    public void die(User user) {
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

                player.getInventory().addItem(new ItemFactory(Material.COMPASS).setDescription("§7Encontrar jogadores").setName("§aBússola").getStack());

                if (account.hasPermission(Rank.PRO)) {
                    for (Kit kit : user.getKits())
                        kit.grant(player);
                }

                player.updateInventory();
            }

            Bukkit.getOnlinePlayers().forEach(this::refreshVisibility);
        }, 1L);
    }

    @Variable(name = "hg.no_players.deadline", permission = Rank.ADMINISTRATOR)
    private int deadLineTimeout = 50;
    private long deadline = -1;

    @Variable(name = "hg.auto_victory", permission = Rank.ADMINISTRATOR)
    @Getter
    private boolean autoVictory = true;

    public void checkWin() {

        if (!autoVictory)
            return;

        List<User> aliveUsers = getPlugin().getUserStorage().getAliveUsers();

        if (aliveUsers.size() == 0) {
            if (deadline != -1 && System.currentTimeMillis() > deadline) {
                Bukkit.broadcastMessage("§4§lFATAL §cThe game waited for players during " + deadLineTimeout + " seconds, as there is no one yet, the room will be restarted.");
                Bukkit.shutdown();
            } else if (deadline == -1)
                deadline = System.currentTimeMillis() + deadLineTimeout * 1000L;
            return;
        } else if (aliveUsers.size() == 1) {

            if (System.currentTimeMillis() < deadline)
                return;

            User winner = aliveUsers.get(0);
            sync(() -> win(Collections.singleton(winner)));
        }

        if (aliveUsers.size() > 1 && deadline != -1) // Checking if anyone joined, so, cancelling the deadline.
            deadline = -1;
    }

    public void win(Set<User> winners) {

        this.hungerGames.getCelebrationStorage().register();
        this.deadline = System.currentTimeMillis() + 15000;

        Location cake = new Location(getWorld(), 0, 130, 0);
        WorldEditAPI.getInstance().makeCylinder(cake, 3, 3, 2, true).forEach(loc -> WorldEditAPI.getInstance().setBlock(loc, loc.getBlockY() == 130 ? Material.GLASS : Material.CAKE_BLOCK, (byte) 0, false));

        setStage(GameStage.VICTORY);

        run(() -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setVelocity(new Vector());
                player.teleport(cake.clone().add(0.5, 1.7, 0.5));
            }

            Iterator<User> userIterator = winners.iterator();

            StringBuilder winnerName = new StringBuilder();

            while (userIterator.hasNext()) {

                User user = userIterator.next();
                Player player = user.getPlayer();

                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getInventory().setHeldItemSlot(0);
                player.setFoodLevel(20);
                player.setHealth(20);
                player.setFireTicks(0);

                if (winners.size() == 1)
                    user.getCelebration().onVictory(player);

                StatsApplier.WIN.apply(user);
                winnerName.append(player.getName());

                if (userIterator.hasNext())
                    winnerName.append(", ");
            }

            Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {

                if (System.currentTimeMillis() > deadline || Bukkit.getOnlinePlayers().isEmpty()) {

                    ServerType serverType = Constants.getServerType();

                    Server server = serverType.getServerCategory().getServerFinder().getBestServer(serverType);

                    if (server == null)
                        server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.HG_LOBBY);

                    if (server != null) {
                        final Server targetServer = server;
                        Bukkit.getOnlinePlayers().forEach(c -> {

                            Account account = Account.fetch(c.getUniqueId());

                            if (account == null) {
                                c.kickPlayer("§c");
                                return;
                            }

                            account.connect(targetServer);
                        });
                    }

                    run(Bukkit::shutdown, 200);
                } else {
                    broadcast("hg.game.victory", winnerName.toString());

                    FireworkAPI.random(cake.clone().add(-10.0D, 0.5, 10.0D));
                    FireworkAPI.random(cake.clone().add(10.0D, 0.5, -10.0D));
                    FireworkAPI.random(cake.clone().add(-10.0D, 0.5, -10.0D));
                    FireworkAPI.random(cake.clone().add(10.0D, 0.5, 10.0D));
                    FireworkAPI.random(cake.clone().add(-5.0D, 0.5, 5.0D));
                    FireworkAPI.random(cake.clone().add(5.0D, 0.5, -5.0D));
                    FireworkAPI.random(cake.clone().add(-5.0D, 0.5, -5.0D));
                    FireworkAPI.random(cake.clone().add(5.0D, 0.5, 5.0D));
                }
            }, 0L, 20L);

            System.out.println("Winner: " + winnerName);
        }, 2L);
    }

    public String getCount() {
        int count = getPlugin().getUserStorage().getAliveUsers().size();
        RecoveryMode recoveryMode = getGame().getRecoveryMode();
        int maxPlayers = getPlugin().getAccountLoader().getMaxPlayers();
        return (recoveryMode.isEnabled() ? count + "/" + recoveryMode.getPlayers() + "/" + maxPlayers : count + "/" + maxPlayers);
    }

    public void handleSidebar(User user) {
        Game game = this;
        GameStage stage = game.getStage();
        String time = format(game.getTime());
        String count = getCount();
        Account account = user.getAccount();
        GameScoreboard scoreboard = user.getScoreboard();

        if (scoreboard == null) //Preventing NPE
            return;

        scoreboard.updateTitle("§b§l" + game.getName().toUpperCase());

        List<String> scores = new ArrayList<>();
        scores.add(" ");

        if (account.getLanguage() == Language.PORTUGUESE) {

            if (stage == GameStage.WAITING)
                scores.add("§fIniciando em: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvencível por: §7" + time);
            else
                scores.add("§fTempo: §7" + time);

            scores.add("§fJogadores: §7" + count);

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
        } else {

            if (stage == GameStage.WAITING)
                scores.add("§fStarting in: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvincible for: §7" + time);
            else
                scores.add("§fGame Time: §7" + time);

            scores.add("§fPlayers: §7" + count);

            if (!user.isAlive()) {
                scores.add(" ");
                scores.add(user.isVanish() ? "§cVANISH MODE" : "§7SPECTATOR MODE");
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
        }

        if (getPlugin().getRankingFactory() != null) {
            scores.add(" ");
            Ranking ranking = account.getRanking();
            scores.add("§fRanking: " + ranking.getColor() + ranking.getName());
        }

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        scoreboard.updateLines(scores);
    }

    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public void setStage(GameStage stage) {
        getHungerGames().getListenerLoader().handle(stage);
        this.stage = stage;
    }

    public void pointCompass(User user, Action action) {

        Player comparator = null;
        Player consumer = user.getPlayer();

        for (User other : getPlugin().getUserStorage().getAliveUsers()) {

            if (other.getUniqueId().equals(user.getUniqueId()))
                continue;

            Player player = other.getPlayer();

            if (player.getLocation().distanceSquared(consumer.getLocation()) >= 225) {
                if (comparator == null || comparator.getLocation().distanceSquared(consumer.getLocation()) > player.getLocation().distanceSquared(consumer.getLocation())) {
                    comparator = player;
                }
            }
        }

        if (comparator == null) {
            consumer.sendMessage("§cNenhum jogador encontrado.");
            consumer.setCompassTarget(getGame().getVariables().getSpawnpoint());
        } else {
            consumer.setCompassTarget(comparator.getLocation());
            consumer.sendMessage(user.getAccount().getLanguage().translate("hg.game.user.compass_pointing_to", comparator.getName()));
        }
    }

    public void registerListener(Object... listeners) {
        for (Object classes : listeners) {
            getRegisteredListeners().add((Listener) classes);
            getPlugin().getListenerLoader().load(classes);
        }
    }

    @Variable(name = "hg.timer.pregame_reduce_time")
    private boolean reduceTime = true;

    @Variable(name = "hg.structure.final_battle.disable_kits")
    private boolean arenaDisableKits = true;

    @Override
    public void execute() {

        int time = getTime();
        GameStage gameStage = getStage();

        final boolean broadcast = time != 0 && time % 30 == 0 || time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time <= 5;

        if (gameStage == GameStage.WAITING) {

            int count = count();

            if (count < getVariables().getMinimumPlayers()) {
                setTime(300);
                return;
            }

            if (reduceTime && time >= 60 && count >= Bukkit.getMaxPlayers()) {
                broadcast("hg.game.time_reduced");
                setTime(time = 40);
            }

            if (broadcast)
                broadcast("hg.starting.broadcast", gameStage, time);

            if (time == 15) {
                if (getGame().getVariables().isTeleportAll())
                    teleport(getGame().getVariables().getSpawnpoint(), 8);
                /*sync(() -> {
                    getHungerGames().getSpawn().getGate().forEach(block -> WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0, false));
                    getHungerGames().getSpawn().getGate().clear();
                });*/
            }

            if (time == 0) {
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
                    finalBattle.setDisableKits(arenaDisableKits);
                    finalBattle.prepare();
                    finalBattle.teleport();
                });
            } else if (getVariables().isFinalCombatSpawn() && time == getVariables().getFinalCombat()) {
                sync(() -> {

                    BO3 bo3;
                    FileArena fileArena = FileArena.getArena("arena_spop");

                    if (fileArena == null) {
                        FileArena.load(new File("/home/ubuntu/misc/hg/structures/arena_spop"));
                        fileArena = FileArena.getArena("arena_spop");
                    }

                    bo3 = fileArena.getBO3();
                    Location loc = new Location(getGame().getWorld(), 0, 10, 0);

                    bo3.spawn(loc, (location, pattern) -> {

                        Block block = location.getBlock();

                        if (pattern.getMaterial() != Material.AIR) {
                            block.setMetadata("unbreakable", new GameMetadata(0));
                        }

                        return true;
                    });

                    PlayerMassiveTeleportExecuteEvent event = new PlayerMassiveTeleportExecuteEvent(getPlugin().getUserStorage().getUsers().stream().map(User::getPlayer).collect(Collectors.toSet()), loc.clone().add(0.5, 0.8, 0.5));
                    event.fire();

                    event.getRecipients().forEach(c -> {

                        Player player = c.getPlayer();

                        player.setNoDamageTicks(20);
                        player.setFallDistance(-1);

                        player.teleport(loc.clone().add(0.5, 0.8, 0.5));
                    });

                    buckets = false;
                    build = false;
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

    @VariableValidation(value = "game_name")
    public boolean isValid(String name) {
        return name.length() <= 28;
    }

    // Variables listeners //

    @Setter
    @Variable(name = "build", announce = true)
    private boolean build = true;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBreakEvent(BlockBreakEvent event) {
        if (!build && !getUser(event.getPlayer().getUniqueId()).isVanish() || event.getBlock().hasMetadata("unbreakable"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPlaceEvent(BlockPlaceEvent event) {
        if (!build && !getUser(event.getPlayer().getUniqueId()).isVanish())
            event.setCancelled(true);
    }

    @Variable(name = "allow_buckets", announce = true)
    private boolean buckets = true;

    @EventHandler
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        if (!buckets && !getUser(event.getPlayer().getUniqueId()).isVanish())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        if (!buckets && !getUser(event.getPlayer().getUniqueId()).isVanish())
            event.setCancelled(true);
    }


    @Variable(name = "combat", announce = true)
    private boolean combat = true;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!combat && event.isBothPlayers())
            event.setCancelled(true);
    }

    @Variable(name = "damage", announce = true)
    private boolean damage = true;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!damage && isPlayer(event.getEntity()))
            event.setCancelled(true);
        else if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
            event.setDamage(event.getDamage() * 0.35);
    }

    @Variable(name = "drops", announce = true)
    private boolean drops = true;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        if (!drops)
            event.setCancelled(true);
    }

    protected void registerRankingHandler() {
        if (getPlugin().getRankingFactory() != null) {

            getPlugin().getRankingFactory().registerRankingHandler(new RankingHandler() {
                @Override
                public void onRankingUpgrade(Account account, Ranking old, Ranking upgrade) {
                    Player player = Bukkit.getPlayer(account.getUniqueId());

                    if (player != null) {
                        player.sendMessage("§eVocê foi promovido para " + upgrade.getColor() + upgrade.getSymbol() + " " + upgrade.getName() + ".");
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 3F);

                        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                        Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                    }
                    async(() -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
                }

                @Override
                public void onRankingDowngrade(Account account, Ranking downgrade) {
                    Player player = Bukkit.getPlayer(account.getUniqueId());

                    if (player != null) {
                        player.sendMessage("§eVocê foi rebaixado para " + downgrade.getColor() + downgrade.getSymbol() + " " + downgrade.getName() + ".");

                        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                        Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                    }

                    async(() -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
                }

                @Override
                public void onChallengerAssign(Account account) {
                    Player player = Bukkit.getPlayer(account.getUniqueId());

                    if (player != null) {
                        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                        Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                    }
                }

                @Override
                public void onChallengerDesign(Account account) {
                    Player player = Bukkit.getPlayer(account.getUniqueId());

                    if (player != null) {
                        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                        Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                    }
                }
            });
        }
    }

}
