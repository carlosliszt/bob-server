/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Neo extends Kit {

    public Neo() {
        setIcon(new ItemStack(Material.ARROW));
        setCategory(KitCategory.STRATEGY);
        setPrice(30000);
        setDefaultRank(Rank.VIP);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {
            if (isUser((Player) event.getEntity()))
                event.setCancelled(true);
        }
    }

    @Override
    public void resetAttributes(User user) {

    }

}