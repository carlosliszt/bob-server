/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.pattern;

import com.minecraft.core.translation.Language;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;

import java.util.Arrays;

@AllArgsConstructor
public enum ItemType {

    AIR("seus punhos", "their hands"),
    WOOD_SWORD("uma Espada de Madeira", "a Wood Sword"),
    STONE_SWORD("uma Espada de Pedra", "a Stone Sword"),
    IRON_SWORD("uma Espada de Ferro", "an Iron Sword"),
    GOLD_SWORD("uma Espada de Ouro", "a Gold Sword"),
    DIAMOND_SWORD("uma Espada de Diamante", "a Diamond Sword"),

    WOOD_AXE("um Machado de Madeira", "a Wood Axe"),
    STONE_AXE("um Machado de Pedra", "a Stone Axe"),
    IRON_AXE("um Machado de Ferro", "an Iron Axe"),
    GOLD_AXE("um Machado de Ouro", "a Gold Axe"),
    DIAMOND_AXE("um Machado de Diamante", "a Diamond Axe"),

    WOOD_PICKAXE("uma Picareta de Madeira", "a Wood Pickaxe"),
    STONE_PICKAXE("uma Picareta de Pedra", "a Stone Pickaxe"),
    IRON_PICKAXE("uma Picareta de Ferro", "an Iron Pickaxe"),
    GOLD_PICKAXE("uma Picareta de Ouro", "a Gold Pickaxe"),
    DIAMOND_PICKAXE("uma Picareta de Diamante", "a Diamond Pickaxe"),

    WOOD_HOE("uma Enxada de Madeira", "a Wood Hoe"),
    STONE_HOE("uma Enxada de Pedra", "a Stone Hoe"),
    IRON_HOE("uma Enxada de Ferro", "an Iron Hoe"),
    GOLD_HOE("uma Enxada de Ouro", "a Gold Hoe"),
    DIAMOND_HOE("uma Enxada de Diamante", "a Diamond Hoe"),

    WOOD_SPADE("uma Pá de Madeira", "a Wood Shovel"),
    STONE_SPADE("uma Pá de Pedra", "a Stone Shovel"),
    IRON_SPADE("uma Pá de Ferro", "an Iron Shovel"),
    GOLD_SPADE("uma Pá de Ouro", "a Gold Shovel"),
    DIAMOND_SPADE("uma Pá de Diamante", "a Diamond Shovel"),

    BOWL("uma Tijela", "a Bowl"),
    MUSHROOM_SOUP("uma Sopa", "a Soup");

    private final String portuguese, english;

    public static String getString(Language language, Material material) {
        ItemType itemType = fromMaterial(material);

        if (itemType != null)
            return language == Language.PORTUGUESE ? itemType.portuguese : itemType.english;
        else
            return StringUtils.replace(StringUtils.capitalize(material.name().toLowerCase()), "_", " ");
    }

    private static ItemType fromMaterial(Material material) {
        return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(material.name())).findFirst().orElse(null);
    }

}
