package com.minecraft.hungergames.game.list;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.command.TeleportallCommand;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.command.KitsCommand;
import com.minecraft.hungergames.command.TeamCommand;
import com.minecraft.hungergames.command.TimeCommand;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.team.Team;
import com.minecraft.hungergames.game.team.TeamStorage;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.Loadable;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;

@Getter
@Loadable
public class ClanxClan extends Game {

    private final TeamStorage teamStorage;

    public ClanxClan(HungerGames hungerGames) {
        super(hungerGames);

        setName("Clan x Clan");

        setDisplay("CLAN X CLAN");

        this.teamStorage = new TeamStorage(
                hungerGames,
                new Team(0, "hg.teams.red", ChatColor.RED, Color.fromRGB(255, 85, 85)),
                new Team(1, "hg.teams.blue", ChatColor.BLUE, Color.fromRGB(85, 85, 255))
        );

        Constants.setServerType(ServerType.CLANXCLAN);
        getVariables().setMostEndGameMechanic(false);
        getVariables().setInvincibility(600);
        getVariables().setFeast(610);
        getVariables().setFinalArena(1800);
        getVariables().setFinalCombat(2100);
        getVariables().setCountStats(false);
        setDamage(false);
        hungerGames.getWhitelist().setActive(true);
        hungerGames.getWhitelist().setMinimumRank(Rank.PARTNER_PLUS);
        getPlugin().setRankingFactory(null);
        getPlugin().getBukkitFrame().registerCommands(new TeamCommand());

        getPlugin().getBukkitFrame().getCommands(TimeCommand.class).forEach(c -> c.getCommandInfo().setRank(Rank.PARTNER_PLUS));
        getPlugin().getBukkitFrame().getCommands(KitsCommand.class).forEach(c -> c.getCommandInfo().setRank(Rank.PARTNER_PLUS));
        getPlugin().getBukkitFrame().getCommands(TeleportallCommand.class).forEach(c -> c.getCommandInfo().setRank(Rank.PARTNER_PLUS));
    }

