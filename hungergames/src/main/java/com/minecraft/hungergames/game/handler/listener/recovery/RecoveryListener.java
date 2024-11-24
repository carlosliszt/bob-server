/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.handler.listener.recovery;

import com.minecraft.core.account.Account;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.event.game.GameRecoveryModeToggleEvent;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.object.RecoveryMode;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.object.AwaySession;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.*;

import java.util.Iterator;

public class RecoveryListener implements Listener, Assistance {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRescueDisable(GameRecoveryModeToggleEvent event) {
        RecoveryMode recoveryMode = event.getGame().getRecoveryMode();
        if (!recoveryMode.isEnabled()) {
            HandlerList.unregisterAll(this);
            getPlugin().getKitStorage().register();

            Iterator<User> iterator = getPlugin().getUserStorage().getAwayUsers().iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                AwaySession awaySession = user.getAwaySession();

                if (user.isOnline()) {
                    iterator.remove();
                    continue;
                }

                if (!awaySession.isLocked()) {
                    LivingUserDieEvent livingClientDieEvent = new LivingUserDieEvent(user, null, false, DieCause.TIMEOUT, user.getInventoryContents(), user.getPlayer().getLocation());
                    livingClientDieEvent.fire();
                    awaySession.invalidate();
                    user.setAwaySession(null);
                    iterator.remove();
                } else
                    awaySession.setRemainingTime((getGame().getVariables().getTimeout() / 2));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        User client = getUser(event.getUniqueId());
        if (client == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("§cO servidor está em modo recuperação e não está aceitando novos jogadores.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        User client = getUser(event.getPlayer().getUniqueId());

        if (client.isAlive() && client.getAwaySession() != null) {
            if (!client.getAwaySession().isLocked()) {
                event.setKickMessage("§cO servidor está em modo recuperação e não está aceitando novos jogadores.");
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProccessCommand(PlayerCommandPreprocessEvent event) {
        if (!Account.fetch(event.getPlayer().getUniqueId()).hasPermission(Rank.TRIAL_MODERATOR))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCraftEvent(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Account.fetch(event.getPlayer().getUniqueId()).hasPermission(Rank.TRIAL_MODERATOR))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreakEvent(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

}
