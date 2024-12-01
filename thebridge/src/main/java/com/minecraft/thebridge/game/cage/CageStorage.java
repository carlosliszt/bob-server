package com.minecraft.thebridge.game.cage;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.server.thebridge.BridgeCageList;
import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.thebridge.TheBridge;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CageStorage {

    private final List<Cage> cages = new ArrayList<>();
    private final Cage defaultCage;

    public CageStorage() {

        for (Class<?> classes : ClassHandler.getClassesForPackage(TheBridge.getInstance(), "com.minecraft.thebridge.game.cage.list")) {
            if (Cage.class.isAssignableFrom(classes)) {
                try {
                    Cage cage = (Cage) classes.getConstructor(TheBridge.class).newInstance(TheBridge.getInstance());
                    cages.add(cage);
                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.shutdown();
                }
            }
        }

        defaultCage = getCages().stream().filter(cage -> cage.getDisplayName().equals("Padrão")).findFirst().orElse(null);
    }

    public void loadCages() {
        getCages().forEach(Cage::parse);
        try (Jedis redis = Constants.getRedis().getResource(Redis.SERVER_CACHE)) {
            BridgeCageList cageList = new BridgeCageList(getCages().stream().map(Cage::getCageConfig).filter(bridgeCageConfig -> !bridgeCageConfig.getDisplayName().equals("Padrão")).collect(Collectors.toList()));
            redis.setex("bridge.cages", 86400, Constants.GSON.toJson(cageList));
        }
    }

    public Cage getCage(final String name) {
        return cages.stream().filter(cage -> cage.getDisplayName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Cage getDefaultCage() {
        return defaultCage.clone();
    }

    public List<Cage> getCages() {
        return cages;
    }
}