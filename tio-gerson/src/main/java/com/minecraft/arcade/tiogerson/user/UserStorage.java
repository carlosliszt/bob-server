/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.tiogerson.user;

import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.user.loader.UserLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserStorage {

    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    public void start(ArcadeMain arcadeMain) {
        arcadeMain.getServer().getPluginManager().registerEvents(new UserLoader(), arcadeMain);
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