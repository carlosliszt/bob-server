/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;

public abstract class PlayerProtectionEvent extends PlayerEvent implements Cancellable {

    @Getter
    @Setter
    private boolean cancelled;

    public PlayerProtectionEvent(Player who) {
        super(who);
    }
}