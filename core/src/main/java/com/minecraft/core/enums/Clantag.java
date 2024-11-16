/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Clantag {

    DEFAULT("Default", "§7", "yQFBm", Rank.MEMBER, false),
    BETA("Beta", "§1", "db37a", Rank.ADMINISTRATOR, true),
    VIP("VIP", "§a", "hd73b", Rank.ADMINISTRATOR, true),
    PRO("Pro", "§6", "27adh", Rank.ADMINISTRATOR, true);

    private final String name, color, uniqueCode;
    private final Rank rank;
    private final boolean dedicated;

    public static Clantag fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(clantag -> clantag.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public static Clantag fromName(String name) {
        return Arrays.stream(getValues()).filter(clantag -> clantag.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Clantag getOrElse(String code, Clantag m) {
        return Arrays.stream(getValues()).filter(clantag -> clantag.getUniqueCode().equals(code)).findFirst().orElse(m);
    }

    @Getter
    private static final Clantag[] values;

    static {
        values = values();
    }

}