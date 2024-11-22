/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import org.bukkit.entity.Player;

public class LobbyCommand implements BukkitInterface {

    @Command(name = "lobby", aliases = {"hub", "l"}, usage = "{label} <target>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        ServerType lobbyType = Constants.getLobbyType();

        if (lobbyType.getServerCategory() != ServerCategory.LOBBY) {
            context.info("no_server_available", "lobby");
            return;
        }

        if (lobbyType == Constants.getServerType()) {
            if (context.getAccount().hasPermission(Rank.ADMINISTRATOR)) {
                context.sendMessage("§cVocê já está conectado neste servidor, idiota.");
            } else {
                context.sendMessage("§cVocê já está conectado neste servidor.");
            }
            return;
        }

        Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(lobbyType);

        if (server == null && lobbyType != ServerType.MAIN_LOBBY)
            server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY);

        if (server == null) {
            context.info("no_server_available", lobbyType.getName());
            return;
        }

        context.getAccount().connect(server);

    }
}
