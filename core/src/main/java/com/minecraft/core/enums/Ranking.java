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
public enum Ranking {

    BRONZE_I(1, "I", "Bronze I", "§7", "✸", "ZZZZ", 0),
    BRONZE_II(2, "II", "Bronze II", "§7", "✸", "ZZZ", 700),
    BRONZE_III(3, "III", "Bronze III", "§7", "✸", "ZZ", 1450),
    BRONZE_IV(4, "IV", "Bronze IV", "§7", "✸", "Z", 2200),

    SILVER_I(5, "I", "Silver I", "§8", "✽", "Y", 3000),
    SILVER_II(6, "II", "Silver II", "§8", "✽", "X", 3850),
    SILVER_III(7, "III", "Silver III", "§8", "✽", "W", 4700),
    SILVER_IV(8, "IV", "Silver IV", "§8", "✽", "V", 5550),

    GOLD_I(9, "I", "Gold I", "§6", "✹", "U", 6500),
    GOLD_II(10, "II", "Gold II", "§6", "✹", "T", 7450),
    GOLD_III(11, "III", "Gold III", "§6", "✹", "S", 8400),
    GOLD_IV(12, "IV", "Gold IV", "§6", "✹", "R", 9350),

    PLATINUM_I(13, "I", "Platinum I", "§5", "❃", "Q", 10000),
    PLATINUM_II(14, "II", "Platinum II", "§5", "❃", "P", 10750),
    PLATINUM_III(15, "III", "Platinum III", "§5", "❃", "O", 11600),
    PLATINUM_IV(16, "IV", "Platinum IV", "§5", "❃", "N", 12550),

    EMERALD_I(17, "I", "Emerald I", "§a", "✯", "M", 13300),
    EMERALD_II(18, "II", "Emerald II", "§a", "✯", "L", 14050),
    EMERALD_III(19, "III", "Emerald III", "§a", "✯", "K", 14800),
    EMERALD_IV(20, "IV", "Emerald IV", "§a", "✯", "J", 15550),

    DIAMOND_I(21, "I", "Diamond I", "§b", "✵", "I", 16500),
    DIAMOND_II(22, "II", "Diamond II", "§b", "✵", "H", 17450),
    DIAMOND_III(23, "III", "Diamond III", "§b", "✵", "G", 18400),
    DIAMOND_IV(24, "IV", "Diamond IV", "§b", "✵", "F", 19350),

    MASTER_I(25, "I", "Master I", "§c", "❁", "E", 20600),
    MASTER_II(26, "II", "Master II", "§c", "❁", "D", 21850),
    MASTER_III(27, "III", "Master III", "§c", "❁", "C", 23100),
    MASTER_IV(28, "IV", "Master IV", "§c", "❁", "B", 25000),

    CHALLENGER(29, "", "Challenger", "§4", "❂", "A", Integer.MAX_VALUE);

    private final int id;
    private final String display;
    private final String name;
    private final String color, symbol;
    private final String order;
    private final int experience;

    @Getter
    private static final Ranking[] values;

    static {
        values = values();
    }

    public Ranking getPreviousRanking() {
        return this == BRONZE_I ? BRONZE_I : getValues()[ordinal() - 1];
    }

    public Ranking getNextRanking() {
        return this == CHALLENGER ? CHALLENGER : getValues()[ordinal() + 1];
    }

    public static Ranking fromId(int id) {
        return Arrays.stream(getValues()).filter(ranking -> ranking.getId() == id).findFirst().orElse(null);
    }

}