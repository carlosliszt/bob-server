/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.user.object;

import com.minecraft.arcade.pvp.user.User;
import lombok.Getter;

@Getter
public class CombatTag {

    private User lastHit;
    private long tagTime = 0L;

    public void addTag(User attacker, int seconds) {
        this.lastHit = attacker;
        this.tagTime = System.currentTimeMillis() + (seconds * 1000L);
    }

    public boolean isTagged() {
        return System.currentTimeMillis() < this.tagTime;
    }

    public void resetTag() {
        this.tagTime = 0L;
    }

}