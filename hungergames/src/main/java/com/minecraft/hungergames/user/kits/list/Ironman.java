/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Ironman extends Kit {

    public Ironman(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.IRON_INGOT));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        giveItem(event.getKiller().getPlayer(), new ItemStack(Material.IRON_INGOT, 1), event.getKiller().getPlayer().getLocation().clone().add(0, 0.5, 0));
    }

}