/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class FollowCommand implements BukkitInterface {

    @Command(name = "follow", usage = "{label} <target>", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<Player> context, Player target) {

        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        if (sender == target) {
            context.info("command.follow.cant_follow_yourself");
            return;
        }

        Account account = context.getAccount();

        Player property = account.getProperty("following", null).getAs(Player.class);

        if (property != null && property.equals(target)) {
            account.setProperty("following", null);
            context.info("command.follow.stopped_succesful");
            return;
        }

        if (property != null) {
            context.info("command.follow.already_following");
            return;
        }

        account.setProperty("following", target);
        context.info("command.follow.execution_successful");

        new BukkitRunnable() {
            public void run() {
                if (account.getProperty("following").getAs(Player.class) == null) {
                    cancel();
                    return;
                }

                if (!sender.isOnline() || !target.isOnline() || !sender.canSee(target) || !sender.getWorld().getUID().equals(target.getWorld().getUID())) {
                    cancel();
                    return;
                }

                Location location = target.getLocation();

                PlayerConnection connection = ((CraftPlayer) sender).getHandle().playerConnection;

                if (connection == null || connection.isDisconnected()) {
                    cancel();
                    return;
                }

                sender.setSneaking(target.isSneaking());
                sender.setSprinting(target.isSprinting());

                connection.teleport(location);
            }
        }.runTaskTimerAsynchronously(BukkitGame.getEngine(), 0, 1);
    }

    @Completer(name = "follow")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }


}