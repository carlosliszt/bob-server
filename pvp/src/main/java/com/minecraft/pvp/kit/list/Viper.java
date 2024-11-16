/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.Constants;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Viper extends Kit {

    public Viper() {
        setIcon(new ItemStack(Material.SPIDER_EYE));
        setCategory(KitCategory.COMBAT);
        setPrice(15000);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {

            Player attacker = (Player) event.getDamager();

            if (isUser(attacker)) {

                if (Constants.RANDOM.nextInt(100) <= 33)
                    ((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 0), true);
            }
        }
    }

    @Override
    public void resetAttributes(User user) {

    }

}