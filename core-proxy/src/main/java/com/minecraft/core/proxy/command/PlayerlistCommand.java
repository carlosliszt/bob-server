/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerlistCommand implements ProxyInterface {

    @Command(name = "playerlist", rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<CommandSender> context) {
        int cracked_users = 0, premium_users = 0, users = 0, v1_7 = 0, v1_8_x = 0;

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            users++;
            if (player.getPendingConnection().isOnlineMode())
                premium_users++;
            else
                cracked_users++;

            if (player.getPendingConnection().getVersion() >= 47)
                v1_8_x++;
            else
                v1_7++;
        }

        CommandSender commandSender = context.getSender();

        msg(commandSender, " ");
        msg(commandSender, " §fTotal: §b" + users + " (1.7 = " + v1_7 + " 1.8.x = " + v1_8_x + ")");
        msg(commandSender, " ");
        msg(commandSender, " §fPremium: §7" + premium_users + " §a(" + ((premium_users * 100) / users) + "%)");
        msg(commandSender, " §fCracked: §7" + cracked_users + " §c(" + ((cracked_users * 100) / users) + "%)");
        msg(commandSender, " ");
    }

    private void msg(CommandSender commandSender, String message) {
        commandSender.sendMessage(TextComponent.fromLegacyText(message));
    }

}