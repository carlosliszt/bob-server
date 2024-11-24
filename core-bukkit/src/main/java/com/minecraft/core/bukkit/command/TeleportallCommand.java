/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TeleportallCommand implements BukkitInterface {

    private BukkitTask bukkitTask;

    @Command(name = "tpall", aliases = {"teleportall"}, platform = Platform.PLAYER, rank = Rank.TRIAL_MODERATOR)
    public void handleCommand(Context<Player> context) {

        if (bukkitTask != null) {
            context.sendMessage("§cConcurrentTeleportException: Já há um teleporte em massa em execução. (id=" + bukkitTask.getTaskId() + ")");
            return;
        }

        Set<Player> recipients = new HashSet<>(context.getSender().getWorld().getPlayers());
        Location location = context.getSender().getLocation().clone();

        PlayerMassiveTeleportExecuteEvent event = new PlayerMassiveTeleportExecuteEvent(recipients, location);
        event.fire();

        if (event.isCancelled())
            return;

        String s = event.getRecipients().size() >= 35 ? "(this process may take a while)" : "";
        context.info("command.tpall.teleport_succesful", s);
        log(context.getAccount(), context.getSender().getName() + " teleportou todos até ele");
        teleport(event.getRecipients(), event.getLocation());
    }

    private void teleport(Set<Player> players, Location location) {
        this.bukkitTask = new BukkitRunnable() {
            final Iterator<? extends Player> playerIterator = players.iterator();

            public void run() {
                for (int i = 0; i < 6; i++) {
                    if (playerIterator.hasNext()) {
                        Player player = playerIterator.next();
                        ((CraftPlayer) player).getHandle().playerConnection.teleport(location);
                    } else {
                        TeleportallCommand.this.bukkitTask = null;
                        cancel();
                        break;
                    }
                }
            }
        }.runTaskTimer(BukkitGame.getEngine(), 3, 10);
    }
}