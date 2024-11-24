/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hub.user.storage;

import com.minecraft.core.account.Account;
import com.minecraft.hub.user.User;
import lombok.Getter;

import java.util.*;

@Getter
public class UserStorage {

    private final Map<UUID, User> users = Collections.synchronizedMap(new HashMap<>());

    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    public void forget(UUID uniqueId) {
        users.remove(uniqueId);
    }

    public User storeIfAbsent(Account account) {
        return this.users.computeIfAbsent(account.getUniqueId(), user -> new User(account)).setAccount(account);
    }

    public Collection<User> getUsers() {
        return users.values();
    }

}