/*
 * Copyright (C) BlazeMC, All Rights Reserved
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class Fireman extends Kit {

    public Fireman(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WATER_BUCKET));
        setItems(new ItemStack(Material.WATER_BUCKET));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    private final ImmutableSet<EntityDamageEvent.DamageCause> CANCEL_DAMAGES = Sets.immutableEnumSet(EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.LAVA);

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (isPlayer(event.getEntity())) {
            if (isUser((Player) event.getEntity()) && CANCEL_DAMAGES.contains(event.getCause()))
                event.setCancelled(true);
        }
    }
}
