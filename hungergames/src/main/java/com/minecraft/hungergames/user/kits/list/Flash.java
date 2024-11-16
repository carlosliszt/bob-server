/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class Flash extends Kit {

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    public Flash(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.REDSTONE_TORCH_ON));
        setItems(new ItemFactory(Material.REDSTONE_TORCH_ON).setName("§aTeleport").setDescription("§7Kit Flash").getStack());
        setCooldown(28);
        setKitCategory(KitCategory.MOVEMENT);
        setPrice(25000);
        setCombatCooldown(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && ACCEPTABLES_INTERACT.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();

            if (event.getAction() == Action.RIGHT_CLICK_AIR) {

                Player player = event.getPlayer();

                if (isCooldown(player) || isCombat(player)) {
                    dispatchCooldown(player);
                    return;
                }

                Block b = player.getTargetBlock((Set<Material>) null, 130);

                if (b == null || b.getType() == Material.AIR) {
                    return;
                }

                Location location = b.getLocation().clone().add(0, 1.8, 0);

                location.setYaw(player.getLocation().getYaw());
                location.setPitch(player.getLocation().getPitch());

                addCooldown(player.getUniqueId());

                player.setFallDistance(0);
                player.teleport(location);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 0), true);

                location.getWorld().strikeLightning(location);
            }
        }
    }

    @Override
    public double getCombatTime() {
        return 1.8;
    }

}