    @Override
    public void load() {
        super.load();
        try {
            getVariable("hg.respawn").setValue(false);
            getVariable("hg.kit.default").setValue(getKit("Miner"));
            getVariable("hg.border.max_y").setDefaultRank(Rank.PARTNER_PLUS);
            getVariable("hg.border.max_radius").setDefaultRank(Rank.PARTNER_PLUS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unload() {
        super.unload();
        getPlugin().getBukkitFrame().unregisterCommand("team");
    }

    @Variable(name = "team.friendly_fire")
    public boolean friendlyFire = false;

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {

        User user = getUser(event.getAccount().getUniqueId());

        if (user.hasTeam()) {
            event.getTeam().setPrefix(user.getTeam().getChatColor().toString());
            event.getTeam().setDisplayName("tag:Z" + user.getTeam().getId() + user.getPlayer().getEntityId());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (friendlyFire)
            return;

        if (event.isBothPlayers()) {

            User entity = getUser(event.getEntity().getUniqueId());
            User attacker = getUser(event.getDamager().getUniqueId());

            if (isFriendly(entity, attacker))
                event.setCancelled(true);

        } else if (isPlayer(event.getEntity()) && event.getDamager() instanceof Projectile) {

            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() != null && projectile.getShooter() instanceof Player) {

                User entity = getUser(event.getEntity().getUniqueId());
                User attacker = getUser(((Player) projectile.getShooter()).getUniqueId());

                if (isFriendly(entity, attacker))
                    event.setCancelled(true);
            }
        }
    }

    private void refresh() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(player.getUniqueId());
            new PlayerUpdateTablistEvent(account, account.getProperty("account_tag", account.getTagList().getHighestTag()).getAs(Tag.class), account.getProperty("account_prefix_type", PrefixType.DEFAULT).getAs(PrefixType.class)).fire();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {

        if (!hasStarted())
            return;

        Player player = event.getPlayer();
        User localUser = getUser(player.getUniqueId());

        if (!localUser.isAlive() || !localUser.hasTeam())
            return;

        event.setCancelled(true);

        Team team = localUser.getTeam();

        Set<Player> recipients = event.getRecipients();

        recipients.removeIf(other -> {

            User user = getUser(other.getUniqueId());

            if (user.isAlive() && team.isMember(user))
                return false;

            return user.isAlive() || !user.getAccount().hasPermission(Rank.PARTNER_PLUS);
        });

        Account account = Account.fetch(player.getUniqueId());

        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(account.getUniqueId(), "chat.cooldown");

        if (cooldown != null && !cooldown.expired()) {
            event.getPlayer().sendMessage(account.getLanguage().translate("wait_to_chat", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        } else {
            CooldownProvider.getGenericInstance().addCooldown(event.getPlayer().getUniqueId(), "chat.cooldown", 3, false);
        }

        Tag tag = account.getProperty("account_tag").getAs(Tag.class);

        recipients.forEach(recipient -> {
            Account account_recipient = Account.fetch(recipient.getUniqueId());
            PrefixType prefixType = account_recipient.getProperty("account_prefix_type").getAs(PrefixType.class);
            String prefix = team.getChatColor() + "[" + account_recipient.getLanguage().translate(team.getName()) + "] ";
            recipient.sendRawMessage(prefix + (tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag).replace("#", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+")) + account.getDisplayName() + " §7»§r " + event.getMessage());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVanishEnable(PlayerVanishEnableEvent event) {
        if (!event.isCancelled()) {

            if (hasStarted())
                return;

            User user = User.fetch(event.getAccount().getUniqueId());

            if (user.hasTeam()) {
                user.getTeam().getMembers().remove(user);
                user.setTeam(null);
                refresh();
            }
        }
    }

    @Override
    public void handleSidebar(User user) {

        Game game = this;
        GameStage stage = game.getStage();
        String time = format(game.getTime());
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

            scores.add("§fTimes: ");

            boolean waiting = stage == GameStage.WAITING;

            for (Team team : getTeamStorage().getTeams()) {

                if (!waiting && team.getMembers().size() == 0)
                    continue;

                String alive = (waiting ? " em fila" : team.getMembers().size() == 1 ? " vivo" : " vivos");

                scores.add(" " + team.getChatColor() + (team.isMember(user) ? ChatColor.ITALIC : "") + account.getLanguage().translate(team.getName()) + " §r" + team.getMembers().size() + alive);
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
        } else {

            if (stage == GameStage.WAITING)
                scores.add("§fStarting in: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvincible for: §7" + time);
            else
                scores.add("§fGame Time: §7" + time);

            scores.add("§fTeams: ");

            boolean waiting = stage == GameStage.WAITING;

            for (Team team : getTeamStorage().getTeams()) {

                if (!waiting && team.getMembers().size() == 0)
                    continue;

                String alive = (waiting ? " in queue" : " alive");

                scores.add(" " + team.getChatColor() + (team.isMember(user) ? ChatColor.ITALIC : "") + account.getLanguage().translate(team.getName()) + " §r" + team.getMembers().size() + alive);
            }

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

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        scoreboard.updateLines(scores);
    }

    @Override
    public void checkWin() {

        if (!isAutoVictory())
            return;

        List<Team> teams = new ArrayList<>(getTeamStorage().getTeams());
        teams.removeIf(team -> team.getMembers().isEmpty());

        if (teams.isEmpty()) {
            Bukkit.shutdown();
        } else if (teams.size() == 1) {
            Set<User> users = new HashSet<>(teams.get(0).getMembers());
            win(users);
        }
        teams.clear();
    }

    public boolean isFriendly(User user1, User user2) {
        if (!user1.hasTeam() || !user2.hasTeam())
            return false;
        return user1.getTeam().equals(user2.getTeam());
    }

    protected void selector(User user) {

        List<ItemStack> itemStacks = new ArrayList<>();

        int limit = getTeamStorage().getMaxSlots();

        for (Team team : getTeamStorage().getTeams()) {

            ItemFactory itemFactory = new ItemFactory(Material.LEATHER_CHESTPLATE).setColor(team.getColor());

            if (team.isMember(user))
                itemFactory.glow();

            int count = team.getMembers().size();

            itemFactory.setName(team.getChatColor() + user.getAccount().getLanguage().translate(team.getName()));
            List<String> description = new ArrayList<>();
            description.add("§aPlayers: §f" + team.getMembers().size() + "/" + getTeamStorage().getMaxSlots());
            description.add(" ");
            if (count >= limit)
                description.add("§cEsse time está lotado!");
            else if (!team.isMember(user))
                description.add("§eClique para selecionar!");
            else
                description.add("§cClique para sair!");
            itemFactory.setDescription(description);

            itemStacks.add(new InteractableItem(itemFactory.getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {

                    if (Vanish.getInstance().isVanished(player.getUniqueId())) {
                        player.sendMessage(user.getAccount().getLanguage().translate("hg.game.not_alive"));
                    } else if (!team.isMember(user)) {

                        if (team.getMembers().size() >= limit) {
                            player.sendMessage("§cEste time está lotado.");
                            return true;
                        }

                        if (user.hasTeam()) {
                            Team currentTeam = user.getTeam();
                            currentTeam.getMembers().remove(user);
                        }

                        user.setTeam(team);
                        team.getMembers().add(user);
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                        player.sendMessage("§eVocê selecionou o time " + team.getChatColor() + user.getAccount().getLanguage().translate(team.getName()));
                    } else if (user.hasTeam()) {
                        Team currentTeam = user.getTeam();
                        user.setTeam(null);
                        currentTeam.getMembers().remove(user);
                        player.sendMessage("§cVocê saiu do time " + team.getChatColor() + user.getAccount().getLanguage().translate(team.getName()));
                    }
                    player.closeInventory();
                    refresh();
                    return true;
                }
            }).getItemStack());
        }

        Selector.Builder builder = new Selector.Builder();

        builder.withSize(27);
        builder.withItems(itemStacks);
        builder.withPreviousPageSlot(18);
        builder.withNextPageSlot(26);
        builder.withName(user.getAccount().getLanguage() == Language.PORTUGUESE ? "Selecionar time" : "Select team");
        builder.build().open(user.getPlayer());
    }
}