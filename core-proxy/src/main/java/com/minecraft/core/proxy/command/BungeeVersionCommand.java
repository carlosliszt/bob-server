package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeVersionCommand {

    @Command(name = "bungeever", aliases = {"bver", "bungeeversion"})
    public void handleCommand(Context<CommandSender> ctx) {
        ctx.sendMessage(BungeeCord.getInstance().getPluginManager().getPlugin("core-proxy").getDescription().getVersion());
    }
}
