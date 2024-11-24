/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.datas;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LogData {

    private final UUID uniqueId;
    private final String nickname, server, content;
    private final Type type;
    private final LocalDateTime createdAt;

    public enum Type {
        COMMAND, CHAT, WARN;
    }

}