/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.redis;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.account.friend.Friend;
import com.minecraft.core.account.friend.status.FriendStatusUpdate;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.clan.service.ClanService;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.event.RedisPubSubEvent;
import com.minecraft.core.proxy.server.ProxyServerStorage;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.proxy.util.player.SkinChanger;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.server.packet.ServerPayload;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.communication.AccountRankUpdateData;
import com.minecraft.core.util.communication.NicknameUpdateData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.awt.*;
import java.sql.SQLException;
import java.util.UUID;

public class ProxyRedisPubSub extends JedisPubSub implements ProxyInterface {

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(Redis.SERVER_COMMUNICATION_CHANNEL)) {

            ServerPayload serverPayload = Constants.GSON.fromJson(message, ServerPayload.class);

            ProxyServerStorage proxyserverStorage = (ProxyServerStorage) ProxyGame.getInstance().getServerStorage();

            Server server = proxyserverStorage.getServer(serverPayload);

            if (server == null) {
                proxyserverStorage.getServers().add(server = new Server(proxyserverStorage.getNameOf(serverPayload.getPort()), serverPayload.getPort(), serverPayload, serverPayload.getServerType(), serverPayload.getServerCategory()));
            }
            server.setLastBreath(serverPayload);

            if (server.getServerCategory() == ServerCategory.UNKNOWN)
                server.setServerCategory(serverPayload.getServerCategory());

            if (server.getServerType() == ServerType.UNKNOWN)
                server.setServerType(serverPayload.getServerType());

            if (!proxyserverStorage.getServers().contains(server)) {
                proxyserverStorage.getServers().add(server);
            }

        } else if (channel.equals(Redis.FRIEND_UPDATE_CHANNEL)) {

            FriendStatusUpdate update = Constants.GSON.fromJson(message, FriendStatusUpdate.class);

            switch (update.getUpdate()) {
                case STATUS:

                    Account holderAccount = Account.fetch(update.getHolder().getUniqueId());

                    switch (update.getStatus()) {
                        case ONLINE:
                            holderAccount.getData(Columns.FRIEND_STATUS).setData(update.getStatus().name());
                            for (Friend friend : holderAccount.getFriends()) {
                                ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(friend.getUniqueId());
                                if (proxiedPlayer != null) {
                                    proxiedPlayer.sendMessage("§6[AMIGOS]§e " + holderAccount.getRank().getDefaultTag().getFormattedColor() + holderAccount.getUsername() + " §eentrou!");
                                }
                            }

                            async(() -> holderAccount.getDataStorage().saveTable(Tables.ACCOUNT));

                            break;
                        case OFFLINE:
                            holderAccount.getData(Columns.FRIEND_STATUS).setData(update.getStatus().name());
                            for (Friend friend : holderAccount.getFriends()) {
                                ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(friend.getUniqueId());
                                if (proxiedPlayer != null) {
                                    proxiedPlayer.sendMessage("§6[AMIGOS]§e " + holderAccount.getRank().getDefaultTag().getFormattedColor() + holderAccount.getUsername() + " §esaiu!");
                                }
                            }

                            async(() -> holderAccount.getDataStorage().saveTable(Tables.ACCOUNT));

                            break;

                        case VANISHED:
                        case SILENTVANISH:
                            holderAccount.getData(Columns.FRIEND_STATUS).setData(update.getStatus().name());
                            async(() -> holderAccount.getDataStorage().saveTable(Tables.ACCOUNT));
                            break;
                    }
                    break;

            }

        } else if (channel.equals(Redis.CLAN_TAG_UPDATE)) {

            String[] parsed = message.split(":");
            String clanId = parsed[0];
            String tagColor = parsed[1];

            Clan clan = Constants.getClanService().getClan(Integer.parseInt(clanId));
            clan.setColor(tagColor);

            try {
                Constants.getClanService().pushClan(clan);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } else if (channel.equals(Redis.NICK_DISGUISE_CHANNEL)) {
            String[] parsed = message.split(":");
            UUID uuid = UUID.fromString(parsed[0]);
            String name = parsed[1];

            Account account = Account.fetch(uuid);

            if (name.equals(account.getUsername()))
                account.removeProperty("nickname");
            else {
                account.setDisplayName(name);
                account.getData(Columns.LAST_NICK).setData(name);
            }

        } else if (channel.equals(Redis.LANGUAGE_UPDATE_CHANNEL)) {
            String[] parsed = message.split(":");
            UUID uuid = UUID.fromString(parsed[0]);
            Language language = Language.fromUniqueCode(parsed[1]);

            Account account = Account.fetch(uuid);
            account.setLanguage(language);
        } else if(channel.equals(Redis.NICK_ADD_CHANNEL)) {
            NicknameUpdateData data = Constants.GSON.fromJson(message, NicknameUpdateData.class);

            Account account = Account.fetch(data.getUniqueId());
            account.addNick(data.getNickname(), data.getChangedAt(), data.getExpiry());

        } else if (channel.equals(Redis.SKIN_CHANGE_CHANNEL)) {
            String[] split = message.split(":");
            UUID uuid = UUID.fromString(split[0]);
            String value = split[1], signature = split[2];

            SkinData skinData = Account.fetch(uuid).getSkinData();

            skinData.setValue(value);
            skinData.setSignature(signature);

            SkinChanger.getInstance().changeTexture(BungeeCord.getInstance().getPlayer(uuid).getPendingConnection(), value, signature);

        } else if (channel.equals(Redis.OPEN_EVENT_CHANNEL)) {
            String finalMessage = "§b§l" + Constants.SERVER_NAME.toUpperCase() + " §7» §r" + message.trim();
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(finalMessage));
        } else if (channel.equals(Redis.PREFERENCES_UPDATE_CHANNEL)) {
            String[] split = message.split(":");

            UUID uuid = UUID.fromString(split[0]);
            int preferences = Integer.parseInt(split[1]);

            Account account = Account.fetch(uuid);
            account.setPreferences(preferences);
        } else {
            ProxyGame.getInstance().getProxy().getPluginManager().callEvent(new RedisPubSubEvent(channel, message));
        }
    }
}