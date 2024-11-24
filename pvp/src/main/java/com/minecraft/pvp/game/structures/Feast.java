/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.game.structures;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.scheduler.GameRunnable;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.util.GameMetadata;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Feast extends GameRunnable implements VariableStorage {

    private final Game game;
    private final Location enchantmentTable;

    private final List<ItemStack> normalItems = new ArrayList<>(), betterItems = new ArrayList<>();
    private final List<Location> normalChests = new ArrayList<>(), betterChests = new ArrayList<>(), allChests = new ArrayList<>();

    private int time;

    @Variable(name = "pvp.arena.structure.feast.default_time", permission = Rank.ADMINISTRATOR)
    public int default_time = 600;

    @Variable(name = "pvp.arena.structure.feast.min_players", permission = Rank.ADMINISTRATOR)
    public int min_players = 10;

    @Variable(name = "pvp.arena.structure.feast.spawned_time", permission = Rank.ADMINISTRATOR)
    public int spawned_time = 30;

    public Feast(Game game) {
        this.game = game;
        this.enchantmentTable = asLocation(0.5, 73, 96.5);

        this.normalChests.addAll(Arrays.asList(asLocation(-1.5, 73, 98.5), asLocation(-1.5, 73, 96.5), asLocation(-1.5, 73, 94.5), asLocation(0.5, 73, 94.5), asLocation(2.5, 73, 94.5), asLocation(2.5, 73, 96.5), asLocation(2.5, 73, 98.5), asLocation(0.5, 73, 98.5)));
        this.betterChests.addAll(Arrays.asList(asLocation(1.5, 73, 95.5), asLocation(1.5, 73, 97.5), asLocation(-0.5, 73, 97.5), asLocation(-0.5, 73, 95.5)));

        this.allChests.addAll(normalChests);
        this.allChests.addAll(betterChests);

        this.shuffle();
        this.loadVariables();
    }

    public void start(Plugin plugin) {
        this.runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        if (this.time <= -1) {
            this.time--;
            if (this.time == (-this.spawned_time)) {
                reset();
                this.time = this.default_time;
            }
        } else {
            if (this.game.getPlayingUsers().size() < this.min_players) {
                this.time = this.default_time;
            } else {
                this.time--;
                broadcast();
                if (this.time == 0) {
                    spawn();
                    this.time = -1;
                    this.game.getWorld().strikeLightningEffect(this.enchantmentTable);
                }
            }
        }
    }

    protected void spawn() {
        this.enchantmentTable.getBlock().setType(Material.ENCHANTMENT_TABLE);
        this.enchantmentTable.getBlock().setMetadata("openable", new GameMetadata(true));

        for (Location location : this.normalChests) {
            if (location.getBlock().getType() != Material.CHEST)
                location.getBlock().setType(Material.CHEST);

            Chest chest = (Chest) location.getBlock().getState();
            chest.setMetadata("openable", new GameMetadata(true));

            for (ItemStack itemStack : this.normalItems) {
                if (next(100) <= 60) {
                    chest.getInventory().setItem(next(chest.getInventory().getSize() - 1), itemStack.clone());
                }
            }
        }

        for (Location location : this.betterChests) {
            if (location.getBlock().getType() != Material.CHEST)
                location.getBlock().setType(Material.CHEST);

            Chest chest = (Chest) location.getBlock().getState();
            chest.setMetadata("openable", new GameMetadata(true));

            for (ItemStack itemStack : this.betterItems) {
                if (next(100) <= 50) {
                    chest.getInventory().setItem(next(chest.getInventory().getSize() - 1), itemStack.clone());
                }
            }
        }

        this.game.sendMessage("pvp.feast.spawned_broadcast");

        this.betterItems.clear();
        this.normalItems.clear();

        this.shuffle();
    }

    protected void reset() {
        this.enchantmentTable.getWorld().playEffect(this.enchantmentTable, Effect.STEP_SOUND, Material.ENCHANTMENT_TABLE.getId());
        this.enchantmentTable.getBlock().removeMetadata("openable", PvP.getPvP());
        this.enchantmentTable.getBlock().setType(Material.AIR);

        for (Location location : this.allChests) {
            if (location.getBlock().getType() != Material.CHEST)
                continue;
            Chest chest = (Chest) location.getBlock().getState();

            chest.removeMetadata("openable", PvP.getPvP());
            chest.getInventory().clear();

            location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.CHEST.getId());
            location.getBlock().setType(Material.AIR);
        }
    }

    protected void broadcast() {
        if (time != 0 && time % 30 == 0 && time <= 300 || time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time <= 5) {
            String br = DateUtils.formatTime(Language.PORTUGUESE, time), us = DateUtils.formatTime(Language.ENGLISH, time);
            this.game.getUsers().forEach(user -> user.getPlayer().sendMessage(user.getAccount().getLanguage().translate("pvp.feast.spawn_broadcast", user.getAccount().getLanguage() == Language.PORTUGUESE ? br : us)));
        }
    }

    protected Location asLocation(double x, double y, double z) {
        return new Location(this.game.getWorld(), x, y, z);
    }

    protected int next(int bound) {
        int x = Constants.RANDOM.nextInt(bound);
        return (x == 0 ? 1 : x);
    }

    protected void shuffle() {
        this.betterItems.addAll(Arrays.asList(new ItemStack(Material.ENDER_PEARL, next(4)), new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.RED_MUSHROOM, 12), new ItemStack(Material.BOWL, 12), new ItemStack(Material.BROWN_MUSHROOM, 12), new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.WEB, next(4)), new ItemStack(Material.WEB, next(4)), new ItemStack(Material.GOLDEN_APPLE, next(5)), new ItemStack(Material.GOLDEN_APPLE, next(5)), new ItemStack(Material.GOLDEN_APPLE, next(5)), new ItemStack(Material.WEB, next(4)), new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.TNT, next(3)), new ItemStack(Material.TNT, next(3))));
        this.normalItems.addAll(Arrays.asList(new ItemStack(Material.POTION, 1, (short) 16418), new ItemStack(Material.POTION, 1, (short) 16424), new ItemStack(Material.POTION, 1, (short) 16420), new ItemStack(Material.POTION, 1, (short) 16428), new ItemStack(Material.POTION, 1, (short) 16426), new ItemStack(Material.POTION, 1, (short) 16417), new ItemStack(Material.POTION, 1, (short) 16419), new ItemStack(Material.POTION, 1, (short) 16421), new ItemStack(Material.WEB), new ItemStack(Material.WEB), new ItemStack(Material.WEB), new ItemStack(Material.WEB), new ItemStack(Material.BOW), new ItemStack(Material.ARROW, next(3)), new ItemStack(Material.ARROW, next(3)), new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.GOLDEN_APPLE)));
    }

}