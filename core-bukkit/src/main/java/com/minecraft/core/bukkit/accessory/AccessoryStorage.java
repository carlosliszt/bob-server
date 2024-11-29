package com.minecraft.core.bukkit.accessory;

import com.minecraft.core.bukkit.server.duels.DuelType;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class AccessoryStorage {
    private final Map<String, Accessory> accessoryList = new HashMap<>();

    public List<Accessory> getAccessories(AccessoryType accessoryType) {
        return accessoryList.values().stream().filter(accessory -> accessory.getType().equals(accessoryType)).collect(Collectors.toList());
    }

    public void register(Accessory... accessories) {
        for (Accessory accessory : accessories) {
            accessoryList.put(accessory.getName().toLowerCase(), accessory);
            accessory.load();
        }
    }

    public void unregister() {
        accessoryList.clear();
        System.out.println("[CACHE] Cleared accessories list loadeds.");
    }
}
