/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserStorage {

    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

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