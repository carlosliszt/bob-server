/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class Flash extends Kit {

    public Flash() {
        setIcon(new ItemStack(Material.REDSTONE_TORCH_ON));
        setCategory(KitCategory.STRATEGY);
        setPrice(10000);
        setDefaultRank(Rank.MEMBER);
        setItems(new ItemFactory(Material.REDSTONE_TORCH_ON).setName("§aTeleport").setDescription("§7Kit Flash").getStack());
    }

    @Override
    public void resetAttributes(User user) {

    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && ACCEPTABLES_INTERACT.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();

            if (event.getAction() == Action.RIGHT_CLICK_AIR) {

                Player player = event.getPlayer();

                if (isCooldown(player)) {
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

                addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 15);
                player.setFallDistance(0);
                player.teleport(location);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 0), true);
            }
        }
    }
}
