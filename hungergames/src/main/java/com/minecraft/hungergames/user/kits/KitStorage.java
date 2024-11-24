/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.map.DuplicateMap;
import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.list.*;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public class KitStorage implements VariableStorage {

    private List<Kit> kits;
    private DuplicateMap<Class<? extends Kit>, Class<? extends Kit>> blockedCombinations;
    private final HungerGames hungerGames;

    @Variable(name = "hg.kit.default", permission = Rank.EVENT_MOD)
    private Kit defaultKit;
    @Variable(name = "hg.kit.cooldown", permission = Rank.EVENT_MOD)
    private boolean cooldown;
    @Variable(name = "hg.kit.free", permission = Rank.EVENT_MOD)
    private boolean freeKits;
    @Variable(name = "hg.kit.block_combinations", permission = Rank.EVENT_MOD)
    private boolean blockCombinations;

    public KitStorage(HungerGames hungerGames) {
        this.hungerGames = hungerGames;
    }

    public KitStorage enable() {
        getHungerGames().getLogger().info("Loading " + getClass().getSimpleName() + "...");
        kits = new ArrayList<>();
        blockedCombinations = new DuplicateMap<>();
        load();
        this.defaultKit = kits.get(0);
        this.cooldown = true;
        this.blockCombinations = true;
        loadVariables();
        loadBlockedCombinations();
        getHungerGames().getLogger().info(getClass().getSimpleName() + " loaded successfully!");
        return this;
    }

    public Kit getRandomKit(KitCategory kitCategory) {
        List<Kit> kitList = new ArrayList<>();

        for (Kit kit : getKits()) {
            if (!kit.isActive())
                continue;
            if (kit.getKitCategory() != kitCategory)
                continue;
            kitList.add(kit);
        }

        return kitList.get(Constants.RANDOM.nextInt((kitList.size() - 1)));
    }

    private void load() {
        kits.add(new Nenhum(hungerGames));
        for (Class<?> classes : ClassHandler.getClassesForPackage(HungerGames.getInstance(), "com.minecraft.hungergames.user.kits.list")) {
            if (Kit.class.isAssignableFrom(classes) && classes != Nenhum.class) {
                try {
                    Kit kit = (Kit) classes.getConstructor(HungerGames.class).newInstance(hungerGames);
                    hungerGames.getLogger().info("Loading the kit " + kit.getName() + "!");
                    kits.add(kit);
                    kit.loadVariables();
                } catch (Exception e) {
                    e.printStackTrace();
                    hungerGames.getLogger().log(Level.SEVERE, "Kit " + classes.getSimpleName() + " did not loaded properly due to an error. (" + e.getMessage() + ")");
                    Bukkit.shutdown();
                }
            }
        }
    }

    public void register() {
        for (Kit kit : getKits()) {
            if (kit.isActive())
                kit.register();
        }
    }

    public void unregister() {
        for (Kit kit : getKits()) {
            kit.unregister();
        }
    }

    public Kit getKit(String name) {
        return getKits().stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Kit getKit(Class<? extends Kit> clazz) {
        return getKits().stream().filter(kit -> kit.getClass() == clazz).findFirst().orElse(null);
    }

    public boolean isBlocked(Class<? extends Kit> kit, Class<? extends Kit> kit2) {
        return blockCombinations && getBlockedCombinations().containsKey(kit) && getBlockedCombinations().get(kit).contains(kit2) || blockCombinations && getBlockedCombinations().containsKey(kit2) && getBlockedCombinations().get(kit2).contains(kit);
    }

    public void loadBlockedCombinations() {
        getBlockedCombinations().put(Anchor.class, Neo.class);
        getBlockedCombinations().put(Demoman.class, Tank.class);
        getBlockedCombinations().put(Demoman.class, Turtle.class);
        getBlockedCombinations().put(Demoman.class, Switcher.class);
        getBlockedCombinations().put(Hulk.class, Phantom.class);
        getBlockedCombinations().put(Fisherman.class, Turtle.class);
        getBlockedCombinations().put(Fisherman.class, Demoman.class);
        getBlockedCombinations().put(Fisherman.class, Launcher.class);
        getBlockedCombinations().put(Fisherman.class, Poseidon.class);
        getBlockedCombinations().put(Fisherman.class, Jackhammer.class);
        getBlockedCombinations().put(Fisherman.class, Phantom.class);
        getBlockedCombinations().put(Stomper.class, Kangaroo.class);
        getBlockedCombinations().put(Stomper.class, Launcher.class);
        getBlockedCombinations().put(Stomper.class, Grappler.class);
        getBlockedCombinations().put(Stomper.class, Phantom.class);
        getBlockedCombinations().put(Stomper.class, Blink.class);
        getBlockedCombinations().put(Stomper.class, Ninja.class);
        getBlockedCombinations().put(Stomper.class, Hulk.class);
        getBlockedCombinations().put(Stomper.class, Assassin.class);
        getBlockedCombinations().put(Urgal.class, Hulk.class);
        getBlockedCombinations().put(Stomper.class, Chameleon.class);
        getBlockedCombinations().put(Stomper.class, Flash.class);
        getBlockedCombinations().put(Stomper.class, Checkpoint.class);
        getBlockedCombinations().put(Endermage.class, Checkpoint.class);
        getBlockedCombinations().put(Endermage.class, Stomper.class);
        getBlockedCombinations().put(Endermage.class, Blink.class);
        getBlockedCombinations().put(Stomper.class, Jumper.class);
        getBlockedCombinations().put(Gladiator.class, Ninja.class);
        getBlockedCombinations().put(Kangaroo.class, Ninja.class);
        getBlockedCombinations().put(Archer.class, Timelord.class);
        getBlockedCombinations().put(Demoman.class, Timelord.class);
        getBlockedCombinations().put(Gladiator.class, Timelord.class);
        getBlockedCombinations().put(Digger.class, Timelord.class);
        getBlockedCombinations().put(Phantom.class, Vacuum.class);
        getBlockedCombinations().put(Gladiator.class, Neo.class);
        getBlockedCombinations().put(Ninja.class, Neo.class);
        // getBlockedCombinations().put(Expel.class, Endermage.class);
        //getBlockedCombinations().put(Expel.class, Stomper.class);
        // getBlockedCombinations().put(Expel.class, Phantom.class);
        //        getBlockedCombinations().put(Miner.class, Forger.class);
    }
}
