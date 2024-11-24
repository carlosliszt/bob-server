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
import com.minecraft.core.util.DateUtils;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;

public class TimeCommand implements Assistance {

    @Command(name = "tempo", usage = "tempo <tempo>", platform = Platform.BOTH, rank = Rank.TRIAL_MODERATOR)
    public void handleCommand(Context<CommandSender> context, String string) throws InvocationTargetException, IllegalAccessException {

        if (!hasStarted() && getGame().getVariables().getMinimumPlayers() > getPlugin().getUserStorage().getAliveUsers().size()) {
            context.info("command.time.insufficient_players", getGame().getVariables().getMinimumPlayers());
            return;
        }

        long time;
        try {
            time = DateUtils.parseDateDiff(string, true);
        } catch (Exception e) {
            context.info("invalid_time", "y,m,d,min,s");
            return;
        }

        time = (time - System.currentTimeMillis()) / 1000;

        if (time <= 0) {
            context.info("command.number_negative");
            return;
        }

        getGame().setTime((int) time);
        if (getStage() == GameStage.INVINCIBILITY)
            getPlugin().getVariableLoader().getVariable("hg.timer.invincibility").setValue((int) time);
    }
}
