package com.minecraft.core.bukkit.server.lobby;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LobbyType {

    MAIN(20, "Main"),
    BRIDGE(20, "The Bridge"),
    DUELS(20, "Duels"),
    HG(20, "HG"),
    PVP(20, "PvP");

    private int maxPlayers;
    private String name;

}
