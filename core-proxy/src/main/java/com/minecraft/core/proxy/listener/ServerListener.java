/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.LogData;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.chat.ChatType;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.core.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {

    @EventHandler
    public void onChatEvent(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();
        Account account = Account.fetch(proxiedPlayer.getUniqueId());

        if (account == null) {
            proxiedPlayer.disconnect(TextComponent.fromLegacyText("§cNão foi possível processar sua conexão."));
            return;
        }

        if (!account.getProperty("authenticated").getAsBoolean() && !event.isCommand()) {
            proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("not_authenticated")));
            event.setCancelled(true);
            return;
        }

        if (!event.isCommand()) {
            if (account.isPunished(PunishType.MUTE, PunishCategory.COMMUNITY)) {
                Punish punish = account.getPunish(PunishType.MUTE, PunishCategory.COMMUNITY);

                if (punish == null) {
                    event.getSender().disconnect(TextComponent.fromLegacyText("Unexpected Data Exception (Try relogging)"));
                    return;
                }

                event.setCancelled(true);

                if (account.getLanguage() == Language.PORTUGUESE) {
                    proxiedPlayer.sendMessage("§cA sua conta está mutada por " + punish.getReason() + (punish.isPermanent() ? "." : " expira em " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED) + "." + (punish.isInexcusable() ? " §c§l(NÃO PODE COMPRAR UNMUTE)" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " §c§l(NÃO PODE COMPRAR UNMUTE)" : ""))));
                } else {
                    proxiedPlayer.sendMessage("§cYour account is muted for " + punish.getReason() + (punish.isPermanent() ? "." : " expires in " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED) + "." + (punish.isInexcusable() ? " §c§l(CAN'T BUY UNMUTE)" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " §c§l(CAN'T BUY UNMUTE)" : ""))));
                }
                return;
            } else if (account.getData(Columns.MUTED).getAsBoolean()) {
                account.getData(Columns.MUTED).setData(false);
            }
        }

        if (!event.isCommand()) {
            ChatType chatType = account.getProperty("chat_type", ChatType.NORMAL).getAs(ChatType.class);

            if (chatType.getProcessor() != null)
                chatType.getProcessor().proccess(account, event);
        } else if (account.getFlag(Flag.PERFORM_COMMANDS)) {
            proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("flag.locked")));
            event.setCancelled(true);
            return;
        }

        ProxyGame.getInstance().addLog(account.getUniqueId(), account.getDisplayName(), proxiedPlayer.getServer().getInfo().getName(), event.getMessage(), event.isCommand() ? LogData.Type.COMMAND : LogData.Type.CHAT);
    }

    @EventHandler(priority = 64)
    public void onProxyPing(final ProxyPingEvent event) {
        final ServerPing ping = event.getResponse();
        final ServerPing.Players players = ping.getPlayers();

        ping.setDescriptionComponent(new TextComponent(MessageUtil.makeCenteredMotd("§c§m-§6§m-§e§m-§a§m-§b§m-§r §b§lBOBMC §b§m-§a§m-§e§m-§6§m-§c§m-§r") + "\n" + MessageUtil.makeCenteredMotd(ChatColor.translateAlternateColorCodes('&', ProxyGame.getInstance().getConfiguration().getString("motd")))));
        players.setMax(ProxyGame.getInstance().getProxy().getConfig().getPlayerLimit());

        event.setResponse(ping);
    }

}