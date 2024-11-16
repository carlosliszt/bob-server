/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.ranking;

import com.minecraft.core.database.enums.Columns;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankingTarget {

    PVP(Columns.PVP_RANK, Columns.PVP_RANK_EXP, null, null),
    HG(Columns.HG_RANK, Columns.HG_RANK_EXP, Columns.HG_KILLS, Columns.HG_WINS),
    SCRIM(Columns.SCRIM_RANK, Columns.SCRIM_RANK_EXP, Columns.SCRIM_KILLS, Columns.SCRIM_WINS),
    THE_BRIDGE(Columns.BRIDGE_RANK, Columns.BRIDGE_RANK_EXP, null, null),
    DUELS(null, null, null, null);

    private final Columns ranking, experience, kills, wins;
}
