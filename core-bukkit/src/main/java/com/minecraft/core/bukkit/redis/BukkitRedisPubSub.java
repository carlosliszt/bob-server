/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.redis;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.event.server.RedisPubSubEvent;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.util.communication.AccountRankUpdateData;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.sql.SQLException;
import java.util.UUID;

public class BukkitRedisPubSub extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {

        if (channel.equals(Redis.RANK_UPDATE_CHANNEL)) {

            AccountRankUpdateData data = Constants.GSON.fromJson(message, AccountRankUpdateData.class);

            Account account = Account.fetch(data.getUniqueId());

            if (account == null)
                return;

            if (data.getAction() == AccountRankUpdateData.Action.REMOVE) {
                account.removeRank(data.getRank());
            } else if (data.getAction() == AccountRankUpdateData.Action.ADD) {
                account.giveRank(data.getRank(), data.getExpiration(), data.getAuthor());
            } else {
                account.removeRank(data.getRank());
                account.giveRank(data.getRank(), data.getExpiration(), data.getAuthor(), data.getAddedAt(), data.getUpdatedAt());
            }

            Player player = Bukkit.getPlayer(account.getUniqueId());

            if (player != null)
                player.setOp(account.hasPermission(Rank.TRIAL_MODERATOR));

            account.loadRanks();
            account.getTagList().loadTags();

            account.setProperty("account_tag", account.getTagList().getHighestTag());

            if (player != null)
                Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, account.getTagList().getHighestTag(), account.getProperty("account_prefix_type", PrefixType.DEFAULT).getAs(PrefixType.class)));
        } else if(channel.equals(Redis.NICK_DISGUISE_CHANNEL)) {
            String[] args = message.split(":");

            Account account = Account.fetch(UUID.fromString(args[0]));

            Player player = Bukkit.getPlayer(account.getUniqueId());

            if(player != null) {
                Property property = new Property("textures", account.getSkinData().getValue(), account.getSkinData().getSignature());
                PlayerDisguise.disguise(player, args[1], property, true);
                PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, Tag.fromUniqueCode(account.getData(Columns.TAG).getAsString()), account.getProperty("account_prefix_type").getAs(PrefixType.class));
                Bukkit.getPluginManager().callEvent(event);
                if(args[1] == account.getUsername()) {
                    account.removeProperty("nickname");
                } else {
                    account.setDisplayName(args[1]);
                }
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

            clan.getMembers().forEach(member -> {

                Player player = Bukkit.getPlayer(member.getUniqueId());

                if (player == null) {
                    return;
                }

                Account account = Account.fetch(player.getUniqueId());

                new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class)).fire();
            });

        } else if (channel.equals(Redis.FLAG_UPDATE_CHANNEL)) {
            String[] args = message.split(":");

            Account account = Account.fetch(UUID.fromString(args[0]));

            if (account == null)
                return;

            account.setFlags(Integer.parseInt(args[1]));
        } else {
            Bukkit.getPluginManager().callEvent(new RedisPubSubEvent(channel, message));
        }
    }
}