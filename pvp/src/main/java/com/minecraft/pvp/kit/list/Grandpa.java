/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public class Grandpa extends Kit {

    public Grandpa() {
        setIcon(new ItemFactory(Material.STICK).glow().getStack());
        setCategory(KitCategory.STRATEGY);
        setPrice(15000);
        setDefaultRank(Rank.VIP);
        setItems(new ItemFactory(Material.STICK).setName("§aPush").addEnchantment(Enchantment.KNOCKBACK, 2).setDescription("§7Kit Grandpa").getStack());
    }

    @Override
    public void resetAttributes(User user) {

    }

}