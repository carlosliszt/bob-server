/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.player;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.handler.AccountEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlayerCommandTabCompleteEvent extends AccountEvent implements Cancellable {

    private String message;
    private List<String> completerList = new ArrayList<>();
    private boolean cancelled = false;
    private boolean filterProxy;

    public PlayerCommandTabCompleteEvent(Account account, String message, boolean filterProxy) {
        super(account);
        this.message = message;
        this.filterProxy = filterProxy;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
