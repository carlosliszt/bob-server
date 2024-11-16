/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ForfeitCommand implements Assistance, VariableStorage {

    public ForfeitCommand() {
        loadVariables();
    }

    @Variable(name = "hg.disable_drops_forfeit")
    public boolean dropItems = true;

    @Command(name = "desistir", platform = Platform.PLAYER, aliases = {"desisto", "forfeit"})
    public void handleCommand(Context<Player> context) {

        User user = User.fetch(context.getUniqueId());

        if (!hasStarted()) {
            context.info("hg.game_did_not_started");
            return;
        }

        if (!user.isAlive()) {
            context.info("hg.game.not_alive");
            return;
        }

        User killer = null;
        DieCause dieCause = DieCause.SURRENDER;
        boolean countStats = false;

        if (user.getCombatTag().isTagged()) {
            killer = user.getCombatTag().getLastHit();
            dieCause = DieCause.COMBAT;
            countStats = true;
        }

        LivingUserDieEvent event = new LivingUserDieEvent(user, killer, countStats, dieCause, (dropItems ? user.getInventoryContents() : Collections.emptyList()), user.getPlayer().getLocation());
        event.fire();
    }

}