/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.game;

import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.hungergames.HungerGames;
import lombok.Getter;

import java.util.List;

public class GameStorage {
    @Getter
    private static final List<Class<?>> games = ClassHandler.getClassesForPackage(HungerGames.getInstance(), "com.minecraft.hungergames.game.list");

    public static Class<?> getGame(String name) {
        return games.stream().filter(c -> c.getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
