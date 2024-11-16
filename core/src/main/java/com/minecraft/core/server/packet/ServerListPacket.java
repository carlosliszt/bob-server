/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.server.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ServerListPacket {

    @SerializedName(value = "servers")
    private final Set<ServerInfo> servers = new HashSet<>();

    @Getter
    public static class ServerInfo {

        private final String name;
        private final int port;

        public ServerInfo(String name, int port) {
            this.name = name;
            this.port = port;
        }

        @Override
        public String toString() {
            return "ServerInfo{" +
                    "name='" + name + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
