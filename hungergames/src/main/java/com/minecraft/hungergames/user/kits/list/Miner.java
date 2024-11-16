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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class Miner extends Kit {

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(Material.COAL_ORE, Material.IRON_ORE);

    public Miner(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.STONE_PICKAXE, true));
        setItems(new ItemFactory(Material.STONE_PICKAXE).setName("§aMinerar").setDescription("§7Kit Miner").addEnchantment(new Enchantment[]{Enchantment.DIG_SPEED}, new int[]{1}).getStack());
        setKitCategory(KitCategory.STRATEGY);
        setPrice(35000);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!ACCEPTABLE_MATERIALS.contains(event.getBlock().getType()))
            return;

        Player player = event.getPlayer();

        if (!isItem(player.getItemInHand(), Material.STONE_PICKAXE))
            return;

        event.setCancelled(true);
        event.setExpToDrop(0);

        getConnectedBlocks(new HashSet<>(), event.getBlock()).forEach(block -> {

            Material material = block.getType() == Material.COAL_ORE ? Material.COAL : Material.IRON_ORE;

            giveItem(player, new ItemStack(material), player.getLocation());
            block.setType(Material.AIR);
        });
    }

    @Variable(name = "hg.kit.miner.max_blocks")
    private int maxBlocks = 30;

    private Set<Block> getConnectedBlocks(HashSet<Block> blocks, Block start) {

        if (blocks.size() >= maxBlocks)
            return blocks;

        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    Block block = start.getLocation().clone().add(x, y, z).getBlock();
                    if (block != null && !blocks.contains(block) && ACCEPTABLE_MATERIALS.contains(block.getType())) {
                        if (blocks.size() < maxBlocks) {
                            blocks.add(block);
                            blocks.addAll(getConnectedBlocks(blocks, block));
                        }
                    }
                }
            }
        }
        return blocks;
    }

}
