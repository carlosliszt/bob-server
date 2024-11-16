/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.proxy.util.server.ServerAPI;
import com.minecraft.core.server.packet.ServerListPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MotherboardCommand implements ProxyInterface {

    private final File DIRECTORY = new File(System.getProperty("user.home") + File.separator + "servers");

    @Command(name = "motherboard", rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<CommandSender> context, String action, String server) {

        File SERVER = new File(DIRECTORY, server.toLowerCase());

        if (!SERVER.exists() || !SERVER.isDirectory()) {
            context.info("server.not_found");
            return;
        }

        ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(server);


        if (serverInfo == null) {
            context.info("server.not_found");
            return;
        }

        async(() -> Argument.get(action).getExecutor().execute(context, getPort(SERVER), serverInfo, SERVER));
    }

    @Completer(name = "motherboard")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return Arrays.stream(Argument.values()).filter(argument -> argument.getKey() != null && startsWith(argument.getKey(), context.getArg(context.argsCount() - 1))).map(Argument::getKey).collect(Collectors.toList());
        else
            return Arrays.stream(Objects.requireNonNull(DIRECTORY.listFiles())).filter(c -> startsWith(c.getName(), context.getArg(context.argsCount() - 1))).map(File::getName).collect(Collectors.toList());
    }

    @AllArgsConstructor
    @Getter
    public enum Argument {

        START("start", (context, port, serverInfo, file) -> {

            if (ServerAPI.getInstance().isOnline(serverInfo)) {
                context.info("command.motherboard.server.already_online");
                return;
            }

            try {
                String serverName = serverInfo.getName().toLowerCase();
                new ProcessBuilder().command("screen", "-dmS", serverName, "./start.sh").directory(file).start();
                context.info("command.motherboard.server.start_succesful", serverName);
            } catch (IOException e) {
                context.info("unexpected_error");
                e.printStackTrace();
            }

        }),

        CLOSE("close", (context, port, serverInfo, file) -> {

            if (!ServerAPI.getInstance().isOnline(serverInfo)) {
                context.info("command.motherboard.server.already_closed");
                return;
            }

            try {
                String serverName = serverInfo.getName().toLowerCase();
                new ProcessBuilder().command("screen", "-X", "-S", serverName, "quit").directory(file).start();
                context.info("command.motherboard.server.closed_succesful", serverName);
            } catch (IOException e) {
                context.info("unexpected_error");
                e.printStackTrace();
            }

        });

        private final String key;
        private final Argument.Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(argument -> argument.getKey() != null && argument.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
        }

        protected interface Executor {
            void execute(Context<CommandSender> context, int port, ServerInfo serverInfo, File file);
        }
    }

    public int getPort(File file) {
        ServerListPacket.ServerInfo server = ProxyGame.getInstance().getServerListPacket().getServers().stream().filter(c -> c.getName().equalsIgnoreCase(file.getName())).findFirst().orElse(null);
        return server == null ? -1 : server.getPort();
    }
}
