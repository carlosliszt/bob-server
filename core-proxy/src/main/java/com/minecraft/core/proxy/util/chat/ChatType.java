/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.chat;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;

@AllArgsConstructor
@Getter
public enum ChatType {

    NORMAL(0, null),
    STAFF(1, (sender, chatEvent) -> {
        chatEvent.setCancelled(true);

        if (!sender.getPreference(Preference.STAFFCHAT)) {
            ((ProxiedPlayer) chatEvent.getSender()).sendMessage(TextComponent.fromLegacyText("§cVocê está falando no staff chat com ele desativado."));
            return;
        }

        String message = "§e[STAFF] " + PrefixType.DEFAULT.getFormatter().format(sender.getRank().getDefaultTag()) + sender.getUsername() + ": §f" + chatEvent.getMessage();
        ProxyServer.getInstance().getPlayers().stream().filter(c -> {
            Account account = Account.fetch(c.getUniqueId());
            return account != null && account.hasPermission(Rank.HELPER) && account.getPreference(Preference.STAFFCHAT);
        }).forEach(staff -> staff.sendMessage(TextComponent.fromLegacyText(message)));
        ProxyGame.getInstance().getDiscord().getDiscordListener().hook(sender, chatEvent.getMessage());
    }),
    CLAN(2, (sender, chatEvent) -> {

        chatEvent.setCancelled(true);

        if (!sender.hasClan()) {
            sender.setProperty("chat_type", sender.getProperty("old_chat_type", ChatType.NORMAL).getAs(ChatType.class));
            return;
        }

        Clan clan = sender.getClan();

        if (clan == null) {
            sender.setProperty("chat_type", sender.getProperty("old_chat_type", ChatType.NORMAL).getAs(ChatType.class));
            return;
        }

        clan.getMembers().forEach(member -> {

            ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(member.getUniqueId());

            if (proxiedPlayer == null)
                return;

            proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§9[CLAN] §7" + sender.getUsername() + ": §r" + chatEvent.getMessage()));
        });
    });

    private final int id;
    private final Processor processor;

    public interface Processor {
        void proccess(Account account, ChatEvent chatEvent);
    }
}
