/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.server;

import com.minecraft.core.server.packet.ServerPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Server {

    private final String name;
    private final int port;
    private ServerPayload breath;
    private ServerType serverType;
    private ServerCategory serverCategory;

    public void setLastBreath(ServerPayload payload) {
        this.breath = payload;
    }

    public boolean isDead() {
        return getBreath() == null || getBreath().getSent() + 2500 < System.currentTimeMillis();
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public void setServerCategory(ServerCategory serverCategory) {
        this.serverCategory = serverCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return port == server.port;
    }
}
