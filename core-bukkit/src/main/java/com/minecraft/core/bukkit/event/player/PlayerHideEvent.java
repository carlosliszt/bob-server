/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.player;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
public class PlayerHideEvent extends ServerEvent implements Cancellable {

    private Player tohide;
    private Player receiver;
    @Setter
    private boolean cancelled;

    public PlayerHideEvent(Player tohide, Player receiver) {
        this.tohide = tohide;
        this.receiver = receiver;
    }

}
