/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.listeners;

import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.user.User;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStopDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.isCancelled())
                return;

            Player p = (Player) event.getEntity();
            EntityPlayer handle = ((CraftPlayer) p).getHandle();

            if (p.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true);
                p.setHealth(20.0D);

                User user = User.fetch(p.getUniqueId());
                UUID lastCombatUUID = user.getLastCombat();
                Player killer = null;

                if (lastCombatUUID != null && (killer = Bukkit.getPlayer(lastCombatUUID)) != null) {
                    handle.killer = ((CraftPlayer) killer).getHandle();
                } else {
                    handle.killer = null;
                }

                List<ItemStack> items = new ArrayList<>();

                for (ItemStack content : p.getInventory().getContents()) {
                    if (content == null)
                        continue;
                    if (content.getType() == Material.AIR)
                        continue;
                    items.add(content);
                }

                for (ItemStack content : p.getInventory().getArmorContents()) {
                    if (content == null)
                        continue;
                    if (content.getType() == Material.AIR)
                        continue;
                    items.add(content);
                }

                UserDiedEvent diedEvent = new UserDiedEvent(user, killer == null ? null : User.fetch(killer.getUniqueId()), items, p.getLocation(), UserDiedEvent.Reason.KILL, user.getGame());
                diedEvent.fire();
            }
        }
    }

    @EventHandler
    public void ifDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitListener(PlayerQuitEvent e) {
        User killed = User.fetch(e.getPlayer().getUniqueId());

        if (!killed.inCombat())
            return;

        User killer = User.fetch(killed.getLastCombat());

        UserDiedEvent diedEvent = new UserDiedEvent(killed, killer, killed.getInventoryContents(), killed.getPlayer().getLocation(), UserDiedEvent.Reason.LOGOUT, killed.getGame());
        diedEvent.fire();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!event.isBothPlayers())
            return;

        User damaged = User.fetch(event.getEntity().getUniqueId());
        User damager = User.fetch(event.getDamager().getUniqueId());

        if (damaged.isKept() || damager.isKept()) {
            event.setCancelled(true);
        } else {
            damaged.addCombat(damager.getUniqueId());
            damager.addCombat(damaged.getUniqueId());
        }
    }

}