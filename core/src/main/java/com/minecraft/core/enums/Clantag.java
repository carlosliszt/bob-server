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

    DEFAULT("Default", "§7", "yQFBm", "GRAY", Rank.MEMBER, false),
    STAFF("Staff", "§5", "db37a", "DARK_PURPLE", Rank.SECONDARY_MOD, true),
    PARTNER_PLUS("Partner+", "§3", "hd73b", "DARK_AQUA", Rank.PARTNER_PLUS, true),
    CHAMPION("Campeão", "§6", "27adh", "GOLD", Rank.ADMINISTRATOR, true),
    RED("Admin", "§c", "d3aad", "RED", Rank.ADMINISTRATOR, true),
    ROSA("Rosa", "§d", "d3a7d", "LIGHT_PURPLE", Rank.ADMINISTRATOR, true),;

    private final String name, color, uniqueCode, chatColor;
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