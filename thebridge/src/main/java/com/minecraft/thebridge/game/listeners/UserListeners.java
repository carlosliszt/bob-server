package com.minecraft.thebridge.game.listeners;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.actionbar.ActionBar;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.util.MessageUtil;
import com.minecraft.thebridge.TheBridge;
import com.minecraft.thebridge.event.UserDiedEvent;
import com.minecraft.thebridge.event.UserJoinGameEvent;
import com.minecraft.thebridge.event.UserLeaveGameEvent;
import com.minecraft.thebridge.event.UserScorePointEvent;
import com.minecraft.thebridge.game.Game;
import com.minecraft.thebridge.game.enums.GameStage;
import com.minecraft.thebridge.game.task.CageTask;
import com.minecraft.thebridge.team.Team;
import com.minecraft.thebridge.user.User;
import com.minecraft.thebridge.util.Items;
import com.minecraft.thebridge.util.Visibility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class UserListeners implements Listener, BukkitInterface {

    private final CooldownProvider cooldownProvider = CooldownProvider.getGenericInstance();

    @EventHandler
    public void onJoinGame(final UserJoinGameEvent event) {
        final User user = event.getUser();

        if (user.getGame() != null)
            user.getGame().quit(user);

        final Player player = user.getPlayer();
        final Account account = user.getAccount();
        final Game game = event.getGame();
        final PlayMode mode = event.getMode();

        if (mode == PlayMode.PLAYER) {

            game.getGray().getUsers().remove(user);

            if (game.isFull()) {
                player.sendMessage(account.getLanguage().translate("arcade.room.full", game.getCode()));
                user.lobby();
                return;
            } else if (game.getStage() != GameStage.WAITING) {
                player.sendMessage(account.getLanguage().translate("arcade.room.already_started", game.getCode()));
                user.lobby();
                return;
            } else {
                game.findTeam(user);
            }
        } else {
            game.getBlue().getUsers().remove(user);
            game.getRed().getUsers().remove(user);
            game.getGray().getUsers().add(user);
        }

        if (event.isTeleport()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            player.teleport(game.getConfiguration().getSpawnPoint());
        }

        user.restoreCombat();
        user.restorePointsAndKills();

        user.setGame(game);

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getOpenInventory().getTopInventory().clear();
        player.setLevel(0);
        player.setExp(0);

        if (mode == PlayMode.PLAYER) {
            player.setGameMode(GameMode.SURVIVAL);
            player.spigot().setCollidesWithEntities(true);
            player.setFlying(false);
            player.setAllowFlight(false);

            game.sendMessage("§7§k" + player.getName() + "§r§e entrou na sala (§b" + game.getAliveUsers().size() + "§e/§b" + game.getMaxPlayers() + "§e).");

            if (Vanish.getInstance().isVanished(player.getUniqueId()))
                Vanish.getInstance().setVanished(player, null);
        } else {
            if (account.hasPermission(Rank.STREAMER_PLUS)) {
                Vanish.getInstance().setVanished(player, account.getRank());
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.spigot().setCollidesWithEntities(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
            }
            Items.find(account.getLanguage()).build(player);
        }

        Visibility.refresh(player);

        new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class)).fire();

        if (((CraftPlayer) player).getHandle().playerConnection != null)
            player.getInventory().setHeldItemSlot(2);
    }

    @EventHandler
    public void onQuitGame(final UserLeaveGameEvent event) {
        final User user = event.getUser();
        final Game game = event.getGame();
        final Team team = event.getTeam();

        if (game.isSpectator(user)) {
            user.getGame().getGray().getUsers().remove(user);
        } else if (team != null) {
            team.getUsers().remove(user);

            if (game.getStage() == GameStage.STARTING || game.getStage() == GameStage.WAITING) {
                game.sendMessage("§7§k" + user.getName() + "§r§e saiu da sala (§b" + game.getAliveUsers().size() + "§e/§b" + game.getMaxPlayers() + "§e).");
            }
        }

        Player player = user.getPlayer();

        Visibility.refreshWorld(player);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        player.setFireTicks(0);

        if (game.isLock()) {
            game.setLock(null);
        }

        if (game.getStage() == GameStage.PLAYING && game.isCountStats()) {
            final Account account = user.getAccount();

            account.getData(user.getGame().getType().getWinstreak()).setData(0);
            account.addInt(1, user.getGame().getType().getLosses());

            account.removeInt(4, Columns.BRIDGE_RANK_EXP);
            TheBridge.getInstance().getRankingFactory().verify(account);

            async(() -> account.getDataStorage().saveTable(Tables.THE_BRIDGE));
        }

        game.checkWin();
    }

    @EventHandler
    public void onScorePoint(final UserScorePointEvent event) {
        final User user = event.getUser();

        user.addPoint();

        final Game game = event.getGame();

        final Team winner = event.getTeam();
        winner.addPoint();

        int point = winner.getPoints();

        final Team loser = event.getLoser();

        game.sendMessage("§a§m                                                                        ");
        game.sendMessage(center("§b§lThe Bridge"));
        game.sendMessage("");
        game.sendMessage(center(winner.getChatColor() + user.getName() + " §emarcou! §7(§6" + point + "º ponto§7)"));
        game.sendMessage(center(winner.getChatColor() + "§l" + point + " §7- " + loser.getChatColor() + "§l" + loser.getPoints()));
        game.sendMessage("");
        game.sendMessage("§a§m                                                                        ");

        game.playSound(Sound.FIREWORK_BLAST, 3.5F, 3.5F);

        game.checkWin();

        CageTask cageTask = new CageTask(game);

        cageTask.setTitle(winner.getChatColor() + user.getName() + " marcou!");
        cageTask.run();

        cooldownProvider.removeCooldown(user.getPlayer(), user.getPlayer().getWorld().getUID().toString());
        ActionBar.sendActionBar(user.getPlayer(), " ");

        if (game.isCountStats())
            user.getAccount().addInt(1, game.getType().getPoints());
    }

    @EventHandler
    public void onUserDiedEvent(UserDiedEvent event) {
        final User killed = event.getKilled();
        final Player player = killed.getPlayer();
        final Game game = event.getGame();

        if (game == null || !killed.isPlaying())
            return;

        final boolean countStats = game.isCountStats();

        if (event.hasKiller()) {
            final User killer = event.getKiller();

            killer.restoreCombat();
            killer.addKill();

            final Player killer_player = killer.getPlayer();

            killer_player.playSound(killer_player.getLocation(), Sound.ORB_PICKUP, 4F, 4F);

            if (countStats)
                killer.getAccount().addInt(1, game.getType().getKills());

            game.sendMessage(killed.getTeam().getChatColor() + player.getName() + "§e foi morto por " + killer.getTeam().getChatColor() + killer_player.getName() + "§e.");
        } else game.sendMessage(killed.getTeam().getChatColor() + player.getName() + " §emorreu.");

        final Team team = killed.getTeam();

        Location location;
        if (team.getChatColor() == game.getBlue().getChatColor()) {
            location = game.getConfiguration().getBlueLocation();
        } else location = game.getConfiguration().getRedLocation();

        player.teleport(location);
        player.setVelocity(new Vector(0, 0, 0));
        player.setHealth(20.0D);
        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);

        killed.restoreCombat();

        player.setItemOnCursor(null);
        player.getOpenInventory().getTopInventory().clear();

        cooldownProvider.removeCooldown(player, player.getWorld().getUID().toString());
        ActionBar.sendActionBar(player, " ");

        killed.giveItems(team);
        player.playSound(player.getLocation(), Sound.HURT_FLESH, 3.0F, 3.0F);

        if (countStats)
            killed.getAccount().addInt(1, game.getType().getDeaths());
    }

    protected final String center(final String message) {
        return MessageUtil.makeCenteredMessage(message);
    }

}