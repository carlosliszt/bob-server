/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.bukkit.event.cooldown;

import com.minecraft.core.bukkit.event.handler.CooldownEvent;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@Setter
public class CooldownStartEvent extends CooldownEvent implements Cancellable {

    private boolean cancelled;

    public CooldownStartEvent(Player player, Cooldown cooldown) {
        super(player, cooldown);
    }

}