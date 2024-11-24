/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.game.util;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.game.Game;
import com.minecraft.core.bukkit.arcade.game.GameType;
import com.minecraft.core.bukkit.listener.SoupListener;
import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GameStorage {

    private final Set<Game> games;
    private final String[] worldGames = new String[]{"arena", "fps", "lava"};

    public GameStorage() {
        this.games = new HashSet<>();
    }

    public void onEnable() {
        this.setupWorlds();

        final Plugin plugin = PvP.getInstance();

        try {
            for (Class<?> clazz : ClassHandler.getClassesForPackage(plugin, "com.minecraft.arcade.pvp.game.list")) {
                if (clazz.getSimpleName().equalsIgnoreCase("Damage"))
                    continue;
                if (Game.class.isAssignableFrom(clazz)) {
                    final Game game = (Game) clazz.newInstance();

                    game.getWorld().setMetadata("game", new FixedMetadataValue(PvP.getInstance(), game));

                    game.loadVariables();

                    plugin.getServer().getPluginManager().registerEvents(game, plugin);

                    getGames().add(game);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        new SoupListener(plugin);
    }

    protected void setupWorlds() {
        for (final String worldName : getWorldGames()) {
            final WorldCreator worldCreator = new WorldCreator(worldName);

            worldCreator.generateStructures(false);
            worldCreator.type(WorldType.FLAT);

            final World world = Bukkit.getServer().createWorld(worldCreator);

            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("mobGriefing", "false");

            world.setAutoSave(false);
            world.setStorm(false);
            world.setThundering(false);

            world.setWeatherDuration(Integer.MIN_VALUE);
            world.setThunderDuration(Integer.MIN_VALUE);

            world.setTime(6000);

            world.getEntities().forEach(Entity::remove);

            world.setSpawnLocation(0, 70, 0);
        }
    }

    public Game getGame(final GameType gameType) {
        return this.games.stream().filter(game -> game.getType() == gameType).findFirst().orElse(null);
    }

}