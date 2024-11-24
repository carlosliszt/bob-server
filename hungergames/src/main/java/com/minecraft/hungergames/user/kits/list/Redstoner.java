/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Redstoner extends Kit {

    public Redstoner(HungerGames hungerGames) {
        super(hungerGames);
        setItems();
        setIcon(Pattern.of(Material.PISTON_BASE));
        setKitCategory(KitCategory.STRATEGY);
    }

    @Override
    public void grant(Player player) {
        player.getInventory().addItem(new ItemStack(Material.PISTON_BASE, 32), new ItemStack(Material.REDSTONE, 64),
                new ItemStack(Material.DIODE, 16), new ItemStack(Material.SLIME_BALL, 16),
                new ItemStack(Material.DISPENSER, 4));
    }
}
