/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
package com.minecraft.core.bukkit.event.handler;

import com.minecraft.core.account.Account;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Account account;

    public AccountEvent(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Event fire() {
        Bukkit.getPluginManager().callEvent(this);
        return this;
    }


}