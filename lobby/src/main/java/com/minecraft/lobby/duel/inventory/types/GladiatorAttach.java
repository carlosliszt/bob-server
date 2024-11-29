package com.minecraft.lobby.duel.inventory.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@Getter
@RequiredArgsConstructor
public enum GladiatorAttach {

    DIAMOND_SWORD(new int[] {0}, Material.DIAMOND_SWORD, 1, Enchantment.DAMAGE_ALL),
    WATER_BUCKET(new int[] {1}, Material.WATER_BUCKET, 1, null),
    LAVA_BUCKET(new int[] {2, 27, 28}, Material.LAVA_BUCKET, 1, null),
    WOOD(new int[] {3}, Material.WOOD, 64, null),
    COBBLE_WALL(new int[] {8}, Material.COBBLE_WALL, 64, null),
    IRON_HELMET(new int[] {9, 18}, Material.IRON_HELMET, 1, null),
    IRON_CHESTPLATE(new int[] {10, 19}, Material.IRON_CHESTPLATE, 1, null),
    IRON_LEGGINGS(new int[] {11, 20}, Material.IRON_LEGGINGS, 1, null),
    IRON_BOOTS(new int[] {12, 21}, Material.IRON_BOOTS, 1, null),
    BOWL(new int[] {13, 22}, Material.BOWL, 64, null),
    COCOA(new int[] {14, 15, 16, 23, 24, 25}, Material.INK_SACK, 64, null),
    STONE_AXE(new int[] {17}, Material.STONE_AXE, 1, null),
    STONE_PICKAXE(new int[] {26}, Material.STONE_PICKAXE, 1, null),
    MUSHROOM_SOUP(new int[] {4, 5, 6, 7, 29, 30, 31, 32, 33, 34, 35}, Material.MUSHROOM_SOUP, 1, null);

    private final int[] allocations;
    private final Material material;
    private final int quantity;
    private final Enchantment enchantment;
}