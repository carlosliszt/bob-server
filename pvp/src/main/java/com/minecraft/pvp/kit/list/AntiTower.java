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
import org.bukkit.inventory.ItemStack;

public class AntiTower extends Kit {

    public AntiTower() {
        setIcon(new ItemStack(Material.DIAMOND_HELMET));
        setCategory(KitCategory.COMBAT);
        setPrice(30000);
        setDefaultRank(Rank.VIP);
    }

    @Override
    public void resetAttributes(User user) {

    }

}
