/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.server.packet;

import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ServerPayload {

    private final Map<String, Object> properties;

    private final ServerCategory serverCategory;
    private final ServerType serverType;
    private final int port;
    private int onlinePlayers;
    private final int maxPlayers;
    private final long sent;

    public ServerPayload(ServerCategory category, ServerType type, int port, int onlinePlayers, int maxPlayers) {
        this.port = port;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.sent = System.currentTimeMillis();
        this.properties = new HashMap<>();
        this.serverCategory = category;
        this.serverType = type;
    }

    public ServerPayload write(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    public Object get(String key) {
        return properties.putIfAbsent(key, "");
    }

    public int getPort() {
        return port;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public long getSent() {
        return sent;
    }

    public ServerCategory getServerCategory() {
        return serverCategory;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void overrideOnlineCount(int i) {
        this.onlinePlayers = i;
    }

    @Override
    public String toString() {
        return "ServerPayload{" +
                "properties=" + properties +
                ", serverCategory=" + serverCategory +
                ", serverType=" + serverType +
                ", port=" + port +
                ", onlinePlayers=" + onlinePlayers +
                ", maxPlayers=" + maxPlayers +
                ", sent=" + sent +
                '}';
    }

    public int getPercentage() {
        int players = getOnlinePlayers();
        int maxPlayers = getMaxPlayers() - 15;

        return (players * 100) / maxPlayers;
    }

    public boolean isWritten(String name) {
        return properties.containsKey(name);
    }

}
