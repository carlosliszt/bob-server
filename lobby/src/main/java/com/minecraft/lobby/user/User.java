/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.user;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.duel.Challenge;
import com.minecraft.lobby.hall.Hall;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class User {

    private final UUID uniqueId;
    private final String name;

    private final Account account;

    private Player player;
    @Setter
    private Hall hall;
    @Setter
    private GameScoreboard scoreboard;
    @Setter
    private Selector selector;
    @Getter
    private final Set<Challenge> challenges;

    public User(Account account) {
        this.account = account;

        this.uniqueId = account.getUniqueId();
        this.name = account.getUsername();
        this.challenges = new HashSet<>();
    }

    public Player getPlayer() {
        if (this.player == null)
            this.player = Bukkit.getPlayer(getUniqueId());
        return player;
    }

    public void handleSidebar() {
        getHall().handleSidebar(this);
    }

    public static User fetch(UUID uuid) {
        return Lobby.getLobby().getUserStorage().getUser(uuid);
    }

    public boolean alreadyInvited(UUID uuid, DuelType duelType) {
        challenges.removeIf(Challenge::expired);
        return getChallenges().stream().anyMatch(c -> c.getReceiver().getUniqueId().equals(uuid) && duelType == c.getDuelType());
    }
}