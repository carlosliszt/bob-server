/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.object;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AwaySession {

    private int remainingTime;
    private final boolean locked;

    public boolean expired() {
        return remainingTime <= 0;
    }

    public void invalidate() {
        this.remainingTime = Integer.MAX_VALUE;
    }

    public void decrease() {
        this.remainingTime--;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
