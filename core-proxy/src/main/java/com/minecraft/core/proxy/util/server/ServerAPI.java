/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.server;

import lombok.Getter;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Collection;

public class ServerAPI {
    @Getter
    private static final ServerAPI instance = new ServerAPI();

    public boolean isOnline(ServerInfo serverInfo) {
        boolean online = true;
        try {
            Socket socket = new Socket();
            socket.connect(serverInfo.getSocketAddress(), 30);
            socket.close();
        } catch (Exception e) {
            online = false;
        }
        return online;
    }

    public boolean hasPendingConnection(ProxiedPlayer player) {
        return !getPendingConnects(player).isEmpty();
    }

    private Collection<ServerInfo> getPendingConnects(ProxiedPlayer proxiedPlayer) {
        try {
            return (Collection<ServerInfo>) field.get(proxiedPlayer);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Field field;

    static {
        try {
            Class<?> clazz = UserConnection.class;
            field = clazz.getDeclaredField("pendingConnects");
            field.setAccessible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
