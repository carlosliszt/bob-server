/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.auth.listener;

import com.minecraft.auth.Auth;
import com.minecraft.auth.util.Captcha;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public class Listeners implements Listener {

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setTicksLived(1);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setLevel(0);
        player.setExp(0);
        player.setGameMode(GameMode.SURVIVAL);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        GameScoreboard scoreboard = new GameScoreboard(player);
        scoreboard.updateTitle("§b§lAUTH");
        scoreboard.updateLines("", "§fAguardando...", "", "§ewww.bobmc.com.br");

        Bukkit.getOnlinePlayers().forEach(other -> {
            player.hidePlayer(other);
            other.hidePlayer(player);
        });

        Bukkit.getScheduler().runTaskLater(Auth.getAuth(), () -> {
            Captcha captcha = Captcha.newInstance();
            player.setMetadata("captcha_challenge", new FixedMetadataValue(Auth.getAuth(), true));
            player.openInventory(captcha.getInventory());
        }, 3L);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (event.getPlayer().hasMetadata("captcha_challenge")) {
            event.getPlayer().removeMetadata("captcha_challenge", Auth.getAuth());
            Bukkit.getScheduler().runTaskLater(Auth.getAuth(), () -> {
                event.getPlayer().setMetadata("captcha_challenge", new FixedMetadataValue(Auth.getAuth(), true));
                Captcha captcha = Captcha.newInstance();
                event.getPlayer().openInventory(captcha.getInventory());
            }, 2L);
        }
    }

    @EventHandler
    public void onPlayerInitialSpawnEvent(PlayerInitialSpawnEvent event) {
        event.setSpawnLocation(new Location(Bukkit.getWorlds().get(0), 0, 28, 0));
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onBreakBlocks(BlockBreakEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlaceBlocks(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    protected boolean isDev(UUID uuid) {
return uuid.equals(UUID.fromString("be4cdc3a-6cb3-4b28-b9d3-47a7da2a13e4")) || uuid.equals(UUID.fromString("97744a87-b5b0-4701-938d-6cda4268f42c"));
    }


}