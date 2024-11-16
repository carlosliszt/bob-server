/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashSet;
import java.util.Set;

public class Lumberjack extends Kit {

    public Lumberjack(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WOOD_AXE));
        setItems(new ItemFactory(Material.WOOD_AXE).setName("§aMachado de lenhador").setDescription("§7Kit Lumberjack").getStack());
        setPrice(25000);
        setKitCategory(KitCategory.STRATEGY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!ACCEPTABLES_MATERIALS.contains(event.getBlock().getType()))
            return;

        if (event.getBlock().hasMetadata("kit.lumberjack.ignore"))
            return;

        Player player = event.getPlayer();

        if (isUser(player) && isItem(player.getItemInHand())) {

            BlockPosition blockPosition = new BlockPosition(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

            getConnectedBlocks(new HashSet<>(), event.getBlock()).forEach(block -> {

                World world = ((CraftWorld) block.getWorld()).getHandle();
                net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(block);

                nmsBlock.dropNaturally(world, blockPosition, nmsBlock.fromLegacyData(block.getData()), 1.0F, 0);
                block.setType(Material.AIR);
            });

            player.updateInventory();
        }
    }

    private final ImmutableSet<Material> ACCEPTABLES_MATERIALS = Sets.immutableEnumSet(Material.LOG, Material.LOG_2);

    @Variable(name = "hg.kit.lumberjack.max_blocks")
    private int maxBlocks = 30;

    private Set<Block> getConnectedBlocks(HashSet<Block> blocks, Block start) {

        if (blocks.size() >= maxBlocks)
            return blocks;

        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    Block block = start.getLocation().clone().add(x, y, z).getBlock();
                    if (block != null && !blocks.contains(block) && ACCEPTABLES_MATERIALS.contains(block.getType())) {
                        if (blocks.size() < maxBlocks) {
                            if (!block.hasMetadata("kit.lumberjack.ignore")) {
                                blocks.add(block);
                                blocks.addAll(getConnectedBlocks(blocks, block));
                            }
                        }
                    }
                }
            }
        }
        return blocks;
    }
}
