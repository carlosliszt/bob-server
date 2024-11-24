/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.punish;

import com.minecraft.core.database.enums.Columns;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum PunishType {

    BAN("Ban", 1, Columns.STAFF_LIFETIME_BANS, Columns.STAFF_MONTHLY_BANS, Columns.STAFF_WEEKLY_BANS),
    EVENT("Event", 2, Columns.STAFF_LIFETIME_EVENTS, Columns.STAFF_MONTHLY_EVENTS, Columns.STAFF_WEEKLY_EVENTS),
    MUTE("Mute", 3, Columns.STAFF_LIFETIME_MUTES, Columns.STAFF_MONTHLY_MUTES, Columns.STAFF_WEEKLY_MUTES),
    STREAM("Stream", 4);

    private final String name;
    private final int id;
    private final List<Columns> columns = new ArrayList<>();

    PunishType(String name, int id, Columns... columns) {
        this.name = name;
        this.id = id;
        this.columns.addAll(Arrays.asList(columns));
    }

    public static PunishType fromString(String value) {
        return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

    @Getter
    private static final PunishType[] values;

    static {
        values = values();
    }

}