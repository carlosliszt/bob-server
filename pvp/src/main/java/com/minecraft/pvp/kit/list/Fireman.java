/*
 * Copyright (C) BlazeMC, All Rights Reserved
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class Fireman extends Kit {

    public Fireman() {
        setIcon(new ItemStack(Material.WATER_BUCKET));
        setCategory(KitCategory.STRATEGY);
        setPrice(10000);
    }

    private final ImmutableSet<EntityDamageEvent.DamageCause> CANCEL_DAMAGES = Sets.immutableEnumSet(EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.LAVA);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (isUser((Player) event.getEntity()) && CANCEL_DAMAGES.contains(event.getCause()))
                event.setCancelled(true);
        }
    }

    @Override
    public void resetAttributes(User user) {

    }

}