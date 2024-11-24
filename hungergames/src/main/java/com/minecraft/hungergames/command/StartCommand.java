/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.command.CommandSender;

public class StartCommand implements Assistance {

    @Command(name = "start", platform = Platform.PLAYER, rank = Rank.SECONDARY_MOD)
    public void startCommand(Context<CommandSender> context) {
        if (hasStarted())
            context.info("hg.game.already_started");
        else {
            getGame().start();

         /*   Coliseum coliseum = getPlugin().getSpawn();
            coliseum.getGate().forEach(c -> WorldEditAPI.getInstance().setBlock(c.getLocation(), Material.AIR, (byte) 0));
            coliseum.getGate().clear();*/

        }
    }

    /*@Command(name = "openthefuckingdoors", platform = Platform.PLAYER, rank = Rank.SECONDARY_MOD)
    public void doorCommand(Context<CommandSender> context) {
        Coliseum coliseum = getPlugin().getSpawn();
        coliseum.getGate().forEach(c -> WorldEditAPI.getInstance().setBlock(c.getLocation(), Material.AIR, (byte) 0));
        coliseum.getGate().clear();
    }*/
}
