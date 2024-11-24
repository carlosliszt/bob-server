/*
 * Copyright (C) BlazeMC, All Rights Reserved
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
import org.bukkit.event.entity.EntityDamageEvent;

public class Turtle extends Kit {

    public Turtle(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.DIAMOND_CHESTPLATE));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageEvent event) {
        if (isPlayer(event.getEntity())) {
            Player player = (Player) event.getEntity();

            if (player.isSneaking() && isUser(player))
                event.setDamage(Math.min(event.getDamage(), 2));
        }
    }
}
