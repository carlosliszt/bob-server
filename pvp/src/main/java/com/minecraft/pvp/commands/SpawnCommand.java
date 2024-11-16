/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.commands;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.pvp.user.User;
import org.bukkit.entity.Player;

public class SpawnCommand implements BukkitInterface {

    @Command(name = "spawn", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {
        User user = User.fetch(context.getUniqueId());

        boolean free = Vanish.getInstance().isVanished(user.getUniqueId()) || user.getGame().getName().equals("Lava");

        if (!free) {
            if (user.inCombat()) {
                context.info("command.spawn.in_combat");
                return;
            }

            if (user.isKept()) {
                context.info("command.spawn.already_in");
                return;
            }
        }

        user.getGame().join(user, true);
    }
}