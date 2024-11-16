/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.event.PunishAssignEvent;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.core.util.StringTimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Date;

public class PunishmentListener implements Listener, ProxyInterface {

    @EventHandler
    public void AssignPunish(PunishAssignEvent event) {
        Punish punish = event.getPunish();
        Account account = event.getAccount();

        ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(account.getUniqueId());

        if (proxiedPlayer != null) {

            if (punish.getType() == PunishType.BAN) {
                StringBuilder msg = new StringBuilder();

                if (account.getLanguage() == Language.PORTUGUESE) {
                    msg.append(punish.isPermanent() ? "§cVocê foi suspenso permanentemente." : "§cVocê está suspenso temporariamente.").append("\n");
                    if (punish.getCategory() != PunishCategory.NONE)
                        msg.append("§cMotivo: ").append(punish.getCategory().getDisplay(Language.PORTUGUESE)).append("\n");

                    if (!punish.isPermanent())
                        msg.append("§cExpira em: ").append(DateUtils.formatDifference(punish.getTime())).append("\n");

                    msg.append("§cPode comprar unban: ").append(punish.isInexcusable() ? "Não" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? "Não" : "Sim")).append("\n");

                    if (punish.isAutomatic())
                        msg.append("§cBanimento automático.\n");

                    msg.append("§cID: #").append(punish.getCode()).append("\n\n");
                    msg.append("§cSaiba mais em ").append(Constants.SERVER_WEBSITE);
                } else {
                    msg.append(punish.isPermanent() ? "§cYou are permanently banned." : "§cYou are temporarily banned.").append("\n");
                    if (punish.getCategory() != PunishCategory.NONE)
                        msg.append("§cReason: ").append(punish.getCategory().getDisplay(Language.ENGLISH)).append("\n");

                    if (!punish.isPermanent())
                        msg.append("§cExpires in: ").append(DateUtils.formatDifference(punish.getTime())).append("\n");

                    msg.append("§cCan buy unban: ").append(punish.isInexcusable() ? "No" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? "No" : "Yes")).append("\n");

                    if (punish.isAutomatic())
                        msg.append("§cAutomatic ban.\n");

                    msg.append("§cBan ID: #").append(punish.getCode()).append("\n\n");
                    msg.append("§cFind out more on ").append(Constants.SERVER_WEBSITE);
                }

                if (punish.getCategory() == PunishCategory.CHEATING) {
                    broadcast(proxiedPlayer.getServer().getInfo());
                }

                proxiedPlayer.disconnect(TextComponent.fromLegacyText(msg.toString()));

            } else if (punish.getType() == PunishType.MUTE && punish.getCategory() == PunishCategory.COMMUNITY) {
                proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§c§m                                                            "));
                if (account.getLanguage() == Language.PORTUGUESE) {
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§cVocê foi" + (punish.isPermanent() ? " permanentemente" : " temporariamente") + " silenciado por " + punish.getReason()));
                    if (!punish.isPermanent())
                        proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§7Seu silenciamento expirará em§c " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED)));
                    proxiedPlayer.sendMessage("");
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§7Saiba mais em §e" + Constants.SERVER_WEBSITE));
                } else {
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§cYou've been" + (punish.isPermanent() ? " permanently" : " temporarily") + " muted for " + punish.getReason()));
                    if (!punish.isPermanent())
                        proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§7Your mute will expire in§c " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED)));
                    proxiedPlayer.sendMessage("");
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§7Find out more on §e" + Constants.SERVER_WEBSITE));
                }
                proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§7ID: §f#" + punish.getCode()));
                proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§c§m                                                            "));
            }
        }

        async(() -> {

            EmbedBuilder builder = new EmbedBuilder();

            builder.setThumbnail("https://crafatar.com/avatars/" + account.getUniqueId() + "?size=256");
            builder.setTimestamp(new Date(punish.getApplyDate()).toInstant());
            builder.setTitle("Punição #" + punish.getCode() + " aplicada! (ID #" + event.getAccount().count(punish.getType()) + ")");
            builder.addField("Alvo", account.getUsername(), true);
            builder.addField("Tipo", punish.getType().getName(), true);
            builder.addField("Autor", punish.getApplier(), true);
            builder.addField("Categoria", punish.getCategory().getDisplay(Language.PORTUGUESE), true);
            builder.addField("Motivo", punish.getReason(), true);

            if (!punish.isPermanent()) {
                builder.addField("Expira em", StringTimeUtils.formatDifference(StringTimeUtils.Type.SIMPLIFIED, (punish.getTime() + 1000)), true);
            }

            TextChannel textChannel = ProxyGame.getInstance().getDiscord().getJDA().getTextChannelById("850799516499443742");

            if (textChannel != null)
                textChannel.sendMessage(builder.build()).queue();
        });
    }

    protected void broadcast(ServerInfo server) {
        for (ProxiedPlayer proxiedPlayer : BungeeCord.getInstance().getPlayers()) {

            Server playerServer = proxiedPlayer.getServer();

            if (playerServer == null)
                continue;

            if (playerServer.getInfo() == server) {
                Account account = Account.fetch(proxiedPlayer.getUniqueId());
                if (account == null)
                    continue;
                proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("commnad.punish.cheating_broadcast")));
            }
        }
    }
}