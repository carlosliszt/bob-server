/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class Grandpa extends Kit {

    public Grandpa(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.STICK, true));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.STICK).setName("§aPush").addEnchantment(Enchantment.KNOCKBACK, 3).setDescription("§7Kit Grandpa").getStack());
    }
}
