/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.event.protocol.PacketReceiveEvent;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class PacketFilterCommand extends DynamicListener {

    private final Set<Player> players = new HashSet<>();

    @Command(name = "filter", usage = "{label} <prefixtype>", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void handleCommand(Context<Player> context, Player player) {

        if (player == null) {
            context.info("target.not_found");
            return;
        }

        if (!players.contains(player))
            players.add(player);
        else
            players.remove(player);

        if (players.isEmpty())
            unregister();
        else
            register();

        context.info("filter_successful");
    }

    @EventHandler
    public void onPacket(PacketReceiveEvent event) {

        if (event.getPacket() instanceof PacketPlayInFlying || event.getPacket() instanceof PacketPlayInFlying.PacketPlayInPosition)
            return;

        if (players.contains(event.getPlayer())) {
            try {
                StringBuilder stringBuilder = new StringBuilder("INCOMING " + event.getPlayer().getName() + "§r ");

                Packet packet = event.getPacket();

                stringBuilder.append(packet.getClass().getSimpleName());

                for (Field field : packet.getClass().getFields()) {
                    field.setAccessible(true);
                    stringBuilder.append(" ").append(field.getName()).append("=").append(field.get(packet));
                }

                System.out.println(stringBuilder + " (" + Constants.GSON.toJson(packet));
            } catch (Exception e) {
                System.out.println("Failed to handle packet " + event.getPacket().getClass().getSimpleName());
            }
        }
    }

 /*   @EventHandler
    public void onPacket(PacketSendEvent event) {
        if (players.contains(event.getPlayer())) {
            try {
                StringBuilder stringBuilder = new StringBuilder("§e§lOUTGOING §c" + event.getPlayer().getName() + "§r ");

                Packet packet = event.getPacket();

                stringBuilder.append(packet.getClass().getSimpleName());

                for (Field field : packet.getClass().getFields()) {
                    field.setAccessible(true);
                    stringBuilder.append(" ").append(field.getName()).append("=").append(field.get(packet));
                }

                Bukkit.getConsoleSender().sendMessage(stringBuilder + " (" + Constants.GSON.toJson(packet));
            } catch (Exception e) {
                System.out.println("Failed to handle packet " + event.getPacket().getClass().getSimpleName());
            }
        }
    }*/

}
