/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.annotation.Optional;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.List;

public class KickCommand implements ProxyInterface {

    @Command(name = "kick", usage = "kick <target> <reason>", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<ProxiedPlayer> context, Account target, @Optional(def = "Não informado.") String[] reason) {
        if (target == null) {
            context.info("target.not_found");
            return;
        }
        if (context.getAccount().getRank().getId() <= target.getRank().getId()) {
            context.info("command.kick.cant_kick_superior");
            return;
        }
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(target.getUsername());
        player.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', ChatColor.RED + Account.fetch(player.getUniqueId()).getLanguage().translate(String.join(" ", reason)))));
        context.info("command.kick.execution_successful", player.getDisplayName());
    }

    @Completer(name = "kick")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }
}