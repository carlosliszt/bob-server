/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Hulk extends Kit {

    public Hulk() {
        setIcon(new ItemStack(Material.SADDLE));
        setCategory(KitCategory.STRATEGY);
        setPrice(10000);
        setDefaultRank(Rank.VIP);
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {

            Player player = event.getPlayer();
            Player clicked = (Player) event.getRightClicked();

            if (player.isInsideVehicle() || clicked.isInsideVehicle())
                return;

            if (isUser(player)) {

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                User targetUser = User.fetch(clicked.getUniqueId());

                if (!clicked.isOnline() || targetUser == null || Vanish.getInstance().isVanished(clicked.getUniqueId()) || !clicked.getWorld().getName().equals(player.getWorld().getName()) || targetUser.isKept())
                    return;

                player.setPassenger(clicked);
                addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 16);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            if (event.getDamager().getPassenger() != null && event.getDamager().getPassenger().getUniqueId() == event.getEntity().getUniqueId()) {
                Vector v = ((Player) event.getDamager()).getEyeLocation().getDirection().setY(0.7D).multiply(1.2F);
                event.getDamager().eject();
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> event.getEntity().setVelocity(v), 3L);
            }
        }
    }

}