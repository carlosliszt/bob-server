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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Boxer extends Kit {

    public Boxer() {
        setIcon(new ItemStack(Material.STONE_SWORD));
        setCategory(KitCategory.COMBAT);
        setPrice(15000);
        setDefaultRank(Rank.VIP);
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            if (isUser((Player) event.getEntity())) {
                event.setDamage(event.getDamage() - 1);
            }
        }
    }

}