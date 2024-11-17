package com.minecraft.core.cosmetic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TitleCategory {

    NONE("Nenhum", "BARRIER", "§aNenhum",  false),
    GENERAL("Geral", "PAPER", "§aGeral",  false),
    DUELS("Duels", "DIAMOND_SWORD", "§aDuels",  false),
    PVP("PvP", "IRON_CHESTPLATE", "§aPvP",  false),
    HG("HG", "MUSHROOM_SOUP", "§aHG",  false),
    SPECIAL("Especial", "BOOK", "§aEspecial",  true),;

    private String name, material, displayName;
    private boolean exclusive;

}
