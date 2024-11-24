/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Nenhum extends Kit {

    public Nenhum(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.ITEM_FRAME));
    }

    @Override
    public void grant(Player player) {
    }

    @Override
    public void setActive(boolean bool, boolean b) {
    }

    @Override
    public void removeItems(Player player) {
    }

    @Override
    public boolean isCombatCooldown() {
        return false;
    }
}
