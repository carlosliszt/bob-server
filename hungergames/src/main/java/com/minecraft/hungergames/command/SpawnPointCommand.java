/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpawnPointCommand implements BukkitInterface, Assistance {

    @Command(name = "setspawnpoint", platform = Platform.PLAYER, rank = Rank.TRIAL_MODERATOR)
    public void setSpawnpoint(Context<Player> context) {

        int radius = 0;

        if (context.argsCount() > 0 && isInteger(context.getArg(0)))
            radius = Integer.parseInt(context.getArg(0));

        Location location = context.getSender().getLocation();

        World world = location.getWorld();

        double x = location.getBlockX() + 0.5;
        double y = location.getY();
        double z = location.getBlockZ() + 0.5;

        float yaw = location.getYaw();

        location = new Location(world, x, y, z, yaw, 0);

        getGame().getVariables().setSpawnpoint(location);
        getGame().getVariables().setSpawnRange(radius);
        context.info("command.setspawnpoint.set", (int) x, (int) y, (int) z, radius);
    }

    @Command(name = "removespawnpoint", platform = Platform.PLAYER, rank = Rank.SECONDARY_MOD)
    public void removeSpawnpoint(Context<Player> context) {

        getGame().getVariables().setSpawnpoint(getGame().getVariables().getDefaultSpawnpoint());
        getGame().getVariables().setSpawnRange(0);
        context.info("command.removespawnpoint.successful");
    }
}
