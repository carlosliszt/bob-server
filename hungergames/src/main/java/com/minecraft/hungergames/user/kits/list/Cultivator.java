/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableMap;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.CocoaPlant;
import org.bukkit.scheduler.BukkitRunnable;

public class Cultivator extends Kit {

    private final ImmutableMap<Integer, TreeType> trees = ImmutableMap.<Integer, TreeType>builder().put(0, TreeType.TREE).put(1, TreeType.REDWOOD).put(2, TreeType.BIRCH).put(3, TreeType.SMALL_JUNGLE).put(4, TreeType.ACACIA).put(5, TreeType.DARK_OAK).build();

    public Cultivator(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SAPLING));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @Variable(name = "hg.kit.cultivator.cocoa_invincibility_grow", permission = Rank.ADMINISTRATOR)
    private boolean cocoaInvincibilityGrow = false;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (!isUser(player))
            return;

        final Block block = event.getBlock();

        if (block.getType() == Material.SAPLING) {
            TreeType treeType = trees.get(((int) block.getData()));
            block.setType(Material.AIR);
            block.getWorld().generateTree(block.getLocation(), treeType);
        } else if (block.getType() == Material.CROPS) {
            block.setData((byte) 7);
        } else if (block.getType() == Material.POTATO) {
            block.setData((byte) 7);
        } else if (block.getType() == Material.CARROT) {
            block.setData((byte) 7);
        } else if (block.getType() == Material.COCOA) {

            if (!cocoaInvincibilityGrow && getGame().getStage() != GameStage.PLAYING)
                return;

            final BlockFace face = ((CocoaPlant) block.getState().getData()).getFacing();
            final BlockState state = block.getState();

            new BukkitRunnable() {
                int i = 0;

                public void run() {
                    if (block.getType() != Material.COCOA) {
                        cancel();
                        return;
                    }

                    if (i == 0) {
                        state.setData(new CocoaPlant(CocoaPlant.CocoaPlantSize.MEDIUM, face));
                        state.update();
                    } else if (i == 1) {
                        state.setData(new CocoaPlant(CocoaPlant.CocoaPlantSize.LARGE, face));
                        state.update();
                        cancel();
                        return;
                    }

                    i++;
                }
            }.runTaskTimer(getPlugin(), 20, 20);
        }
    }
}
