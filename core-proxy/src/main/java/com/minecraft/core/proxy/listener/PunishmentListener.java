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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.awt.*;
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
                    msg.append(punish.isPermanent() ? "§cVocê foi banido permanentemente." : "§cVocê foi banido temporariamente.").append("\n");
                    if (punish.getCategory() != PunishCategory.NONE)
                        msg.append("§cMotivo: ").append(punish.getCategory().getDisplay(Language.PORTUGUESE)).append("\n");

                    if (!punish.isPermanent())
                        msg.append("§cExpira em: ").append(DateUtils.formatDifference(punish.getTime())).append("\n");

                    msg.append("§cPode comprar unban: ").append(punish.isInexcusable() ? "Não" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? "Não" : "Sim")).append("\n");

                    msg.append("§cID: #").append(punish.getCode()).append("\n\n");

                    if (!punish.isInexcusable()) {
                        msg.append("§eCompre seu unban em: §bbobmc.com.br/unban\n");
                    }

                    msg.append("§cBanido injustamente? Contate-nos via: " + Constants.SERVER_WEBSITE + "/appeal").append("\n");

                } else {
                    msg.append(punish.isPermanent() ? "§cYou are permanently banned." : "§cYou are temporarily banned.").append("\n");
                    if (punish.getCategory() != PunishCategory.NONE)
                        msg.append("§cReason: ").append(punish.getCategory().getDisplay(Language.ENGLISH)).append("\n");

                    if (!punish.isPermanent())
                        msg.append("§cExpires in: ").append(DateUtils.formatDifference(punish.getTime())).append("\n");

                    msg.append("§cCan buy unban: ").append(punish.isInexcusable() ? "No" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? "No" : "Yes")).append("\n");

                    msg.append("§cBan ID: #").append(punish.getCode()).append("\n\n");
                    if (!punish.isInexcusable()) {
                        msg.append("§ePurchase your unban in: §bbobmc.com.br/unban\n");
                    }

                    msg.append("§cUnfairly banned? Contact us via: " + Constants.SERVER_WEBSITE + "/appeal").append("\n");
                }

                if (punish.getType() == PunishType.BAN) {
                    broadcast(proxiedPlayer.getServer().getInfo(), account.getUsername(), punish.getCategory() == PunishCategory.CHEATING);
                }

                proxiedPlayer.disconnect(TextComponent.fromLegacyText(msg.toString()));

            } else if (punish.getType() == PunishType.MUTE && punish.getCategory() == PunishCategory.COMMUNITY) {
                if (account.getLanguage() == Language.PORTUGUESE) {
                    proxiedPlayer.sendMessage("§cA sua conta foi mutada por " + punish.getReason() + (punish.isPermanent() ? "." : " expira em " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED) + "." + (punish.isInexcusable() ? " §c§l(NÃO PODE COMPRAR UNMUTE)" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " §c§l(NÃO PODE COMPRAR UNMUTE)" : ""))));
                } else {
                    proxiedPlayer.sendMessage("§cYour account has been muted for " + punish.getReason() + (punish.isPermanent() ? "." : " expires in " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED) + "." + (punish.isInexcusable() ? " §c§l(CAN'T BUY UNMUTE)" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " §c§l(CAN'T BUY UNMUTE)" : ""))));
                }
            }
        }

        async(() -> {

            EmbedBuilder builder = new EmbedBuilder();

            switch (punish.getType()) {
                case BAN:
                    builder.setColor(Color.RED);
                    builder.setAuthor("🔨 BANIDO" + (punish.isInexcusable() ? " ❌" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? " ❌" : "")));
                    builder.setTitle(account.getUsername());
                    builder.addField(new MessageEmbed.Field(":mega: Motivo", punish.getCategory().getDisplay(Language.PORTUGUESE) + " (#" + event.getAccount().count(punish.getType()) + ")", false));
                    if (!punish.isPermanent())
                        builder.addField(":stopwatch: Expira em", StringTimeUtils.formatDifference(StringTimeUtils.Type.SIMPLIFIED, (punish.getTime() + 1000)), false);
                    builder.setThumbnail("https://mineskin.eu/helm/" + account.getUniqueId() + "/256");
                    break;

                case MUTE:
                    builder.setColor(Color.YELLOW);
                    builder.setAuthor("🔇 MUTADO" + (punish.isInexcusable() ? " ❌" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " ❌" : "")));
                    builder.setTitle(account.getUsername());
                    builder.addField(new MessageEmbed.Field(":mega: Motivo", punish.getCategory().getDisplay(Language.PORTUGUESE), false));
                    if (!punish.isPermanent())
                        builder.addField(":stopwatch: Expira em", StringTimeUtils.formatDifference(StringTimeUtils.Type.SIMPLIFIED, (punish.getTime() + 1000)), false);
                    builder.setThumbnail("https://mineskin.eu/helm/" + account.getUniqueId() + "/256");
                    break;
            }

            TextChannel textChannel = ProxyGame.getInstance().getDiscord().getJDA().getTextChannelById("1310230187400761374");

            if (textChannel != null && punish.getType() == PunishType.BAN || punish.getType() == PunishType.MUTE)
                textChannel.sendMessageEmbeds(builder.build()).queue();
        });
    }

    protected void broadcast(ServerInfo server, String username, boolean cheating) {
        for (ProxiedPlayer proxiedPlayer : BungeeCord.getInstance().getPlayers()) {

            Server playerServer = proxiedPlayer.getServer();

            if (playerServer == null)
                continue;

            if (playerServer.getInfo() == server) {
                Account account = Account.fetch(proxiedPlayer.getUniqueId());
                if (account == null)
                    continue;
                if (cheating)
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("commnad.punish.cheating_broadcast", username)));
                else
                    proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("command.punish.community_broadcast")));
            }
        }
    }
}