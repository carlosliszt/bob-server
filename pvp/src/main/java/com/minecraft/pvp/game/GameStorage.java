/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.game;

import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.pvp.PvP;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class GameStorage {

    private final List<Game> games = new ArrayList<>();

    public GameStorage() {
        try {
            for (Class<?> clazz : ClassHandler.getClassesForPackage(PvP.getPvP(), "com.minecraft.pvp.game.types")) {
                if (Game.class.isAssignableFrom(clazz)) {
                    Game game = (Game) clazz.newInstance();
                    getGames().add(game);
                    Bukkit.getPluginManager().registerEvents(game, PvP.getPvP());
                    game.loadVariables();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        PvP.getEngine().getBukkitFrame().registerAdapter(Game.class, this::getGameByRoute);
    }

    public List<Game> getGames() {
        return games;
    }

    public Game getGameByRoute(String route) {
        return getGames().stream().filter(game -> game.getName().equals(route)).findFirst().orElse(null);
    }

    public Game getGameByWorld(World world) {
        return getGames().stream().filter(game -> game.getWorld().getUID().equals(world.getUID())).findFirst().orElse(null);
    }

    public Game getGame(Class<?> clazz) {
        return getGames().stream().filter(game -> game.getClass().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())).findFirst().orElse(null);
    }
}