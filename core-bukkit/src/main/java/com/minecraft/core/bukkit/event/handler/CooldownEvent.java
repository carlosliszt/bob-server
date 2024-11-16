/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.handler;

import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public abstract class CooldownEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    @NonNull
    private Player player;

    @Getter
    @NonNull
    private Cooldown cooldown;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}