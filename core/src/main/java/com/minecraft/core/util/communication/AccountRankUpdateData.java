/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.communication;

import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountRankUpdateData {

    private final UUID uniqueId;
    private final Rank rank;
    private final long addedAt, updatedAt, expiration;
    private final String author;
    private final Action action;

    public enum Action {

        ADD, REMOVE, REPLACE

    }

}
