/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.game;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.user.User;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Game implements Listener, BukkitInterface, VariableStorage {

    private final String name = getClass().getSimpleName();
    private final UUID uniqueId = UUID.randomUUID();

    private GameType type;
    private World world;
    private Location spawn, lobby;

    private boolean active = true;

    private final Set<User> users = new HashSet<>();
    private final List<Columns> columnsToLoad = new ArrayList<>();

    private int limit;

    private ImmutableSet<EntityDamageEvent.DamageCause> validDamages;
    private final ImmutableSet<Material> validDrops = Sets.immutableEnumSet(Material.MUSHROOM_SOUP, Material.BOWL, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM);

    public List<User> getPlayingUsers() {
        return this.users.stream().filter(user -> !Vanish.getInstance().isVanished(user.getUniqueId())).collect(Collectors.toList());
    }

    public void join(User user, boolean teleport) {
        this.users.add(user);

        Player player = user.getPlayer();

        if (player.getPassenger() != null)
            player.getPassenger().leaveVehicle();

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setItemOnCursor(null);
        player.getOpenInventory().getTopInventory().clear();
        player.setLevel(0);
        player.setExp(0);
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
        player.setGameMode(GameMode.SURVIVAL);

        user.restoreCombat();
        user.setKept(true);
        user.setGame(this);
        user.setCanFirstFall(true);
        user.getKit1().resetAttributes(user);
        user.getKit2().resetAttributes(user);

        if (teleport)
            player.teleport(getSpawn());

        PvP.getPvP().getVisibility().update();
    }

    public void rejoin(User user, Rejoin rejoin) {
        quit(user);

        if (rejoin == Rejoin.PLAYER) {
            join(user, true);
        } else if (rejoin == Rejoin.VANISH) {
            if (user.inCombat()) {
                new UserDiedEvent(user, User.fetch(user.getLastCombat()), user.getInventoryContents(), user.getPlayer().getLocation(), UserDiedEvent.Reason.LOGOUT, user.getGame()).fire();
            }

            PvP.getPvP().getVisibility().update();
        }

        this.users.add(user);
    }

    public void quit(User user) {
        this.users.remove(user);
        PvP.getPvP().getVisibility().update();
    }

    public abstract void handleSidebar(User user);

    public abstract void onLogin(User user);

    public void sendMessage(String tag) {
        getUsers().forEach(user -> user.getPlayer().sendMessage(user.getAccount().getLanguage().translate(tag)));
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public GameType getType() {
        return type;
    }

    public World getWorld() {
        return world;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getSpawn() {
        return spawn;
    }

    public boolean isActive() {
        return active;
    }

    public Set<User> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || (obj instanceof Game) && (((Game) obj).getName().equals(this.getName()));
    }

    public void addColumn(Columns... columns) {
        this.columnsToLoad.addAll(Arrays.asList(columns));
    }

    public PvP getPlugin() {
        return PvP.getPvP();
    }

    public enum Rejoin {
        PLAYER, VANISH;
    }

}