/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hub.util.lobby;

import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.hub.Hub;

import java.util.List;

public class LobbyStorage {

    private static final List<Class<?>> games = ClassHandler.getClassesForPackage(Hub.getInstance(), "com.minecraft.hub.lobby");

    public static Class<?> getHall(String name) {
        return games.stream().filter(c -> c.getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}