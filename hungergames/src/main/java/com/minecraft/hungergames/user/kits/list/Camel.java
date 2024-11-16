/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Camel extends Kit {

    public Camel(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SAND));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @EventHandler
    public void onServerHeartbeat(ServerHeartbeatEvent event) {

        if (!event.isPeriodic(20))
            return;

        getPlugin().getUserStorage().getUsers().forEach(this::apply);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {

        Player player = event.getPlayer();

        if (event.getBlock().getType() == Material.CACTUS) {
            if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
                if (isUser(player))
                    Bukkit.getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(), player));
            }
        }
    }

    public void apply(User user) {
        if (!user.isAlive() || !isUser(user))
            return;

        Player player = user.getPlayer();

        if (player.getLocation().getBlock().getBiome() == Biome.DESERT) {

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 118, 0), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 118, 1), true);
        }
    }
}
