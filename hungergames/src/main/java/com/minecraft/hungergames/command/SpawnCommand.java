/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SpawnCommand implements Assistance, BukkitInterface {

    @Command(name = "spawn", platform = Platform.PLAYER)
    public void killCommand(Context<Player> context) {

        User user = getUser(context.getUniqueId());

        if (hasStarted() && user.isAlive()) {
            context.info("hg.game.already_started");
            return;
        } else if (hasStarted() && !user.getAccount().hasPermission(Rank.VIP)) {
            context.info("game.spectatorlist.no_permission", Constants.SERVER_STORE);
            return;
        }

        Player player = context.getSender();

        Location location = HungerGames.getInstance().getGame().getVariables().getSpawnpoint().clone();
        int range = HungerGames.getInstance().getGame().getVariables().getSpawnRange();
        location.add(randomize(range), 0, randomize(range));

        ((CraftPlayer) player).getHandle().playerConnection.teleport(location);
    }
}