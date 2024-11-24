/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class Demoman extends Kit {

    public Demoman(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.GRAVEL));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.GRAVEL).setName("§aMinas explosivas").setDescription("§7Kit Demoman").setAmount(8).getStack());
        setPrice(25000);
    }

    private static final String TAG = "hg.kit.demoman.explosive";

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (isUser(player.getPlayer()) && block.getType() == Material.GRAVEL && isItem(player.getItemInHand())) {

            if (checkInvincibility(player)) {
                event.setCancelled(true);
                return;
            }

            if (block.hasMetadata(TAG))
                block.removeMetadata(TAG, getPlugin());

            block.setMetadata(TAG, new GameMetadata(player.getUniqueId()));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Block block = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (block.hasMetadata(TAG)) {

            FixedMetadataValue metadataValue = (FixedMetadataValue) block.getMetadata(TAG).get(0);
            UUID owner = (UUID) metadataValue.value();

            User user = getUser(event.getPlayer().getUniqueId());

            if (!user.isAlive())
                return;

            if (owner == event.getPlayer().getUniqueId())
                return;

            if (getKit("Tank").isUser(event.getPlayer())) {
                block.removeMetadata(TAG, getPlugin());
                block.getLocation().getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType().getId());
                block.setType(Material.AIR);
                return;
            }

            User attacker = getUser(owner);

            if (attacker != null && attacker.isAlive() && attacker.isOnline())
                user.getCombatTag().addTag(attacker, 3);

            block.removeMetadata(TAG, getPlugin());
            block.setType(Material.AIR);
            block.getWorld().createExplosion(block.getLocation().clone().add(0.5D, 0.5D, 0.5D), 4.3F);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().hasMetadata(TAG))
            event.getBlock().removeMetadata(TAG, getPlugin());
    }

}
