/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command.executor;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.Executor;

public class ProxyAsynchronouslyExecutor implements Executor {

    private final Plugin plugin;
    private final TaskScheduler scheduler;

    public ProxyAsynchronouslyExecutor(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getProxy().getScheduler();
    }

    @Override
    public void execute(Runnable command) {
        scheduler.runAsync(plugin, command);
    }

}