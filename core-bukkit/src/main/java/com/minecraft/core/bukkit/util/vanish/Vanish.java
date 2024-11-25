/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.vanish;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.friend.Friend;
import com.minecraft.core.account.friend.FriendStatus;
import com.minecraft.core.account.friend.status.FriendStatusUpdate;
import com.minecraft.core.bukkit.event.player.PlayerHideEvent;
import com.minecraft.core.bukkit.event.player.PlayerShowEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Vanish {

    @Getter
    private static final Vanish instance = new Vanish();
    @Getter
    private final HashMap<UUID, Rank> playerVanish;

    public Vanish() {
        this.playerVanish = new HashMap<>();
    }

    public void setVanished(Player player, Rank rank) {
        setVanished(player, rank, true);
    }

    public void setVanished(Player player, Rank rank, boolean silent) {

        Account account = Account.fetch(player.getUniqueId());

        if (account == null)
            return;

        if (rank != null) {
            PlayerVanishEnableEvent event = new PlayerVanishEnableEvent(account, rank, silent);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {

                playerVanish.put(player.getUniqueId(), event.getRank());

                for (Player everyone : Bukkit.getOnlinePlayers()) {

                    Account acc = Account.fetch(everyone.getUniqueId());

                    if (acc == null)
                        continue;

                    if (acc.getRank().getCategory().getImportance() < rank.getCategory().getImportance()) {

                        PlayerHideEvent playerHideEvent = new PlayerHideEvent(player, everyone);
                        playerHideEvent.fire();

                        if (!event.isCancelled())
                            everyone.hidePlayer(player);
                    } else {

                        if (!everyone.getWorld().getUID().equals(player.getWorld().getUID()))
                            continue;

                        PlayerShowEvent playerShowEvent = new PlayerShowEvent(player, everyone);
                        playerShowEvent.fire();

                        if (!event.isCancelled())
                            everyone.showPlayer(player);
                    }
                }

                player.setGameMode(GameMode.CREATIVE);

                if (silent) {
                    FriendStatusUpdate friendStatusUpdate = FriendStatusUpdate.builder()
                            .holder(new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis()))
                            .status(FriendStatus.SILENTVANISH)
                            .update(FriendStatusUpdate.Update.STATUS)
                            .build();
                    Constants.getRedis().publish(Redis.FRIEND_UPDATE_CHANNEL, Constants.GSON.toJson(friendStatusUpdate));

                } else {
                    FriendStatusUpdate friendStatusUpdate = FriendStatusUpdate.builder()
                            .holder(new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis()))
                            .status(FriendStatus.VANISHED)
                            .update(FriendStatusUpdate.Update.STATUS)
                            .build();
                    Constants.getRedis().publish(Redis.FRIEND_UPDATE_CHANNEL, Constants.GSON.toJson(friendStatusUpdate));
                }

                if (event.getRank().getCategory() == Rank.Category.NONE)
                    player.sendMessage(account.getLanguage().translate("command.vanish.visible"));

                if (!event.isSilent() && event.getRank().getCategory() != Rank.Category.NONE) {
                    player.sendMessage(account.getLanguage().translate("command.vanish.enable", "vanish"));
                    player.sendMessage(account.getLanguage().translate("command.vanish.enable_info", rank.getCategory().getDisplay()));
                }
            }
        } else {
            PlayerVanishDisableEvent event = new PlayerVanishDisableEvent(account, silent);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                playerVanish.remove(player.getUniqueId());

                for (Player everyone : player.getWorld().getPlayers()) {
                    PlayerShowEvent playerShowEvent = new PlayerShowEvent(player, everyone);
                    playerShowEvent.fire();

                    if (!event.isCancelled())
                        everyone.showPlayer(player);
                }

                player.setGameMode(GameMode.SURVIVAL);

                FriendStatusUpdate friendStatusUpdate = FriendStatusUpdate.builder()
                        .holder(new Friend(account.getUsername(), account.getUniqueId(), System.currentTimeMillis()))
                        .status(FriendStatus.ONLINE)
                        .update(FriendStatusUpdate.Update.STATUS)
                        .build();

                Constants.getRedis().publish(Redis.FRIEND_UPDATE_CHANNEL, Constants.GSON.toJson(friendStatusUpdate));

                if (!event.isSilent()) {
                    player.sendMessage(account.getLanguage().translate("command.vanish.disable", "vanish"));
                    player.sendMessage(account.getLanguage().translate("command.vanish.disable_info"));
                }
            }
        }
    }

    public Rank getRank(UUID uuid) {
        return playerVanish.get(uuid);
    }

    public Rank getRank(Player player) {
        return playerVanish.get(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return playerVanish.containsKey(uuid);
    }

    public boolean isVanished(Account account) {
        return playerVanish.containsKey(account.getUniqueId());
    }

    public boolean isVanishedToRankExact(UUID uniqueId, Rank rank) {
        return playerVanish.get(uniqueId).getCategory().getImportance() == rank.getCategory().getImportance();
    }

    public boolean isVanishedToCategory(UUID uniqueId, Rank.Category category) {
        return playerVanish.get(uniqueId).getCategory().getImportance() == category.getImportance();
    }
}
