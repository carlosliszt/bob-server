/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TeleportCommand implements BukkitInterface {

    @Command(name = "teleport", aliases = {"tp"}, usage = "{label} [player] <target> and/or <x> <y> <z>", platform = Platform.PLAYER, rank = Rank.STREAMER_PLUS)
    public void handleCommand(Context<Player> context, String[] args) {
        Player sender = context.getSender();

        if (args.length >= 1 && args.length <= 4) {
            Player player;
            if (args.length != 1 && args.length != 3) {
                player = getPlayer(args[0]);
            } else {
                if (sender == null) {
                    context.info("command.teleport.missing_player");
                    return;
                }

                player = sender;
            }

            if (player == null) {
                context.info("target.not_found");
                return;
            }

            if (args.length < 3) {
                String arg = args[args.length - 1];
                Player target = getPlayer(arg);

                if (target == null || !sender.canSee(target)) {
                    context.info("target.not_found");
                    return;
                }

                player.teleport(target, PlayerTeleportEvent.TeleportCause.COMMAND);
                context.info("command.teleport.successful_teleport", player.getName(), target.getName());

                Account account = context.getAccount();
                log(account, account.getDisplayName() + " se teleportou para " + target.getName());
            } else if (player.getWorld() != null) {
                Location playerLocation = player.getLocation();

                double x = this.getCoordinate(playerLocation.getX(), args[args.length - 3]);
                double y = this.getCoordinate(playerLocation.getY(), args[args.length - 2], 0, 0);
                double z = this.getCoordinate(playerLocation.getZ(), args[args.length - 1]);

                if (x == -3.0000001E7D || y == -3.0000001E7D || z == -3.0000001E7D) {
                    context.info("command.teleport.invalid_location");
                    return;
                }

                playerLocation.setX(x);
                playerLocation.setY(y);
                playerLocation.setZ(z);

                int new_x = (int) x;
                int new_y = (int) y;
                int new_z = (int) z;

                player.teleport(playerLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                context.info("command.teleport.successful_teleport", player.getName(), new_x + ", " + new_y + ", " + new_z);

                Account account = context.getAccount();
                log(account, account.getDisplayName() + " teleportou " + player.getName() + " para " + new_x + ", " + new_y + ", " + new_z);
            }
        }
    }

    @Completer(name = "teleport")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1 || context.argsCount() == 2)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

    private double getCoordinate(double current, String input) {
        return this.getCoordinate(current, input, -30000000, 30000000);
    }

    private double getCoordinate(double current, String input, int min, int max) {
        boolean relative = input.startsWith("~");
        double result = relative ? current : 0.0D;
        if (!relative || input.length() > 1) {
            boolean exact = input.contains(".");
            if (relative) {
                input = input.substring(1);
            }

            double testResult = getDouble(input);
            if (testResult == -3.0000001E7D) {
                return -3.0000001E7D;
            }

            result += testResult;
            if (!exact && !relative) {
                result += 0.5D;
            }
        }

        if (min != 0 || max != 0) {
            if (result < (double) min) {
                result = -3.0000001E7D;
            }

            if (result > (double) max) {
                result = -3.0000001E7D;
            }
        }

        return result;
    }

    private double getDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException var2) {
            return -3.0000001E7D;
        }
    }

    private Player getPlayer(String text) {
        Player target;

        if (Constants.isUniqueId(text))
            target = Bukkit.getServer().getPlayer(UUID.fromString(text));
        else
            target = Bukkit.getServer().getPlayerExact(text);
        return target;
    }

}