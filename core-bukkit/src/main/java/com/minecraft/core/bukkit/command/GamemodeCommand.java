/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamemodeCommand implements BukkitInterface {

    @Command(name = "gamemode", aliases = {"gm"}, usage = "{label} <mode> [player]", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<Player> context, String[] args) {
        Player sender = context.getSender();

        String playerArg = sender.getName();

        if (args.length == 2) {
            playerArg = args[1];
        }

        Player target = Bukkit.getPlayerExact(playerArg);

        if (target == null) {
            context.info("target.not_found");
            return;
        }

        GameMode gameMode = getGameMode(args[0]);

        if (gameMode == null) {
            context.info("command.gamemode.mode_not_found");
            return;
        }

        if (target.getGameMode().equals(gameMode)) {
            context.info("command.gamemode.target_already_in_gamemode");
            return;
        }

        target.setGameMode(gameMode);

        Account account = context.getAccount();

        if (target != sender) {
            log(account, account.getDisplayName() + " alterou o modo de jogo de " + target.getName() + " para " + gameMode.name());
            context.info("command.gamemode.target_changed_mode", target.getName());
        } else {
            if (account.getVersion() >= 47)
                context.sendMessage("Your game mode has been updated.");
            log(account, account.getDisplayName() + " alterou seu próprio modo de jogo para " + gameMode.name());
        }
    }

    @Completer(name = "gamemode")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            List<String> ret = new ArrayList<>();
            for (GameMode gm : GameMode.values()) {
                if (startsWith(gm.name().toLowerCase(), context.getArg(0).toLowerCase()))
                    ret.add(gm.name().toLowerCase());
            }
            return ret;
        }
        if (context.argsCount() == 2)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

    private GameMode getGameMode(String input) {
        if (multiEqualsIgnoreCase(input, "0", "s", "survival"))
            return GameMode.SURVIVAL;
        if (multiEqualsIgnoreCase(input, "1", "c", "creative"))
            return GameMode.CREATIVE;
        if (multiEqualsIgnoreCase(input, "2", "a", "adventure"))
            return GameMode.ADVENTURE;
        if (multiEqualsIgnoreCase(input, "3", "sp", "spectator"))
            return GameMode.SPECTATOR;
        return null;
    }

}