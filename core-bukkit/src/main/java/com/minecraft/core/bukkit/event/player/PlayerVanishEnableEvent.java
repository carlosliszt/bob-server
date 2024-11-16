/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.player;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.handler.AccountEvent;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

@Getter
@Setter
public class PlayerVanishEnableEvent extends AccountEvent implements Cancellable {

    private Rank rank;
    private boolean silent;
    private boolean cancelled;

    public PlayerVanishEnableEvent(Account account, Rank rank, boolean silent) {
        super(account);
        this.rank = rank;
        this.silent = silent;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
