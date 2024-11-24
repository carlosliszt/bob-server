/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.player;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.handler.AccountEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

@Getter
@Setter
public class PlayerTeamAssignEvent extends AccountEvent {

    private Player viewer;
    private Team team;

    public PlayerTeamAssignEvent(Account account, Player viewer, Team team) {
        super(account);
        this.team = team;
        this.viewer = viewer;
    }
}
