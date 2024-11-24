/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.storage;

import com.minecraft.core.account.Account;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.listener.UserLoader;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserStorage {

    private Map<UUID, User> users;
    private final HungerGames hungerGames;

    public UserStorage(HungerGames hungerGames) {
        this.hungerGames = hungerGames;
    }

    public UserStorage enable() {
        getHungerGames().getLogger().info("Loading " + getClass().getSimpleName() + "...");
        Bukkit.getPluginManager().registerEvents(new UserLoader(), getHungerGames());
        this.users = Collections.synchronizedMap(new HashMap<>());
        getHungerGames().getLogger().info(getClass().getSimpleName() + " loaded successfully!");
        return this;
    }

    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public User storeIfAbsent(Account account) {
        return this.users.computeIfAbsent(account.getUniqueId(), user -> new User(account)).setAccount(account);
    }

    public void forget(UUID uuid) {
        users.remove(uuid);
    }

    public List<User> getAliveUsers() {
        return getUsers().stream().filter(c -> c.isOnline() && c.isAlive()).collect(Collectors.toList());
    }

    public Collection<User> getAwayUsers() {
        return getUsers().stream().filter(c -> c.isAlive() && c.getAwaySession() != null).collect(Collectors.toList());
    }
}
