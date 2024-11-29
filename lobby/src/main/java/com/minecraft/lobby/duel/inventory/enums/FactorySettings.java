package com.minecraft.lobby.duel.inventory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum FactorySettings {

    MAIN("Menu do Duels", 36),
    KILL_EFFECT("Menu do Duels: Efeitos de Kills", 54),
    PREFERENCES("Menu do Duels: Preferências", 54);

    private final String title;
    private final int slots;
}
