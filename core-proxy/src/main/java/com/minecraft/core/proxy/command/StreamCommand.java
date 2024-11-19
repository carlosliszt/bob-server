package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StreamCommand implements ProxyInterface {

    @Command(name = "stream", usage = "stream <url>", platform = Platform.PLAYER, rank = Rank.YOUTUBER)
    public void handleCommand(Context<ProxiedPlayer> context, String url) {
        if (url == null || url.isEmpty()) {
            context.info("command.stream.empty_url");
            return;
        }

        Account account = context.getAccount();

        if (!account.hasPermission(Rank.PARTNER_PLUS)) {
            context.info("command.insufficient_permission");
            return;
        }

        if (account.getProperty("stream_cooldown", 0L).getAsLong() > System.currentTimeMillis()) {
            context.info("command.stream.cooldown", (account.getProperty("stream_cooldown").getAsLong() / 1000));
            return;
        }

        account.setProperty("stream_cooldown", System.currentTimeMillis() + 1200000);

        String announce = "§b§l" + Constants.SERVER_NAME.toUpperCase() + " §7» §fO nosso " + PrefixType.DEFAULT.getFormatter().format(account.getRank().getDefaultTag()) + account.getUsername() + " §festá em live agora! Acesse§e " + url;

        for (int i = 1; i < 3; i++) {
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(announce));
        }

    }

}