/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.skywars.user;

import com.minecraft.skywars.Skywars;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserStorage {

    private final Map<UUID, User> users;

    public UserStorage(final Skywars instance) {
        this.users = Collections.synchronizedMap(new HashMap<>());

        instance.getServer().getPluginManager().registerEvents(new UserLoader(instance), instance);
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