/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.protocol;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.lang.reflect.Field;

@Getter
@Setter
@AllArgsConstructor
public class PacketSendEvent extends ServerEvent implements Cancellable {

    private final Player player;
    private Packet packet;
    private boolean cancelled;

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public <T> T getValue(String fieldName) {
        try {
            Field f = getPacket().getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(getPacket());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(String fieldName, Object value) {
        try {
            Field f = getPacket().getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(getPacket(), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
