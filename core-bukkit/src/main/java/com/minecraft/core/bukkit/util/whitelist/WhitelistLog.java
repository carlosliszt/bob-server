/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.whitelist;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WhitelistLog {

    private final UUID uniqueId;
    private final String content;
    private final Action action;
    private final LocalDateTime createdAt;

    public enum Action {
        ON, OFF, ADD, REMOVE, IMPORT, CLEAR;
    }

}