/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerProtectionRemoveEvent extends PlayerProtectionEvent {

    @Getter
    public static final HandlerList handlerList = new HandlerList();

    public PlayerProtectionRemoveEvent(Player who) {
        super(who);
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

}