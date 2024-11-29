package com.minecraft.lobby.duel.inventory.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

@Getter
@RequiredArgsConstructor
public enum SoupAttach {

    DIAMOND_SWORD(new int[] {0}, Material.DIAMOND_SWORD, 1, Enchantment.DAMAGE_ALL),
    MUSHROOM_SOUP(new int[] {1,2,3,4,5,6,7,8}, Material.MUSHROOM_SOUP, 1, null);

    private final int[] allocations;
    private final Material material;
    private final int quantity;
    private final Enchantment enchantment;
}