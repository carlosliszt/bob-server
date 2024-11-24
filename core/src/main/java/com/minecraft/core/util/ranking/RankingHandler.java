/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.ranking;

import com.minecraft.core.account.Account;
import com.minecraft.core.enums.Ranking;

public abstract class RankingHandler {

    public void onRankingUpgrade(Account account, Ranking old, Ranking upgrade) {
    }

    public void onRankingDowngrade(Account account, Ranking downgrade) {
    }

    public void onChallengerAssign(Account account) {
    }

    public void onChallengerDesign(Account account) {
    }
}
