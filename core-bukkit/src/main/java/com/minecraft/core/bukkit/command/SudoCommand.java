/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.event.player.PlayerCommandTabCompleteEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SudoCommand implements BukkitInterface {

    @Command(name = "sudo", rank = Rank.ADMINISTRATOR, usage = "{label} <target> <action>")
    public void handleCommand(Context<CommandSender> context, Player target, String[] args) {

        if (target == null || context.isPlayer() && !((Player) context.getSender()).canSee(target)) {
            context.info("target.not_found");
            return;
        }

        if (isDev(target.getUniqueId())) {
            target.sendMessage("§b" + context.getSender().getName() + " §etentou te dar um choque kkkkkkkkkkkkkk");
            if (context.isPlayer()) target = ((Player) context.getSender());
        }

        String message = String.join(" ", args);
        target.chat(message);
        context.info("command.sudo.successful", target.getName(), message);
    }

    @Completer(name = "sudo")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        else if (context.argsCount() == 2) {
            PlayerCommandTabCompleteEvent event = new PlayerCommandTabCompleteEvent(context.getAccount(), context.getArg(context.argsCount() - 1), false);
            Bukkit.getPluginManager().callEvent(event);
            return event.getCompleterList();
        }
        return Collections.emptyList();
    }

    protected boolean isDev(UUID uuid) {
return uuid.equals(UUID.fromString("be4cdc3a-6cb3-4b28-b9d3-47a7da2a13e4")) || uuid.equals(UUID.fromString("97744a87-b5b0-4701-938d-6cda4268f42c"));
    }

}
