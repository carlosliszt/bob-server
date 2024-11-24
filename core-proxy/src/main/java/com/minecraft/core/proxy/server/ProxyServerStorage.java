/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.server;

import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.server.packet.ServerListPacket;
import net.md_5.bungee.api.ProxyServer;

import java.util.Comparator;

public class ProxyServerStorage extends ServerStorage {

    public ProxyServerStorage() {
        super();
    }

    @Override
    public int myPort() {
        return 25565;
    }

    @Override
    public void close() {
    }

    @Override
    public void send() {
    }

    @Override
    public void open() {
        ProxyGame.getInstance().getServerListPacket().getServers().stream().sorted(Comparator.comparingInt(ServerListPacket.ServerInfo::getPort)).forEach(server -> {
            getServers().add(new Server(server.getName(), server.getPort(), null, ServerType.UNKNOWN, ServerCategory.UNKNOWN));
        });
    }

    @Override
    public int count() {
        return ProxyServer.getInstance().getOnlineCount();
    }

    @Override
    public boolean isListen(ServerType serverType) {
        return true;
    }

    @Override
    public void listen(ServerType... serverTypes) {
    }

    @Override
    public String getNameOf(int port) {
        ServerListPacket serverList = ProxyGame.getInstance().getServerListPacket();
        ServerListPacket.ServerInfo serverInfo = serverList.getServers().stream().filter(o -> o.getPort() == port).findFirst().orElse(null);
        return serverInfo == null ? "" : serverInfo.getName();
    }
}
