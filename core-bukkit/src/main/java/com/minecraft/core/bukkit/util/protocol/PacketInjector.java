/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.protocol;

import io.netty.channel.Channel;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketInjector {

    private final String pipelineName = "BobPackets";

    public void addPlayer(Player p) {
        try {
            Channel ch = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
            if (ch.pipeline().get(pipelineName) == null) {
                PacketListener packetListener = new PacketListener(p);
                ch.pipeline().addBefore("packet_handler", pipelineName, packetListener);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void removePlayer(Player p) {
        try {
            Channel ch = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
            if (ch.pipeline().get(pipelineName) != null) {
                ch.eventLoop().submit(() -> {
                    ch.pipeline().remove("BobPackets");
                });
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}