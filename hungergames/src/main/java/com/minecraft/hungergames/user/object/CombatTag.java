/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.object;

import com.minecraft.hungergames.user.User;
import lombok.Getter;

@Getter
public class CombatTag {

    private User lastHit;
    private long tagTime = 0;

    public void addTag(User attacker, int seconds) {
        this.lastHit = attacker;
        this.tagTime = System.currentTimeMillis() + (seconds * 1000L);
    }

    public boolean isTagged() {
        return System.currentTimeMillis() < tagTime;
    }

}
