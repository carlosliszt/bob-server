package com.minecraft.arcade.tiogerson.listener;

import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerShowEvent;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.event.protocol.PacketReceiveEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.translation.Language;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;

import java.util.Set;

public class PlayerListener implements Listener {

    private final ArcadeMain instance;

    public PlayerListener(ArcadeMain arcadeMain) {
        this.instance = arcadeMain;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            User user = User.fetch(player.getUniqueId());

            Room room = user.getRoom();

            if(event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION)
                event.setCancelled(true);

            if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
                event.setCancelled(true);

            if(event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK)
                event.setCancelled(true);

            if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying())
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            User user = User.fetch(player.getUniqueId());
            User damagerUser = User.fetch(damager.getUniqueId());

            Room room = user.getRoom();

            if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying() && !Vanish.getInstance().isVanished(player.getUniqueId()))
                event.setCancelled(true);

        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying())
            event.setCancelled(true);
    }

    @EventHandler
    private void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onAwardAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onVanishEnable(PlayerVanishEnableEvent event) {

        Player player = Bukkit.getPlayer(event.getAccount().getUniqueId());
        User user = User.fetch(event.getAccount().getUniqueId());
        Room room = user.getRoom();

        if (room != null) {
            if (!room.isSpectator(user)) {
                player.sendMessage("§aReentrando como espectador.");
                room.join(user, PlayMode.VANISH, true);
            }
        }
    }

    @EventHandler
    public void onVanishEnable(PlayerVanishDisableEvent event) {

        Player player = Bukkit.getPlayer(event.getAccount().getUniqueId());
        User user = User.fetch(event.getAccount().getUniqueId());
        Room room = user.getRoom();

        if (room != null) {
            if (room.isSpectator(user)) {
                player.sendMessage("§aReentrando como jogador.");
                room.join(user, PlayMode.PLAYER, true);
            }
        }
    }

    @EventHandler
    public void onShow(PlayerShowEvent event) {
        if (!event.getReceiver().getWorld().getUID().equals(event.getTohide().getWorld().getUID()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {
        Account account = event.getAccount();
        User user = User.fetch(account.getUniqueId());

        if (user.isPlaying()) {

            Room room = user.getRoom();

            if (room == null || room.getStage() == RoomStage.WAITING || room.getStage() == RoomStage.STARTING) {
                if (account.hasClan()) {
                    final Clan clan = Constants.getClanService().fetch(account.getData(Columns.CLAN).getAsInt());

                    if (clan == null)
                        return;

                    event.getTeam().setSuffix(" " + ChatColor.valueOf(clan.getColor()) + "[" + clan.getTag().toUpperCase() + "]");
                }
            } else if (room.getEnzo().getMembers().contains(user)) {
                event.getTeam().setPrefix("§9§lENZO§9 ");
                event.getTeam().setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            } else {
                event.getTeam().setPrefix("§c§lTIO§c ");
                event.getTeam().setNameTagVisibility(NameTagVisibility.ALWAYS);

            }


        }
    }
}