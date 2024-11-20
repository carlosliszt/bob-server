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
    ADMINISTRATOR(22, "A", "§4", false, "IzPLp", "Admin", "administrator"),
    REPORTER(21, "B", "§c", true, "uwu12", "Reporter"),
    PRIMARY_MOD(20, "C", "§5", false, "CYrov", "Mod+", "moderator+"),
    SECONDARY_MOD(19, "D", "§5", false, "dyOYO", "Mod", "moderator"),
    TRIAL_MODERATOR(18, "E", "§5", false, "XGyAp", "Trial", "trialmoderator", "trialmod"),
    PARTNER_PLUS(17, "F", "§3", false, "vAjST", "Partner+", "Partnermais"),
    HELPER(16, "G", "§9", false, "b3761", "Helper", "Ajudante"),
    BUILDER(15, "H", "§3", false, "VvNPg", "Builder", "constructor"),
    PARTNER(14, "I", "§b", false, "OBFGf", "Partner"),
    YOUTUBER(13, "J", "§b", false, "lMFIR", "YT", "youtuber"),
    STREAMER(12, "K", "§b", false, "OMFaf", "Stream", "Streamer'"),
    CHAMPION(11, "L", "§6", true, "c7x3b", "Champion", "Vencedor"),
    BETA(10, "M", "§1", false, "DxmFd", "Beta"),
    ULTRA_PLUS(9, "N", "§d", false, "m8AnE", "Ultra+", "Ultramais"),
    CARNAVAL(8, "O", "§6", true, "carnv", "Carnaval"),
    NATAL(7, "P", "§c", true, "xma21", "Natal", "Christmas"),
    FERIAS(6, "Q", "§a", true, "vc021", "Férias", "Vacation"),
    ENDERLORE(5, "R", "§5", true, "hlw21", "Enderlore"),
    ULTRA(4, "S", "§d", false, "y7t2a", "Ultra"),
    PRO(3, "T", "§6", false, "QHGIn", "Pro"),
    MVP(2, "U", "§9", false, "ytw22", "MVP"),
    VIP(1, "V", "§a", false, "yDTiT", "VIP"),
    MEMBER(0, "W", "§7", false, "EalNl", "Membro", "member", "normal", "default", "none", "null");

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