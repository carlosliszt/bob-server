/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.player;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.handler.AccountEvent;
import org.bukkit.event.Cancellable;

public class PlayerVanishDisableEvent extends AccountEvent implements Cancellable {

    private boolean cancelled, silent;

    public PlayerVanishDisableEvent(Account account, boolean silent) {
        super(account);
        this.silent = silent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public boolean isSilent() {
        return silent;
    }
}
