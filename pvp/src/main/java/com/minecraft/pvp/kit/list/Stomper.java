/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class Stomper extends Kit {

    public Stomper() {
        setIcon(new ItemStack(Material.IRON_BOOTS));
        setCategory(KitCategory.STRATEGY);
        setPrice(35000);
        setLimit(20);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity();

            if (!isUser(player))
                return;

            double dmg = event.getDamage();

            if (dmg <= 2)
                return;

            event.setDamage(Math.min(dmg, 4));

            for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                if (!(entity instanceof Player))
                    continue;

                Player target = (Player) entity;

                if (target.getGameMode().equals(GameMode.CREATIVE))
                    continue;

                double damage = dmg;

                if (target.isSneaking())
                    damage = 4;

                User user = User.fetch(target.getUniqueId());

                if (user.isKept())
                    return;

                if (user.getKit1().getName().equalsIgnoreCase("AntiTower") || user.getKit2().getName().equalsIgnoreCase("AntiTower"))
                    damage = 1;

                if ((target.getHealth() - damage) <= 0) {
                    new UserDiedEvent(user, User.fetch(player.getUniqueId()), user.getInventoryContents(), target.getLocation(), UserDiedEvent.Reason.KILL, user.getGame()).fire();
                } else {
                    target.damage(damage, player);
                }
            }
        }
    }

    @Override
    public void resetAttributes(User user) {

    }

}