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

    ROSA(30, "0", "§d", false, "r0s4", "Rosa", "Pink"),
    ADMINISTRATOR(25, "A", "§4", false, "IzPLp", "Admin", "administrator"),
    PRIMARY_MOD(24, "B", "§5", false, "CYrov", "Mod+", "moderator+"),
    SECONDARY_MOD(23, "C", "§5", false, "dyOYO", "Mod", "moderator"),
    TRIAL_MODERATOR(22, "D", "§5", false, "XGyAp", "Trial", "trialmoderator", "trialmod"),
    YOUTUBER_PLUS(21, "E", "§3", false, "vAjST", "YT+", "youtuberplus", "youtuber+", "youtuber+"),
    STREAMER_PLUS(20, "F", "§3", false, "Ofjaf", "Streamer+", "streamerplus", "stream+"),
    HELPER(19, "G", "§9", false, "b3761", "Helper", "Ajudante"),
    BUILDER(18, "H", "§3", false, "VvNPg", "Builder", "constructor"),
    DESTAQUE(17, "I", "§6", true, "dstqe", "Destaque"),
    YOUTUBER(16, "J", "§b", false, "lMFIR", "YT", "youtuber"),
    STREAMER(15, "K", "§b", false, "OMFaf", "Stream", "Streamer'"),
    PARTNER(14, "L", "§b", false, "OBFGf", "Partner"),
    SHORTS(13, "M", "§b", false, "dn873", "Shorts", "Short"),
    CHAMPION(12, "N", "§6", true, "c7x3b", "Champion", "Vencedor"),
    ELITE(11, "O", "§c", false, "m8AnE", "Elite"),
    BETA(10, "P", "§1", false, "DxmFd", "Beta"),
    BOB(9, "Q", "§d", true, "jSBMB", "Bob"),
    CXC(8, "R", "§6", true, "3b28a", "CxC"),
    YEAR_2021(7, "S", "§b", true, "21g47", "2021"),
    NATAL(6, "T", "§c", true, "xma21", "Natal", "Christmas"),
    HALLOWEEN(5, "U", "§5", true, "hlw21", "Halloween"),
    PRO(4, "V", "§6", false, "QHGIn", "Pro"),
    VIP(3, "W", "§a", false, "yDTiT", "VIP"),
    BOOST(2, "X", "§d", true, "qVFqz", "Boost", "nitro", "nitrobooster", "booster"),
    TWITCH(1, "Y", "§5", true, "ytw22", "Twitch", "Subscriber"),
    MEMBER(0, "Z", "§7", false, "EalNl", "Membro", "member", "normal", "default", "none", "null");

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
        if (this == STREAMER_PLUS || this == PRIMARY_MOD || this == ROSA)
            return color + "§o";
        return color;
    }
}