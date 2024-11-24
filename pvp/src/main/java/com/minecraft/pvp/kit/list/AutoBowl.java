/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.bukkit.event.player.PlayerSoupDrinkEvent;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class AutoBowl extends Kit {

    public AutoBowl() {
        setIcon(new ItemStack(Material.BOWL));
        setCategory(KitCategory.NONE);
        setPrice(25000);
        setActive(false);
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSoupDrink(PlayerSoupDrinkEvent event) {
        final Player player = event.getPlayer();

        if (isUser(player)) {
            event.setItemStack(new ItemStack(Material.AIR));
            player.getInventory().addItem(new ItemStack(Material.BOWL));
        }
    }
}
