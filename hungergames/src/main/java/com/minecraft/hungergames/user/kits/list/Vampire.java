/*
 * Copyright (C) BlazeMC, All Rights Reserved
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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Vampire extends Kit {

    public Vampire(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.REDSTONE));
        setKitCategory(KitCategory.COMBAT);
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        Player player = event.getKiller().getPlayer();

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 3), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 800, 1), true);
    }

}