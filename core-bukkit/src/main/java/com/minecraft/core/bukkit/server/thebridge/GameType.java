package com.minecraft.core.bukkit.server.thebridge;

import com.minecraft.core.database.enums.Columns;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GameType {

    SOLO(2, 10, Columns.BRIDGE_SOLO_WINS, Columns.BRIDGE_SOLO_LOSSES, Columns.BRIDGE_SOLO_KILLS, Columns.BRIDGE_SOLO_DEATHS, Columns.BRIDGE_SOLO_POINTS, Columns.BRIDGE_SOLO_ROUNDS, Columns.BRIDGE_SOLO_WINSTREAK, Columns.BRIDGE_SOLO_MAX_WINSTREAK),
    DOUBLE(4, 5, Columns.BRIDGE_DOUBLES_WINS, Columns.BRIDGE_DOUBLES_LOSSES, Columns.BRIDGE_DOUBLES_KILLS, Columns.BRIDGE_DOUBLES_DEATHS, Columns.BRIDGE_DOUBLES_POINTS, Columns.BRIDGE_DOUBLES_ROUNDS, Columns.BRIDGE_DOUBLES_WINSTREAK, Columns.BRIDGE_DOUBLES_MAX_WINSTREAK);

    private final int maxPlayers, maxGames;
    private final Columns wins, losses, kills, deaths, points, rounds, winstreak, maxWinstreak;

    @Getter
    private static final GameType[] values;

    static {
        values = values();
    }

}