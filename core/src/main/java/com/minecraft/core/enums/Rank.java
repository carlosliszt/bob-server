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
public enum Rank {

    CARLOS(100, "Carlos", "Carlos", "carlos", Category.HEADSHIP, Tag.ROSA),
    DEVELOPER_ADMIN(20, "Developer Admin", "Developer", "6qx2d", Category.HEADSHIP, Tag.ADMINISTRATOR),
    OWNER_ADMIN(19, "Owner Admin", "Owner", "dms0l", Category.HEADSHIP, Tag.ADMINISTRATOR),
    ADMINISTRATOR(18, "Administrator", "Admin", "erv58", Category.ADMINISTRATION, Tag.ADMINISTRATOR),
    ASSISTANT_MOD(17, "Assistant Moderator+", "AssistantMod+", "dy3e7", Category.ASSISTANTS, Tag.PRIMARY_MOD),
    PRIMARY_MOD(16, "Moderator+", "Mod+", "qpyem", Category.ASSISTANTS, Tag.PRIMARY_MOD),
    EVENT_MOD(15, "Event Moderator", "EventMod", "i4hnd", Category.MODERATION, Tag.SECONDARY_MOD),
    SECONDARY_MOD(14, "Moderator", "Mod", "if76n", Category.MODERATION, Tag.SECONDARY_MOD),
    TRIAL_MODERATOR(13, "Trial", "Trial", "3fmfl", Category.MODERATION, Tag.TRIAL_MODERATOR),
    YOUTUBER_PLUS(12, "Youtuber+", "YT+", "my2ec", Category.PARTNER, Tag.YOUTUBER_PLUS),
    STREAMER_PLUS(11, "Streamer+", "Streamer+", "3gxcg", Category.PARTNER, Tag.STREAMER_PLUS),
    HELPER(10, "Helper", "Helper", "hlp21", Tag.HELPER),
    BUILDER(9, "Builder", "Builder", "y3j9w", Tag.BUILDER),
    YOUTUBER(8, "Youtuber", "YT", "48ggf", Tag.YOUTUBER),
    STREAMER(7, "Streamer", "Streamer", "g5lbl", Tag.STREAMER),
    PARTNER(6, "Partner", "Partner", "23gmo", Tag.PARTNER),
    SHORTS(5, "Shorts", "Shorts", "3bd71", Tag.PARTNER),
    ELITE(4, "Elite", "Elite", "hf67h", Tag.ELITE),
    BETA(3, "Beta", "Beta", "0bxjm", Tag.BETA),
    PRO(2, "Pro", "Pro", "ye4o5", Tag.PRO),
    VIP(1, "VIP", "VIP", "mvvz3", Tag.VIP),
    MEMBER(0, "Membro", "Membro", "hwyr2", Tag.MEMBER);

    private final int id;
    private final String name, displayName, uniqueCode;
    private final Category category;
    private final Tag defaultTag;

    Rank(int id, String name, String displayName, String uniqueCode, Tag tag) {
        this(id, name, displayName, uniqueCode, Category.NONE, tag);
    }

    public boolean isStaffer() {
        return this.getId() >= HELPER.getId();
    }

    public static Rank fromId(int i) {
        return Arrays.stream(getValues()).filter(rank -> rank.getId() == i).findFirst().orElse(null);
    }

    public static Rank fromString(String name) {
        return Arrays.stream(getValues()).filter(rank -> rank.getDisplayName().equalsIgnoreCase(name) || rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Rank fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(rank -> rank.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    @Getter
    private static final Rank[] values;

    static {
        values = values();
    }

    @Getter
    @AllArgsConstructor
    public enum Category {

        NONE(0, "Jogadores"),
        PARTNER(1, "Partners"),
        MODERATION(2, "Moderação"),
        ASSISTANTS(3, "Auxiliares"),
        ADMINISTRATION(4, "Administração"),
        HEADSHIP(5, "Head Admin");

        private final int importance;
        private final String display;
    }

}