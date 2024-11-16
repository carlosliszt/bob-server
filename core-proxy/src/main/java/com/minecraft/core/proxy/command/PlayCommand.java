/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlayCommand implements ProxyInterface {

    @Command(name = "play", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context) {

        if (context.argsCount() == 0) {
            context.sendMessage("§cUso do /play:");
            for (ServerType serverType : ServerType.values())
                if (serverType.isShown())
                    context.sendMessage("§c* /play " + serverType.getName());
        } else {

            ServerType serverType = ServerType.getByName(context.getArg(0).toUpperCase());

            if (serverType == ServerType.UNKNOWN || !serverType.isShown()) {
                context.info("server.not_found");
                return;
            }

            Server server = serverType.getServerCategory().getServerFinder().getBestServer(serverType);

            if (server == null || server.isDead()) {
                context.info("no_server_available", serverType.getName());
                return;
            }

            ServerInfo proxyServer = ProxyServer.getInstance().getServerInfo(server.getName());

            if (context.getSender().getServer().getInfo().equals(proxyServer)) {
                context.info("already_connected");
                return;
            }

            context.getSender().connect(proxyServer);
        }
    }

    @Completer(name = "play")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return Arrays.stream(ServerType.values()).filter(c -> c.isShown() && startsWith(c.getName(), context.getArg(0))).map(map -> map.getName().toLowerCase()).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
