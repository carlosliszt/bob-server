/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.hungergames.game.structure.Feast;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

public class FeastCommand implements Assistance {

    @Command(name = "feast", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        Feast feast = getGame().getFeast();

        if (feast == null || !feast.isSpawned()) {
            context.info("hg.game.feast_not_spawned");
            return;
        }
        context.getSender().setCompassTarget(feast.getLocation());
        context.info("hg.game.user.compass_pointing_to", "Feast");
    }
}
