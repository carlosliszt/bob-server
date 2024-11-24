/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.handler.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.pattern.Condition;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.leaderboard.LeaderboardPreset;
import com.minecraft.hungergames.util.selector.Items;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;

@RecurringListener(register = GameStage.WAITING, unregister = GameStage.INVINCIBILITY)
public class WaitingListener implements Listener, BukkitInterface, Assistance, VariableStorage {

    public WaitingListener() {
        loadVariables();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreSpawn(PlayerInitialSpawnEvent event) {
        Location location = HungerGames.getInstance().getGame().getVariables().getSpawnpoint().clone();
        int range = HungerGames.getInstance().getGame().getVariables().getSpawnRange();
        event.setSpawnLocation(location.add(randomize(range), 0, randomize(range)));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player p = event.getPlayer();
        Account account = Account.fetch(p.getUniqueId());
        User user = getUser(p.getUniqueId());

        p.getInventory().clear();
        p.setLevel(0);
        p.setExp(0);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.getActivePotionEffects().clear();

        user.setCondition(Condition.ALIVE);
        user.loadKits();

        if (account.hasPermission(Rank.PARTNER_PLUS))
            Vanish.getInstance().setVanished(p, account.getRank());

        Items.STARTING.apply(user);

        for (LeaderboardPreset leaderboard : getGame().getLeaderboardPresets()) {
            LeaderboardHologram hologram = new LeaderboardHologram(leaderboard.getLeaderboard(), leaderboard.getDisplayName(), event.getPlayer(), leaderboard.getLocation());
            hologram.show();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanishEnable(PlayerVanishEnableEvent event) {
        if (!event.isCancelled()) {
            User user = getUser(event.getAccount().getUniqueId());
            user.setCondition(Condition.SPECTATOR);
            run(() -> refreshVisibility(user.getPlayer()), 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanishDisable(PlayerVanishDisableEvent event) {
        if (!event.isCancelled()) {
            User user = User.fetch(event.getAccount().getUniqueId());
            user.setCondition(Condition.ALIVE);
            getGame().handleSidebar(user);
            run(() -> refreshVisibility(user.getPlayer()), 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || !Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        HungerGames.getInstance().getUserStorage().forget(e.getPlayer().getUniqueId());
    }

    @Variable(name = "hg.pregame_build")
    private boolean build = false;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()) || !build)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()) || !build)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

}