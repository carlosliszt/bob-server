package com.minecraft.core.bukkit.arcade.game;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum GameType {

    BEDWARS(GameStyle.ARCADE, false, GameQuantity.SOLO, GameQuantity.DOUBLES, GameQuantity.THREESOME, GameQuantity.GROUP),
    TIOGERSON(GameStyle.ARCADE, false, GameQuantity.SOLO),
    THE_BRIDGE(GameStyle.DUEL, false, GameQuantity.SOLO, GameQuantity.DOUBLES, GameQuantity.GROUP),
    UHC(GameStyle.DUEL, false, GameQuantity.SOLO, GameQuantity.DOUBLES, GameQuantity.GROUP),
    SKYWARS(GameStyle.ARCADE, false, GameQuantity.SOLO, GameQuantity.DOUBLES),
    GLADIATOR(GameStyle.DUEL, true, GameQuantity.SOLO, GameQuantity.DOUBLES),
    SIMULATOR(GameStyle.DUEL, true, GameQuantity.SOLO, GameQuantity.DOUBLES),
    SCRIM(GameStyle.DUEL, true, GameQuantity.SOLO, GameQuantity.DOUBLES),
    SOUP(GameStyle.DUEL, true, GameQuantity.SOLO, GameQuantity.DOUBLES),
    BOXING(GameStyle.DUEL, false, GameQuantity.SOLO),
    SUMO(GameStyle.DUEL, false, GameQuantity.SOLO),
    ARENA(GameStyle.PVP, true, GameQuantity.NONE),
    FPS(GameStyle.PVP, true, GameQuantity.NONE),
    LAVA(GameStyle.PVP, true, GameQuantity.NONE),
    DAMAGE(GameStyle.PVP, true, GameQuantity.NONE);

    private final GameStyle style;
    private final boolean allowLegacy;
    private final GameQuantity[] quantities;

    GameType(final GameStyle style, boolean allowLegacy, final GameQuantity... quantities) {
        this.style = style;
        this.allowLegacy = allowLegacy;
        this.quantities = quantities;
    }

    @Getter
    private static final GameType[] values;

    static {
        values = values();
    }

    public static GameType fetch(String s) {
        return Arrays.stream(getValues()).filter(arg -> arg.name().equalsIgnoreCase(s)).findFirst().orElse(null);
    }
}