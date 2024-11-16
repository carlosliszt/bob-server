/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class Turtle extends Kit {

    public Turtle() {
        setIcon(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setCategory(KitCategory.STRATEGY);
        setPrice(20000);
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (player.isSneaking() && isUser(player))
                event.setDamage(Math.min(event.getDamage(), 2));
        }
    }

}