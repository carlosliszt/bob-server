/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Snail extends Kit {

    public Snail(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SOUL_SAND));
        setKitCategory(KitCategory.COMBAT);
        setPrice(35000);
    }

    private final Random random = Constants.RANDOM;

    @Variable(name = "hg.kit.snail.apply_chance", permission = Rank.ADMINISTRATOR)
    private int chance = 33;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {

            Player attacker = (Player) event.getDamager();

            if (isUser(attacker)) {

                if (random.nextInt(100) <= chance)
                    ((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 0), true);
            }
        }
    }


}
