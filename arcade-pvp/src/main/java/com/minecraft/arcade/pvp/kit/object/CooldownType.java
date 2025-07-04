/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.kit.object;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CooldownType {

    COMBAT("Combate: ", true), DEFAULT("Kit ", true);

    private final String word;
    private final boolean display;
}