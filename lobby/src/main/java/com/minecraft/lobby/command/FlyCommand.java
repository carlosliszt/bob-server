/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.lobby.user.User;
import org.bukkit.entity.Player;

public class FlyCommand {

    @Command(name = "fly", platform = Platform.PLAYER, rank = Rank.VIP)
    public void handleCommand(Context<Player> context) {

        Player sender = context.getSender();
        User user = User.fetch(sender.getUniqueId());

        if (user.isParkourMode()) {
            context.sendMessage("§b§lPARKOUR§c Você não pode voar enquanto estiver no parkour.");
            return;
        }

        boolean bool = !user.getAccount().getProperty("lobby.fly", false).getAsBoolean();
        user.getAccount().setProperty("lobby.fly", bool);

        sender.setAllowFlight(!sender.getAllowFlight());

        if (sender.getAllowFlight())
            sender.setFlying(true);

        if (bool)
            context.info("command.fly.activated");
        else
            context.info("command.fly.disabled");
    }
}