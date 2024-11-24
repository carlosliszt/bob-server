/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.skywars.user;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.skywars.Skywars;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class User {

    private final Account account;

    private Player player;

    private GameScoreboard scoreboard;

    public User(final Account account) {
        this.account = account;
    }

    public UUID getUniqueId() {
        return getAccount().getUniqueId();
    }

    public String getName() {
        return getAccount().getDisplayName();
    }

    public static User fetch(final UUID uuid) {
        return Skywars.getInstance().getUserStorage().getUser(uuid);
    }

}