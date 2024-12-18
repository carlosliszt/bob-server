/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.server;

import com.minecraft.core.Constants;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public enum ServerCategory {

    AUTH((type) -> {
        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),
    BEDWARS((type) -> {
        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),
    CTF((type) -> null),

    TIOGERSON((type) -> {
        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),

    DUELS((type) -> {

        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;

    }),
    HG((type) -> {

        List<Server> servers = getServersCopy();
        servers.removeIf(c -> c.getServerType() != type || c.isDead() || type == ServerType.HGMIX && !c.getBreath().get("stage").toString().equals("WAITING"));
        servers.sort((server1, server2) -> Integer.compare(server2.getBreath().getOnlinePlayers(), server1.getBreath().getOnlinePlayers()));

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),
    LOBBY((type) -> {

        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        servers.sort((a, b) -> Boolean.compare(a.getBreath().getPercentage() > 50, b.getBreath().getPercentage() > 50));

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),
    PVP((type) -> {
        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),
    THE_BRIDGE((type) -> {
        List<Server> servers = getServersCopy();

        servers.removeIf(c -> c.getServerType() != type || c.isDead());

        if (servers.isEmpty())
            return null;

        Server server = servers.get(0);
        servers.clear();
        return server;
    }),
    SKYWARS((type) -> null),
    UNKNOWN((type) -> null);

    private final ServerFinder serverFinder;

    public ServerFinder getServerFinder() {
        return serverFinder;
    }

    public interface ServerFinder {
        Server getBestServer(ServerType type);
    }

    public static List<Server> getServersCopy() {
        return new ArrayList<>(Constants.getServerStorage().getServers());
    }

    public int getId() {
        return ordinal();
    }

    public String getName() {
        return name().toLowerCase();
    }
}
