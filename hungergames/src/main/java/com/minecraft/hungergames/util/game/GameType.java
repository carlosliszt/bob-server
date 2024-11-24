/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum GameType {

    DOUBLEKIT(2, "Double Kit"), SINGLEKIT(1, "Single Kit"),
    TRIPLEKIT(3, "Triple Kit"), QUADRUPLEKIT(4, "Quadruple Kit");

    private final int maxKits;
    private final String name;

    public static GameType fromString(String string) {
        return Arrays.stream(values()).filter(prefixType -> prefixType.name().equalsIgnoreCase(string)).findFirst().orElse(null);
    }
}
