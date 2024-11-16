/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class StafflistCommand implements ProxyInterface {

    @Command(name = "stafflist", aliases = {"sl"}, rank = Rank.PRIMARY_MOD)
    public void handleCommand(Context<CommandSender> context) {
        List<Account> accountList = new ArrayList<>();

        for (ProxiedPlayer players : ProxyServer.getInstance().getPlayers()) {
            Account accounts = Account.fetch(players.getUniqueId());

            if (accounts == null) {
                players.disconnect(TextComponent.fromLegacyText("§cNão foi possível processar sua conexão."));
                continue;
            }

            if (!accounts.hasPermission(Rank.HELPER))
                continue;
            if (!context.getAccount().hasPermission(accounts.getRank()))
                continue;
            accountList.add(accounts);
        }

        accountList.stream().sorted((a, b) -> Integer.compare(b.getRank().getId(), a.getRank().getId())).forEach(account -> {
            Tag tag = account.getRank().getDefaultTag();

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(account.getUniqueId());
            String server = player.getServer().getInfo().getName();

            TextComponent serverComponent = new TextComponent(" §f- §7(" + server + ") ");
            TextComponent textComponent = new TextComponent(tag.getColor() + "§l" + tag.getName().toUpperCase() + " §r" + tag.getColor() + account.getUsername());

            serverComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server));
            serverComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§7Click to go.")}));

            context.getSender().sendMessage(serverComponent, textComponent);
        });
    }
}