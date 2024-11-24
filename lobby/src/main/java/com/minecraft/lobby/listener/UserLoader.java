/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInitialSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.github.paperspigot.Title;

import java.util.UUID;

public class UserLoader implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        UUID uniqueId = event.getUniqueId();

        Account account = Account.fetch(uniqueId);

        if (account == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "unexpected_error");
            return;
        }

        User user = new User(account);
        user.setHall(Lobby.getLobby().getHall());

        Lobby.getLobby().getUserStorage().store(uniqueId, user);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Hall hall = user.getHall();

        user.setScoreboard(new GameScoreboard(player));

        hall.join(user);
        hall.handleSidebar(user);

        Account account = user.getAccount();

        if (account.hasPermission(Rank.PARTNER_PLUS))
            Vanish.getInstance().setVanished(event.getPlayer(), account.getRank());

        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
        boolean announcePlayerJoin = tag.isBetween(Tag.PARTNER_PLUS, Tag.MEMBER) && !Vanish.getInstance().isVanished(user.getUniqueId());

        if (announcePlayerJoin) {
            hall.getUsers().forEach(recipient -> {
                Account account_recipient = recipient.getAccount();

                if (account_recipient == null)
                    return;

                PrefixType prefixType = account_recipient.getProperty("account_prefix_type", PrefixType.DEFAULT).getAs(PrefixType.class);
                recipient.getPlayer().sendRawMessage(prefixType.getFormatter().format(tag).replace("#", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+") + account.getDisplayName() + " §6entrou no lobby!");
            });
        }

        player.sendTitle(new Title("§b§l" + Constants.SERVER_NAME.toUpperCase(), account.getLanguage().translate("lobby.welcome_title"), 8, 15, 12));

        hall.handleNPCs(user);
    }

    @EventHandler
    public void onDefineSpawn(PlayerInitialSpawnEvent event) {
        User user = User.fetch(event.getPlayer().getUniqueId());
        Location location = user.getHall().getSpawn();
        event.setSpawnLocation(user.getAccount().hasPermission(Rank.VIP) ? location.clone().add(0, 3, 0) : location);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());
        user.getHall().quit(user);
        Lobby.getLobby().getUserStorage().forget(player.getUniqueId());
    }

}