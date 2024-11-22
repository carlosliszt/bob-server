package com.minecraft.core.proxy.staff;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffStorage {

    private final Map<UUID, Staffer> users = Collections.synchronizedMap(new HashMap<>());

    public void store(UUID uuid, Staffer account) {
        getUsers().put(uuid, account);
    }

    public void forget(UUID uniqueId) {
        getUsers().remove(uniqueId);
    }

    public Staffer getUser(UUID uuid) {
        return getUsers().get(uuid);
    }

    public Map<UUID, Staffer> getUsers() {
        return users;
    }


}
