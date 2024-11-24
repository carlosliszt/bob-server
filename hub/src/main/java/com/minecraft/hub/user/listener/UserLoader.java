/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hub.user.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.hub.Hub;
import com.minecraft.hub.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserLoader implements Listener, BukkitInterface {

    protected final Hub hub;

    public UserLoader(final Hub hub) {
        this.hub = hub;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(final AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        Account account = Account.fetch(event.getUniqueId());

        if (account == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "unexpected_error");
            return;
        }

        hub.getUserStorage().storeIfAbsent(account);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();

        final User user = hub.getUserStorage().getUser(player.getUniqueId());

        user.setPlayer(player);
        user.setBossbar(hub.getBossbarProvider().getBossbar(player));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        event.setQuitMessage(null);
        hub.getUserStorage().forget(event.getPlayer().getUniqueId());
    }

}