/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.fields;

import com.minecraft.core.enums.Rank;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Flag {

    NICK("nick", 0x1, Rank.ADMINISTRATOR),
    NICK_CHOOSE("nick_choose", 0x2, Rank.ADMINISTRATOR),
    SKIN("skin", 0x3, Rank.ADMINISTRATOR),
    SKIN_CHOOSE("skin_choose", 0x4, Rank.ADMINISTRATOR),
    TAG("tag", 0x5, Rank.ADMINISTRATOR),
    CLAN_TAG("clantag", 0x6, Rank.DEVELOPER_ADMIN),
    MEDAL("medal", 0x7, Rank.DEVELOPER_ADMIN),
    PUNISH("punish", 0x8, Rank.DEVELOPER_ADMIN),
    WORLD_EDIT("worldedit", 0x9, Rank.DEVELOPER_ADMIN),
    PERFORM_COMMANDS("commands", 0xa, Rank.DEVELOPER_ADMIN),
    PREFIXTYPE("prefixtype", 0xb, Rank.DEVELOPER_ADMIN),
    UNPUNISH("unpunish", 0xc, Rank.DEVELOPER_ADMIN);

    private final String name;
    private final int bitIndex;
    private final Rank rank;

    Flag(String name, int bitIndex, Rank rank) {
        this.name = name;
        this.bitIndex = bitIndex;
        this.rank = rank;
    }

    public static Flag from(String string) {
        return Arrays.stream(getValues()).filter(flag -> flag.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    @Getter
    private static final Flag[] values;

    static {
        values = values();
    }

}