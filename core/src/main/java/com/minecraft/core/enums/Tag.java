/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Tag {

    ROSA(100, "0", "§d", false, "r0s4", "Rosa", "Pink"),
    ADMINISTRATOR(25, "1", "§4", false, "IzPLp", "Admin", "administrator"),
    REPORTER(24, "A", "§c", true, "uwu12", "Reporter"),
    PRIMARY_MOD(23, "B", "§5", false, "CYrov", "Mod+", "moderator+"),
    SECONDARY_MOD(22, "C", "§5", false, "dyOYO", "Mod", "moderator"),
    TRIAL_MODERATOR(21, "D", "§5", false, "XGyAp", "Trial", "trialmoderator", "trialmod"),
    PARTNER_PLUS(20, "E", "§3", false, "vAjST", "Partner+", "Partnermais"),
    HELPER(19, "F", "§9", false, "b3761", "Helper", "Ajudante"),
    BUILDER(18, "G", "§3", false, "VvNPg", "Builder", "constructor"),
    PARTNER(17, "H", "§b", false, "OBFGf", "Partner"),
    YOUTUBER(16, "I", "§b", false, "lMFIR", "YT", "youtuber"),
    STREAMER(15, "J", "§b", false, "OMFaf", "Stream", "Streamer'"),
    CHAMPION(14, "K", "§6", true, "c7x3b", "Champion", "Vencedor"),
    BETA(13, "L", "§1", false, "DxmFd", "Beta"),
    ULTRA_PLUS_3(12, "M", "§6", true, "m8An3", "Ultra+3", "Ultramais3"),
    ULTRA_PLUS_2(11, "N", "§b", true, "m8An2", "Ultra+2", "Ultramais2"),
    ULTRA_PLUS_1(10, "O", "§5", true, "m8An1", "Ultra+1", "Ultramais1"),
    ULTRA_PLUS(9, "P", "§d", false, "m8AnE", "Ultra+", "Ultramais"),
    CARNAVAL(8, "Q", "§6", true, "carnv", "Carnaval"),
    NATAL(7, "R", "§c", true, "xma21", "Natal", "Christmas"),
    FERIAS(6, "S", "§a", true, "vc021", "Férias", "Vacation"),
    ENDERLORE(5, "T", "§5", true, "hlw21", "Enderlore"),
    ULTRA(4, "U", "§d", false, "y7t2a", "Ultra"),
    PRO(3, "V", "§6", false, "QHGIn", "Pro"),
    MVP(2, "W", "§9", false, "ytw22", "MVP"),
    VIP(1, "X", "§a", false, "yDTiT", "VIP"),
    MEMBER(0, "Y", "§7", false, "EalNl", "Membro", "member", "normal", "default", "none", "null");

    private final int id;
    private final String order;
    private final String color;
    private final boolean dedicated;
    private final String uniqueCode;
    private final String[] usages;

    Tag(int id, String order, String color, boolean dedicated, String uniqueCode, String... usages) {
        this.id = id;
        this.order = order;
        this.color = color;
        this.dedicated = dedicated;
        this.uniqueCode = uniqueCode;
        this.usages = usages;
    }

    @Getter
    private static final Tag[] values;

    static {
        values = values();
    }

    public Rank getDefaultRank() {
        if(this == ROSA) {
            return Rank.BOB;
        } else {
            return Rank.valueOf(this.name());
        }
    }

    public static Tag fromString(String name) {
        return Arrays.stream(getValues()).filter(tag -> tag.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isBetween(Tag tag1, Tag tag2) {
        return this.getId() < tag1.getId() && this.getId() > tag2.getId();
    }

    public static Tag fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(tag -> tag.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public static Tag getOrElse(String code, Tag t) {
        return Arrays.stream(getValues()).filter(tag -> tag.getUniqueCode().equals(code)).findFirst().orElse(t);
    }

    public String getName() {
        return this.usages[0];
    }

    public static Tag fromUsages(String text) {
        for (Tag tag : getValues()) {
            for (String u : tag.getUsages()) {
                if (u.equalsIgnoreCase(text))
                    return tag;
            }
        }
        return null;
    }

    public String getMemberSetting(PrefixType prefixType) {
        return (prefixType == PrefixType.DEFAULT_WHITE ? "§f" : "§7");
    }

    public String getFormattedColor() {
        if (this == PARTNER_PLUS || this == PRIMARY_MOD || this == ROSA || this == SECONDARY_MOD || this == ADMINISTRATOR || this == HELPER || this == PARTNER || this == TRIAL_MODERATOR)
            return color + "§o";

        return color;
    }
}