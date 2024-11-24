/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.listeners;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.event.player.PlayerShowEvent;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.event.server.ServerPayloadSendEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.enums.Ranking;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.GameMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

public class ServerListeners implements Listener, BukkitInterface {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onServerPayloadSendEvent(ServerPayloadSendEvent event) {
        PvP.getPvP().getGameStorage().getGames().forEach(game -> event.getPayload().write(game.getName(), game.getPlayingUsers().size()));
    }

    @EventHandler
    public void onPlayerVanishEnableEvent(PlayerVanishEnableEvent event) {
        User user = User.fetch(event.getAccount().getUniqueId());
        user.getGame().rejoin(user, Game.Rejoin.VANISH);
        PvP.getPvP().getVisibility().update();
    }

    @EventHandler
    public void onPlayerVanishDisableEvent(PlayerVanishDisableEvent event) {
        User user = User.fetch(event.getAccount().getUniqueId());
        user.getGame().rejoin(user, Game.Rejoin.PLAYER);
        PvP.getPvP().getVisibility().update();
    }

    @EventHandler
    public void ignite(ExplosionPrimeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerShowEvent(PlayerShowEvent event) {
        if (!event.getReceiver().getWorld().getUID().equals(event.getTohide().getWorld().getUID()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onUpdateVanish(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(1))
            return;
        PvP.getPvP().getVisibility().tick();
    }

    @EventHandler
    public void onRefreshVanish(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(36000))
            return;
        async(() -> PvP.getPvP().getRankingFactory().query());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (!p.isOnGround())
            return;
        Location standBlock = p.getLocation().clone().add(0, -0.00001, 0);
        if (standBlock.getBlock().getType() == Material.SPONGE) {
            p.setVelocity(new Vector(0.0D, 8.0D, 0.0D));
            p.setMetadata("NO_DAMAGE", new GameMetadata(true));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        Player p = (Player) event.getEntity();
        if (p.hasMetadata("NO_DAMAGE")) {
            event.setCancelled(true);
            p.removeMetadata("NO_DAMAGE", PvP.getPvP());
        }
    }

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {
        Ranking ranking = event.getAccount().getRanking();
        event.getTeam().setSuffix(" " + ranking.getColor() + ranking.getDisplay() + ranking.getSymbol());
        event.getTeam().setCanSeeFriendlyInvisibles(false);
    }

    @EventHandler
    public void onProjectile(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow)
            event.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            User user = User.fetch(event.getEntity().getUniqueId());

            Kit kit = user.getKit1();

            if (kit.isCombatCooldown()) {
                kit.addCooldown(user.getUniqueId(), Kit.CooldownType.COMBAT, kit.getCombatTime());
            }

            kit = user.getKit2();

            if (kit.isCombatCooldown()) {
                kit.addCooldown(user.getUniqueId(), Kit.CooldownType.COMBAT, kit.getCombatTime());
            }
        }
    }

    private final ImmutableSet<Material> CHECK_MATERIALS = Sets.immutableEnumSet(Material.CHEST, Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.FURNACE, Material.WORKBENCH, Material.JUKEBOX, Material.ENDER_CHEST, Material.HOPPER, Material.HOPPER_MINECART, Material.DROPPER, Material.DISPENSER);

    @EventHandler
    public void onInteractChest(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        if (CHECK_MATERIALS.contains(block.getType())) {
            if (!block.hasMetadata("openable"))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreakBlocks(BlockBreakEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlaceBlocks(BlockPlaceEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

}