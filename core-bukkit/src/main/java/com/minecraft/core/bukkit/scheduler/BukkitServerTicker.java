/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.scheduler;

import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BukkitServerTicker extends GameRunnable {

    @Getter
    private static final BukkitServerTicker singleton = new BukkitServerTicker();

    public void start(Plugin plugin) {
        this.runTaskTimer(plugin, 1, 1);
    }

    @Getter
    private int currentTick;

    @Override
    public void run() {
        currentTick++;
        Bukkit.getPluginManager().callEvent(new ServerHeartbeatEvent(currentTick));
    }
}