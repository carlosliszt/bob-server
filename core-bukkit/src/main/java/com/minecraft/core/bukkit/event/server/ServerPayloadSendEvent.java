/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.server;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import com.minecraft.core.server.packet.ServerPayload;
import lombok.Getter;
import org.bukkit.event.Cancellable;

@Getter
public class ServerPayloadSendEvent extends ServerEvent implements Cancellable {

    private final ServerPayload payload;
    private boolean cancelled;

    public ServerPayloadSendEvent(ServerPayload payload) {
        this.payload = payload;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
