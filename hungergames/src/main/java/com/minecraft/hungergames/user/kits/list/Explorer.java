/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;

public class Explorer extends Kit {

    public Explorer(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.MAP));
        setKitCategory(KitCategory.STRATEGY);
    }

}