/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Rank {

    BOB(100, "Bob", "Bob", "bob", Category.HEADSHIP, Tag.ROSA),
    DEVELOPER_ADMIN(20, "Developer Admin", "Developer", "6qx2d", Category.HEADSHIP, Tag.ADMINISTRATOR),
    OWNER_ADMIN(19, "Owner Admin", "Owner", "dms0l", Category.HEADSHIP, Tag.ADMINISTRATOR),
    ADMINISTRATOR(18, "Administrator", "Admin", "erv58", Category.ADMINISTRATION, Tag.ADMINISTRATOR),
    ASSISTANT_MOD(17, "Assistant Moderator+", "AssistantMod+", "dy3e7", Category.ASSISTANTS, Tag.PRIMARY_MOD),
    PRIMARY_MOD(16, "Moderator+", "Mod+", "qpyem", Category.ASSISTANTS, Tag.PRIMARY_MOD),
    EVENT_MOD(15, "Event Moderator", "EventMod", "i4hnd", Category.MODERATION, Tag.SECONDARY_MOD),
    SECONDARY_MOD(14, "Moderator", "Mod", "if76n", Category.MODERATION, Tag.SECONDARY_MOD),
    TRIAL_MODERATOR(13, "Trial", "Trial", "3fmfl", Category.MODERATION, Tag.TRIAL_MODERATOR),
    PARTNER_PLUS(12, "Partner+", "Partner+", "my2ec", Category.PARTNER, Tag.PARTNER_PLUS),
    HELPER(11, "Helper", "Helper", "hlp21", Tag.HELPER),
    BUILDER(10, "Builder", "Builder", "y3j9w", Tag.BUILDER),
    PARTNER(9, "Partner", "Partner", "23gmo", Tag.PARTNER),
    YOUTUBER(8, "Youtuber", "YT", "48ggf", Tag.YOUTUBER),
    STREAMER(7, "Streamer", "Streamer", "g5lbl", Tag.STREAMER),
    BETA(6, "Beta", "Beta", "0bxjm", Tag.BETA),
    BLAZE_PLUS(5, "Blaze+", "Blaze+", "hf67h", Tag.BLAZE_PLUS),
    BLAZE(4, "Blaze", "Blaze", "y4oaa", Tag.BLAZE),
    MVP(3, "MVP", "MVP", "y4o5a", Tag.MVP),
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

    public static List<Rank> getRanksByCategory(Category category) {
        return Arrays.stream(getValues()).filter(rank -> rank.getCategory() == category).collect(Collectors.toList());
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