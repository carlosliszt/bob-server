/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.server;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerHeartbeatEvent extends ServerEvent {

    private int tick;

    public boolean isPeriodic(int x) {
        return tick % x == 0;
    }


}
