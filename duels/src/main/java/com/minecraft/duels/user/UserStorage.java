/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.duels.user;

import com.minecraft.duels.Duels;
import com.minecraft.duels.user.loader.UserLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserStorage {

    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    public void start(Duels duels) {
        duels.getServer().getPluginManager().registerEvents(new UserLoader(), duels);
    }

    public void store(UUID uuid, User account) {
        getUsers().put(uuid, account);
    }

    public void forget(UUID uniqueId) {
        getUsers().remove(uniqueId);
    }

    public User getUser(UUID uuid) {
        return getUsers().get(uuid);
    }

    public Map<UUID, User> getUsers() {
        return users;
    }

}