/*
 * Copyright (C) BlazeMC, All Rights Reserved
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

public class Viper extends Kit {

    public Viper(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SPIDER_EYE));
        setKitCategory(KitCategory.COMBAT);
        setPrice(35000);
        setPermission(Rank.MEMBER);
    }

    private final Random random = Constants.RANDOM;

    @Variable(name = "hg.kit.viper.apply_chance", permission = Rank.ADMINISTRATOR)
    public int chance = 20;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {

            Player attacker = (Player) event.getDamager();

            if (isUser(attacker) && !isUser(getUser(event.getEntity().getUniqueId()))) {

                if (random.nextInt(100) <= chance)
                    ((Player) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1), true);
            }
        }
    }
}
