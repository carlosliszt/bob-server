/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Camel extends Kit {

    public Camel() {
        setIcon(new ItemStack(Material.SAND));
        setCategory(KitCategory.MOVEMENT);
        setPrice(10000);
        setDefaultRank(Rank.MEMBER);
    }

    @EventHandler
    public void move(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (isUser(player)) {
            if (BIOMES.contains(player.getLocation().getBlock().getBiome())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 118, 0), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 118, 0), true);
            }
        }
    }

    private final ImmutableSet<Biome> BIOMES = Sets.immutableEnumSet(Biome.DESERT, Biome.MESA);

    @Override
    public void resetAttributes(User user) {
    }

}