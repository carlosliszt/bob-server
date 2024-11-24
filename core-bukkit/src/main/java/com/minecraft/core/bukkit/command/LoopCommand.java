/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LoopCommand implements BukkitInterface {

    @Command(name = "loop", aliases = {"circuit"}, usage = "{label} <times> <delay> <action>", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<Player> context, Integer times, Integer delay, String[] message) {

        Player player = context.getSender();

        if (times <= 0) {
            context.info("command.number_negative");
            return;
        }

        String msg = String.join(" ", message);
        new BukkitRunnable() {
            int executed = 0;

            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (executed == times) {
                    cancel();
                    return;
                }
                player.chat(msg);
                executed++;
            }
        }.runTaskTimer(BukkitGame.getEngine(), 0, Math.max(0, delay));
    }
}
