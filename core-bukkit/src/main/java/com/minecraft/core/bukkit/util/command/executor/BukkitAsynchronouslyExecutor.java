/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.executor;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.Executor;

public class BukkitAsynchronouslyExecutor implements Executor {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public BukkitAsynchronouslyExecutor(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void execute(Runnable command) {
        scheduler.runTaskAsynchronously(plugin, command);
    }

}
