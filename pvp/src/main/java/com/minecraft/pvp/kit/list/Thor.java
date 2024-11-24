/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class Thor extends Kit {

    public Thor() {
        setIcon(new ItemStack(Material.WOOD_AXE));
        setItems(new ItemFactory(Material.WOOD_AXE).setName("§aInvocar raio").setDescription("§7Kit Thor").getStack());
        setCategory(KitCategory.STRATEGY);
        setCooldown(7);
        setPrice(10000);
        setDefaultRank(Rank.VIP);
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if (!event.getAction().name().contains("RIGHT_CLICK"))
            return;

        if (event.hasItem() && isUser(event.getPlayer())) {

            if (!isItem(event.getPlayer()))
                return;

            Player player = event.getPlayer();

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            Block block = (event.getAction() == Action.RIGHT_CLICK_BLOCK ? event.getClickedBlock() : player.getTargetBlock((Set<Material>) null, 100));

            Location playerLocation = player.getLocation().clone();
            Location blockLocation = block.getLocation().clone();

            playerLocation.setY(0);
            blockLocation.setY(0);

            if (playerLocation.distanceSquared(blockLocation) > 36) {
                return;
            }

            addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 7);

            Location strikeLightningLocation = block.getWorld().getHighestBlockAt(block.getLocation()).getLocation();
            strikeLightningLocation.getWorld().strikeLightning(strikeLightningLocation);
        }
    }

}