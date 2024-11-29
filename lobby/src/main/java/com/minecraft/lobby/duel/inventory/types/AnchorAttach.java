package com.minecraft.lobby.duel.inventory.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@Getter
@RequiredArgsConstructor
public enum AnchorAttach {

    DIAMOND_SWORD(new int[] {0}, Material.DIAMOND_SWORD, 1, Enchantment.DAMAGE_ALL),
    IRON_CHESTPLATE(new int[] {1, 10, 19, 28, 31}, Material.IRON_CHESTPLATE, 1, null),
    IRON_LEGGINGS(new int[] {8, 11, 20, 29, 32}, Material.IRON_LEGGINGS, 1, null),
    IRON_HELMET(new int[] {9, 18, 27}, Material.IRON_HELMET, 1, null),
    IRON_BOOTS(new int[] {12, 21, 30}, Material.IRON_BOOTS, 1, null),
    BOWL(new int[] {13, 22, 34}, Material.BOWL, 64, null),
    COCOA(new int[] {14, 15, 16, 17, 23, 24, 25, 26, 35}, Material.INK_SACK, 64, null),
    IRON_INGOT(new int[] {33}, Material.IRON_INGOT, 30, null),
    MUSHROOM_SOUP(new int[] {2, 3, 4, 5, 6, 7}, Material.MUSHROOM_SOUP, 1, null);

    private final int[] allocations;
    private final Material material;
    private final int quantity;
    private final Enchantment enchantment;
}
