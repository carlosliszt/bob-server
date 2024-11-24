/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.event.player.PlayerSoupDrinkEvent;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class AutoBowl extends Kit {

    public AutoBowl(HungerGames hungerGames) {
        super(hungerGames);
        setDisplayName("Auto Bowl");
        setIcon(Pattern.of(Material.BOWL));
        setKitCategory(KitCategory.NONE);
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