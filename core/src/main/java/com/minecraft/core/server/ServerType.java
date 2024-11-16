/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ServerType {

    UNKNOWN("local", false, ServerCategory.UNKNOWN),
    PROXY("BOB", false, ServerCategory.UNKNOWN),
    LIMBO_AFK("AFK", false, ServerCategory.LOBBY),
    HGMIX("hgmix", true, ServerCategory.HG),
    DOUBLEKIT("doublekit", true, ServerCategory.HG),
    SINGLEKIT("singlekit", true, ServerCategory.HG),
    MM_CLANXCLAN("clanxclan", true, ServerCategory.HG),
    CLANXCLAN("clanxclan", false, ServerCategory.HG),
    TEAMHG("teamhg", true, ServerCategory.HG),
    TOURNAMENT("TORNEIO", false, ServerCategory.HG),
    THE_BRIDGE("thebridge", false, ServerCategory.THE_BRIDGE),
    SCRIM("scrim", true, ServerCategory.HG),
    EVENT("evento", true, ServerCategory.HG),
    PVP("pvp", false, ServerCategory.PVP),
    DUELS_LOBBY("duels", true, ServerCategory.LOBBY),
    PVP_LOBBY("pvp", true, ServerCategory.LOBBY),
    HG_LOBBY("hg", true, ServerCategory.LOBBY),
    THE_BRIDGE_LOBBY("thebridge", true, ServerCategory.LOBBY),
    GLADIATOR_LOBBY("gladiator", false, ServerCategory.LOBBY),
    MAIN_LOBBY("lobby", false, ServerCategory.LOBBY),
    AUTH("auth", false, ServerCategory.AUTH),
    DUELS("duels", false, ServerCategory.DUELS);

    private final String name;
    private final boolean shown;
    private final ServerCategory serverCategory;

    public static ServerType getByName(String name) {
        return Arrays.stream(values()).filter(c -> c.isShown() && c.getName().equalsIgnoreCase(name)).findFirst().orElse(UNKNOWN);
    }

    public boolean isShown() {
        return this.shown;
    }

    public String getName() {
        return this.name;
    }
}
