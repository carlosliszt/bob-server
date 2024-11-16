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
import com.minecraft.hungergames.game.structure.Minifeast;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

public class ForceMinifeastCommand implements BukkitInterface, Assistance {

    @Command(name = "forceminifeast", platform = Platform.PLAYER, rank = Rank.SECONDARY_MOD)
    public void handleCommand(Context<Player> context) {

        if (!hasStarted()) {
            context.info("hg.game_did_not_started");
            return;
        }

        Minifeast.fetch().setLocation(context.getSender().getPlayer().getLocation()).prepare().generate();
    }
}
