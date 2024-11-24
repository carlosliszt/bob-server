/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.leaderboard.libs;

import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaderboardData {

    private static final SkinData DEFAULT;
    private final UUID uniqueId;
    private final Columns statistic;
    private final Map<Columns, Data> dataMap = new HashMap<>();
    private SkinData skinData = new SkinData();

    static {
        DEFAULT = new SkinData();
        DEFAULT.setSource(SkinData.Source.FORCED);
        DEFAULT.setValue("ewogICJ0aW1lc3RhbXAiIDogMTYyMjE4OTY2MjQwOSwKICAicHJvZmlsZUlkIiA6ICJkZTE0MGFmM2NmMjM0ZmM0OTJiZTE3M2Y2NjA3MzViYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTUlRlYW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdiNWU3Nzg5N2U3ODExM2RiY2Y2NTQzYTY2MDQxY2E1MTFmYjc4ZjM1ZGY1MjRiYWJiZDE5MWZlZGQwOTFjOSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        DEFAULT.setSignature("cCKog205IiJdVd8a80kulhlYQu7iYYQfVr91XkzL9GtPf0VV1kpF1QMYsNhbCGDnwfdigXLHH8BMNuEhDA4GwY5DLkxiwrv7qqAsMwEcVa9jjJlLEOyNDRmQlXXLzx0dDXcZ498Q9YhG8P/+jMuIUI0qTd7npMyilKD7avxRmNrrf+ZTtpS970pVU+nflOw8HMTmOtw9slLPtSAm8pFtWmC/0Y3tPn3wBN25ibxZY+bXxHEBPSkwy1GqK9P28dPSs9z0TGj3Q5fNbRUZvpaDfD9fbz6eVyD1IdxCK9ZddtKd7a8L/B76wNtupyPR9ttt9lgcVfKA9M9tBT8KZwKVkYzcFshlUUnuAsx8EdVAN1Y9+rsQuwg8lOJTiZHROIuCdaDdJ6+W4t7BmdZZyFHEtlX2jpZnyHYl6AEtMdJaTJYaVOqVrUx5RjYBvnJMBNmMG2OGELcc3/z2RMBk6FzAKqP0x7qXo1n3++AxKR19eMV11/E5wvER7amPSy0qub7xfSZL3dH/cUDD1z1ODByiVjzswKQtRcCe28ErncMDRsrl+U87XmCLbYLo4Ox2zi0dv5yBkDVq9A2voqB4TBQ1kwQ5A05ovh4kt4atQTCPfA6zyeobZjRam9eOkIQdejNTcRnCrhVUdh5WDuBbhijJKgkogHsVheUtfMCg04xNGps=");
        DEFAULT.setName("default.skin");
        DEFAULT.setUpdatedAt(-1);
    }

    public LeaderboardData(UUID uniqueId, Columns[] columns, Object[] values) {

        this.uniqueId = uniqueId;
        this.statistic = columns[0];

        for (int i = 0; i < columns.length; i++) {

            Object value;
            if (i + 1 > values.length)
                value = columns[i].getDefaultValue();
            else
                value = values[i];

            setValue(columns[i], value);
        }
    }

    public LeaderboardData(UUID uniqueId, Columns statistic) {
        this.uniqueId = uniqueId;
        this.statistic = statistic;
    }

    public Data getValue(Columns column) {
        return dataMap.computeIfAbsent(column, v -> new Data(column, column.getDefaultValue()));
    }

    public void setValue(Columns column, Object value) {
        dataMap.computeIfAbsent(column, v -> new Data(column, value)).setData(value);

        if (column == Columns.SKIN)
            loadSkinData();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return getValue(Columns.USERNAME).getAsString();
    }

    public SkinData getSkinData() {
        return skinData.isInvalid() ? DEFAULT : skinData;
    }

    public void loadSkinData() {
        System.out.println("Loaded " + this + "'s skin!");
        this.skinData = SkinData.fromJson(getValue(Columns.SKIN).getAsJsonObject());
    }

    public Data getStatistic() {
        return dataMap.get(statistic);
    }

    @Override
    public String toString() {
        return "LeaderboardData{" +
                "uniqueId=" + uniqueId +
                "name=" + getName() +
                '}';
    }
}
