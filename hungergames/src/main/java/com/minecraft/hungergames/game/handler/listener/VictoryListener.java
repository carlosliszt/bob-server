/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.handler.listener;

import com.minecraft.core.bukkit.event.server.ServerPayloadSendEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RecurringListener(register = GameStage.VICTORY, unregister = GameStage.NONE)
public class VictoryListener implements Listener, Assistance, BukkitInterface {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPayloadSendEvent(ServerPayloadSendEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        HungerGames.getInstance().getUserStorage().forget(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cSala não disponível. (reason=WIN)");
    }

}
