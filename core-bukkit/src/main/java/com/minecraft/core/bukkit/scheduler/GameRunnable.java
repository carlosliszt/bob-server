/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.scheduler;

import com.minecraft.core.bukkit.util.BukkitInterface;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class GameRunnable implements Runnable, BukkitInterface {

    private int taskId = -1;

    public GameRunnable() {
    }

    public synchronized void cancel() throws IllegalStateException {
        Bukkit.getScheduler().cancelTask(this.getTaskId());
    }

    public synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().runTask(plugin, this));
    }

    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().runTaskAsynchronously(plugin, this));
    }

    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().runTaskLater(plugin, this, delay));
    }

    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, delay));
    }

    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period));
    }

    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period));
    }

    public synchronized int getTaskId() throws IllegalStateException {
        int id = this.taskId;
        if (id == -1) {
            throw new IllegalStateException("Not scheduled yet");
        } else {
            return id;
        }
    }

    private void checkState() {
        if (this.taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + this.taskId);
        }
    }

    private BukkitTask setupId(BukkitTask task) {
        this.taskId = task.getTaskId();
        return task;
    }

}