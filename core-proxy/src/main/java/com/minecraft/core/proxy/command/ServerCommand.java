/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.proxy.util.server.ServerAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class ServerCommand implements ProxyInterface, Listener {

    @Command(name = "server", platform = Platform.PLAYER, rank = Rank.STREAMER_PLUS)
    public void handleCommand(Context<ProxiedPlayer> context) {
        String[] args = context.getArgs();

        Account account = context.getAccount();

        if (args.length == 0) {

            Map<ServerInfo, Mode> serverHash = new HashMap<>();

            for (ServerInfo serverInfo : ProxyServer.getInstance().getServersCopy().values()) {
                if (ServerAPI.getInstance().isOnline(serverInfo) && serverInfo.getName().toLowerCase().contains("lobby"))
                    serverHash.put(serverInfo, Mode.LOBBY);
                else if (ServerAPI.getInstance().isOnline(serverInfo))
                    serverHash.put(serverInfo, Mode.ONLINE);
                else
                    serverHash.put(serverInfo, Mode.OFFLINE);
            }

            List<ServerInfo> serverInfo = new ArrayList<>(serverHash.keySet());
            serverInfo.sort((a, b) -> Integer.compare(serverHash.get(b).getId(), serverHash.get(a).getId()));

            int max = serverHash.size() * 2;

            TextComponent[] textComponents = new TextComponent[max];
            textComponents[0] = new TextComponent("§eLista de servidores: ");

            int i = max - 1;

            for (ServerInfo server : serverInfo) {
                if (i < max - 1) {
                    textComponents[i] = new TextComponent("§f, ");
                    i -= 1;
                }

                Mode mode = serverHash.get(server);
                TextComponent textComponent = mode.getComponentBuilder().build(account, server);

                textComponents[i] = textComponent;
                i -= 1;
            }

            context.getSender().sendMessage(textComponents);
            return;
        }

        Argument argument = Argument.get(args[0]);

        if (argument.getMinimumArgs() > args.length) {
            context.info(MessageType.INCORRECT_USAGE.getMessageKey(), "/server <server>");
            return;
        }

        argument.getExecutor().execute(context);
    }

    @Completer(name = "server")
    public List<String> handleComplete(Context<CommandSender> context) {
        List<String> response = Arrays.stream(Argument.values()).filter(argument -> argument.getKey() != null && startsWith(argument.getKey(), context.getArg(context.argsCount() - 1))).map(Argument::getKey).collect(Collectors.toList());
        response.addAll(BungeeCord.getInstance().getServers().values().stream().filter(server -> startsWith(server.getName(), context.getArg(context.argsCount() - 1))).map(info -> info.getName().toLowerCase()).collect(Collectors.toList()));
        return response;
    }

    @AllArgsConstructor
    @Getter
    public enum Argument {

        DEFAULT(null, 1, context -> {

            ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(context.getArg(0));

            if (serverInfo == null) {
                context.info("server.not_found");
                return;
            }
            context.getSender().connect(serverInfo);
        }),

        STATUS("status", 2, context -> {

            ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(context.getArg(1));

            if (serverInfo == null) {
                context.info("server.not_found");
                return;
            }

            boolean online = ServerAPI.getInstance().isOnline(serverInfo);

            context.sendMessage("§bOnline:§6 %s", online);
            if (online)
                context.sendMessage("§bPlayers:§6 %s", serverInfo.getPlayers().size());
        });

        private final String key;
        private final int minimumArgs;
        private final Argument.Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(argument -> argument.getKey() != null && argument.getKey().equalsIgnoreCase(key)).findFirst().orElse(DEFAULT);
        }

        protected interface Executor {
            void execute(Context<ProxiedPlayer> context);
        }
    }

    @AllArgsConstructor
    @Getter
    enum Mode {

        LOBBY(1, (account, serverInfo) -> ProxyInterface.createTextComponent("§b" + serverInfo.getName(), HoverEvent.Action.SHOW_TEXT, "§eClique para entrar!", ClickEvent.Action.RUN_COMMAND, "/server " + serverInfo.getName())),

        ONLINE(2, (account, serverInfo) -> ProxyInterface.createTextComponent("§a" + serverInfo.getName(), HoverEvent.Action.SHOW_TEXT, "§aJogadores: §f" + serverInfo.getPlayers().size() + "\n\n§eClique para entrar!", ClickEvent.Action.RUN_COMMAND, "/server " + serverInfo.getName())),

        OFFLINE(3, (account, serverInfo) -> (account.getProperty("isAdmin").getAsBoolean() ? ProxyInterface.createTextComponent("§c" + serverInfo.getName(), HoverEvent.Action.SHOW_TEXT, "§6Clique para ligar!", ClickEvent.Action.SUGGEST_COMMAND, "/motherboard start " + serverInfo.getName()) : ProxyInterface.createTextComponent("§c" + serverInfo.getName(), null, null, null, null)));

        private final int id;
        private final ComponentBuilder componentBuilder;

        protected interface ComponentBuilder {
            TextComponent build(Account account, ServerInfo serverInfo);
        }

    }

}
