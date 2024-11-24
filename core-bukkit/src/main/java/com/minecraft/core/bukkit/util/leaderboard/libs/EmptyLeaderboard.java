/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.leaderboard.libs;

import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;

import java.util.ArrayList;
import java.util.List;

public class EmptyLeaderboard extends Leaderboard {

    public EmptyLeaderboard() {
        super(null, LeaderboardUpdate.NEVER, LeaderboardType.PLAYER, -1);
    }

    @Override
    public Leaderboard query() {
        return this;
    }

    @Override
    public void destroy() {
    }

    @Override
    public List<LeaderboardData> values() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "EmptyLeaderboard{statistic=null, empty=true}";
    }
}
