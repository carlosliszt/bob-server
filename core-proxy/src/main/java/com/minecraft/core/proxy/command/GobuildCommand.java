package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GobuildCommand {

    @Command(name = "gobuild", platform = Platform.PLAYER, rank = Rank.BUILDER)
    public void onCommand(Context<ProxiedPlayer> context) {
        ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo("build");

        if (serverInfo == null) {
            context.info("server.not_found");
            return;
        }

        context.getSender().connect(serverInfo);
    }

    @Command(name = "survival", platform = Platform.PLAYER, rank = Rank.BUILDER)
    public void onSurvival(Context<ProxiedPlayer> context) {
        ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo("Survival");

        if (serverInfo == null) {
            context.info("server.not_found");
            return;
        }

        context.getSender().connect(serverInfo);
    }

}