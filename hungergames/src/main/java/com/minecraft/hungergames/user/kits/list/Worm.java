/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Worm extends Kit {

    public Worm(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.DIRT));
        setPrice(20000);
        setKitCategory(KitCategory.STRATEGY);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getBlock().getType() == Material.DIRT) {

            Player player = event.getPlayer();

            if (isUser(player)) {
                event.setInstaBreak(true);

                if (player.getHealth() < player.getMaxHealth() && Constants.RANDOM.nextInt(7) <= 3)
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 1));

                if (player.getFoodLevel() < 20)
                    player.setFoodLevel(player.getFoodLevel() + 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        if (!isPlayer(event.getEntity()))
            return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        Player player = (Player) event.getEntity();

        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT) {
            if (isUser(player)) {
                event.setDamage((player.getHealth() - event.getDamage() < 1 ? 0 : 4));
            }
        }
    }
}
