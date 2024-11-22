/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.auth;

import com.minecraft.auth.listener.Listeners;
import com.minecraft.auth.scheduler.AlertScheduler;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.server.BukkitServerStorage;
import com.minecraft.core.bukkit.util.item.InteractableItemListener;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.server.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;

public class Auth extends BukkitGame {

    @Override
    public void onLoad() {
        unsafe(this);
        Constants.setRedis(new Redis());
        //setVisible(true);
    }

    @Override
    public void onEnable() {
        Constants.setServerStorage(new BukkitServerStorage());

        Constants.setServerType(ServerType.AUTH);

        startServerDump();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "Auth");

        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getPluginManager().registerEvents(new InteractableItemListener(), this);

        new AlertScheduler().start();

        WorldBorder worldBorder = Bukkit.getWorlds().get(0).getWorldBorder();
        worldBorder.setCenter(new Location(Bukkit.getWorlds().get(0), 0, 28, 0));
        worldBorder.setSize(150);
    }

    @Override
    public void onDisable() {
        try {
            Constants.getRedis().getJedisPool().destroy();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Auth getAuth() {
        return Auth.getPlugin(Auth.class);
    }
}