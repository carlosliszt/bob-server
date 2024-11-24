/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.server;

import com.minecraft.core.server.packet.ServerPayload;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ServerStorage {

    @Getter
    private final List<Server> servers;

    public ServerStorage() {
        this.servers = new ArrayList<>();
    }

    public abstract int myPort();

    public abstract void close();

    public abstract void send();

    public abstract void open();

    public abstract int count();

    public abstract boolean isListen(ServerType serverType);

    public abstract void listen(ServerType... serverTypes);

    public abstract String getNameOf(int port);

    public Server getServer(ServerPayload payload) {
        return servers.stream().filter(c -> c.getPort() == payload.getPort()).findAny().orElse(null);
    }

    public List<Server> getServers(ServerType type) {
        return servers.stream().filter(c -> c.getServerType() == type).collect(Collectors.toList());
    }

    public Server getServer(String name) {
        return servers.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public Server getLocalServer() {
        return servers.stream().filter(c -> c.getPort() == myPort()).findAny().orElse(null);
    }

    public int sum(List<Integer> ints) {
        int count = 0;

        if (ints.isEmpty())
            return -1;

        for (int anInt : ints) {
            count += anInt;
        }
        return count;
    }

    public void registerServer(Server server) {
        servers.add(server);
        servers.sort(Comparator.comparingInt(Server::getPort));
    }

    public int count(ServerType type) {
        return sum(getServers().stream().filter(c -> c.getBreath() != null && !c.isDead() && c.getServerType() == type).map(c -> c.getBreath().getOnlinePlayers()).collect(Collectors.toList()));
    }

    public int count(ServerType... serverTypes) {
        List<ServerType> serverTypeList = Arrays.asList(serverTypes);
        return sum(getServers().stream().filter(c -> c.getBreath() != null && !c.isDead() && serverTypeList.contains(c.getServerType())).map(c -> c.getBreath().getOnlinePlayers()).collect(Collectors.toList()));
    }
}
