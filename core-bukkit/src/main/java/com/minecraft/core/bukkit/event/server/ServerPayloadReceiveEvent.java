/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.server;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.packet.ServerPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerPayloadReceiveEvent extends ServerEvent {

    private final Server server;
    private final ServerPayload payload;

}
