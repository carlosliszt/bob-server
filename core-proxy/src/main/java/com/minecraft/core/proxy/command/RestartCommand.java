package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class RestartCommand {

    @Command(name = "carlos", platform = Platform.BOTH, rank = Rank.DEVELOPER_ADMIN)
    public void restartCommand(Context<CommandSender> context) {

        for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
            player.sendMessage("§cO servidor será reiniciado em 10 segundos, volte em alguns instantes.");
        }

        BungeeCord.getInstance().getScheduler().schedule(ProxyGame.getInstance(), () -> {
            BungeeCord.getInstance().stop();
        }, 10, TimeUnit.SECONDS);
    }

}
