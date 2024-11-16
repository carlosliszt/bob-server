/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Poseidon extends Kit {

    public Poseidon() {
        setIcon(new ItemStack(Material.WATER_BUCKET));
        setCategory(KitCategory.MOVEMENT);
        setPrice(10000);
    }

    @EventHandler
    public void move(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (isUser(player)) {
            Block block = player.getLocation().getBlock();
            if (MATERIALS.contains(block.getType())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 118, 0), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 118, 0), true);
            }
        }
    }

    private final ImmutableSet<Material> MATERIALS = Sets.immutableEnumSet(Material.WATER, Material.STATIONARY_WATER);

    @Override
    public void resetAttributes(User user) {
    }

}