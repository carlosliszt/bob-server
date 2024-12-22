/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PasteCommand implements ProxyInterface {

    @Command(name = "paste", usage = "paste <id> <command> [format...]", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<ProxiedPlayer> context, String paste, String[] rawcommand) {

        String URL = "http://localhost/raw/";

        context.info("command.whitelist.import_processing");

        String command = createArgs(0, rawcommand, "...", false);

        async(() -> {

            HttpRequest request = HttpRequest.get(URL + paste).connectTimeout(5000).readTimeout(5000).userAgent("Administrator/1.0.0").acceptJson();

            if (request.ok()) {
                request.bufferedReader().lines().forEach(line -> {
                    String[] lx = line.split(" ");
                    BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), String.format(command, (Object[]) lx));
                    context.sendMessage("§aComando '§f" + command + "§a' §aexecutado para: §f" + line);
                });
            } else {
                context.info("unexpected_error");
            }
        });
    }
}
