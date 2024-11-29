package com.minecraft.lobby.duel.inventory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum FactoryInventory {

    MAIN("Menu do Duels: Editor de Inventário", 36),
    GLADIATOR("Editor: Gladiator", 54),
    SIMULATOR("Editor: Simulator", 54),
    SOUP("Editor: Sopa", 54),
    BRIDGE("Editor: The Bridge", 54);

    private final String title;
    private final int slots;
}
