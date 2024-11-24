/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.pattern;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Condition {

    LOADING("Carregando"), DEAD("Morto"), ALIVE("Vivo"), SPECTATOR("Espectador");

    private final String fancyName;

    @Override
    public String toString() {
        return fancyName;
    }
}
