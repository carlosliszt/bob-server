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
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.structure.Feast;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

public class ForcefeastCommand implements BukkitInterface, Assistance {

    @Command(name = "forcefeast", platform = Platform.PLAYER, rank = Rank.SECONDARY_MOD)
    public void handleCommand(Context<Player> context) {

        if (!hasStarted()) {
            context.info("hg.game_did_not_started");
            return;
        }

        Feast feast = new Feast(HungerGames.getInstance(), -1);
        feast.setLocation(context.getSender().getPlayer().getLocation());
        feast.build().spawn(true);
    }

}