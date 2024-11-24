/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.leaderboard.libs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LeaderboardUpdate {

    NEVER(Integer.MAX_VALUE), HOUR(72000), HALF_HOUR(36000), MINUTE(1200), SECOND(20), TICK(1);

    private final int period;
}
