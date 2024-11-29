package com.minecraft.lobby.duel.inventory.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@Getter
@RequiredArgsConstructor
public enum FastAttach {

    DIAMOND_SWORD(new int[] {0}, Material.DIAMOND_SWORD, 1, Enchantment.DAMAGE_ALL),
    WATER_BUCKET(new int[] {1}, Material.WATER_BUCKET, 1, null),
    LAVA_BUCKET(new int[] {2, 27}, Material.LAVA_BUCKET, 1, null),
    WOOD(new int[] {3}, Material.WOOD, 64, null),
    COBBLE_WALL(new int[] {8}, Material.COBBLE_WALL, 64, null),
    STONE_AXE(new int[] {4}, Material.STONE_AXE, 1, null),
    STONE_PICKAXE(new int[] {5}, Material.STONE_PICKAXE, 1, null);

    private final int[] allocations;
    private final Material material;
    private final int quantity;
    private final Enchantment enchantment;
}