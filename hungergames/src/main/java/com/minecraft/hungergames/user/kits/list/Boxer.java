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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Boxer extends Kit {

    public Boxer(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.STONE_SWORD));
        setPrice(25000);
        setKitCategory(KitCategory.COMBAT);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            if (isUser((Player) event.getEntity())) {
                event.setDamage(event.getDamage() - 0.5);
            }
        }
    }
}
