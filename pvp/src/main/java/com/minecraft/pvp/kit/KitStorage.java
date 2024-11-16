/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit;

import com.minecraft.core.bukkit.util.map.DuplicateMap;
import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.kit.list.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class KitStorage {

    private final List<Kit> kits = new ArrayList<>();
    private final DuplicateMap<Class<? extends Kit>, Class<? extends Kit>> blockedCombinations = new DuplicateMap<>();

    public KitStorage() {
        getKits().add(new Nenhum());
        try {
            for (Class<?> clazz : ClassHandler.getClassesForPackage(PvP.getPvP(), "com.minecraft.pvp.kit.list")) {
                if (clazz.getSimpleName().equalsIgnoreCase("Nenhum"))
                    continue;
                if (Kit.class.isAssignableFrom(clazz)) {
                    Kit kit = (Kit) clazz.newInstance();
                    getKits().add(kit);
                    Bukkit.getPluginManager().registerEvents(kit, PvP.getPvP());
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        getBlockedCombinations().put(Kangaroo.class, Stomper.class);
        getBlockedCombinations().put(Grappler.class, Stomper.class);
        getBlockedCombinations().put(Flash.class, Stomper.class);
        getBlockedCombinations().put(Ninja.class, Stomper.class);
        getBlockedCombinations().put(AntiTower.class, Stomper.class);
        getBlockedCombinations().put(Neo.class, Stomper.class);
        getBlockedCombinations().put(Neo.class, Ninja.class);
        getBlockedCombinations().put(Fisherman.class, Neo.class);
        getBlockedCombinations().put(Switcher.class, Neo.class);
        getBlockedCombinations().put(Archer.class, Neo.class);
        getBlockedCombinations().put(Hulk.class, Grappler.class);
        getBlockedCombinations().put(Flash.class, Switcher.class);
    }

    public boolean isBlocked(Class<? extends Kit> kit, Class<? extends Kit> kit2) {
        if (getBlockedCombinations().containsKey(kit) && getBlockedCombinations().get(kit).contains(kit2)) {
            return true;
        } else return getBlockedCombinations().containsKey(kit2) && getBlockedCombinations().get(kit2).contains(kit);
    }

    public Kit getKit(String name) {
        return getKits().stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Kit> getKits() {
        return kits;
    }

    public DuplicateMap<Class<? extends Kit>, Class<? extends Kit>> getBlockedCombinations() {
        return blockedCombinations;
    }

}