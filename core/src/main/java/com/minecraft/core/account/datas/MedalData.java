/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.datas;

import com.minecraft.core.enums.Medal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MedalData {

    private final Medal medal;
    private final String addedBy;
    private long addedAt, expiration;

    public boolean hasExpired() {
        return !isPermanent() && expiration < System.currentTimeMillis();
    }

    public boolean isPermanent() {
        return expiration == -1;
    }

}