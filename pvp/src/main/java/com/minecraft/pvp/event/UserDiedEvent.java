/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.event;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.user.User;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public class UserDiedEvent extends ServerEvent {

    private final User killed, killer;
    private final List<ItemStack> drops;
    private final Location location;
    private final Reason reason;
    private final Game game;

    public UserDiedEvent(User killed, User killer, List<ItemStack> drops, Location location, Reason reason, Game game) {
        this.killed = killed;
        this.killer = killer;
        this.drops = drops;
        this.location = location;
        this.reason = reason;
        this.game = game;
    }

    public boolean hasKiller() {
        return killer != null;
    }

    public boolean inSameGame() {
        return killed.getGame().equals(killer.getGame());
    }

    public enum Reason {
        LOGOUT, KILL;
    }

}