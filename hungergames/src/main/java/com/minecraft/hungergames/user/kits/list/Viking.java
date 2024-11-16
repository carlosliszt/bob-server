/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Viking extends Kit {

    public Viking(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.STONE_AXE));
        setPrice(50000);
        setKitCategory(KitCategory.COMBAT);
    }

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE);

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!event.isBothPlayers())
            return;

        Player player = (Player) event.getDamager();

        if (player.getItemInHand() == null)
            return;

        if (ACCEPTABLE_MATERIALS.contains(player.getItemInHand().getType()) && isUser(player)) {
            event.setDamage(event.getDamage() + 1.25);
        }
    }

}
