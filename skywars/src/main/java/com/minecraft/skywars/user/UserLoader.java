/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.skywars.user;

import com.minecraft.core.account.Account;
import com.minecraft.skywars.Skywars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class UserLoader implements Listener {

    private final Skywars instance;

    public UserLoader(final Skywars instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(final AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        final UUID uniqueId = event.getUniqueId();

        final User user = new User(Account.fetch(uniqueId));

        instance.getUserStorage().store(uniqueId, user);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(final PlayerQuitEvent event) {
        event.setQuitMessage(null);

        instance.getUserStorage().forget(event.getPlayer().getUniqueId());
    }

}