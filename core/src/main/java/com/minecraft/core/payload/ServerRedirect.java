/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.payload;

import com.minecraft.core.server.Server;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ServerRedirect {

    private UUID uniqueId;
    private Route route;

    public ServerRedirect(UUID uuid, Route route) {
        this.uniqueId = uuid;
        this.route = route;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Route {

        private Server server;
        private String internalRoute;

    }

}
