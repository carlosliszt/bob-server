/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.event.game.GameRecoveryModeToggleEvent;
import com.minecraft.hungergames.game.handler.listener.recovery.RecoveryListener;
import com.minecraft.hungergames.game.object.RecoveryMode;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;

public class LockCommand implements Listener, Assistance, BukkitInterface {

    @Command(name = "lock", platform = Platform.PLAYER, rank = Rank.EVENT_MOD)
    public void handleCommand(Context<Player> context) throws InvocationTargetException, IllegalAccessException {

        if (!hasStarted()) {
            context.info("hg.game_did_not_started");
            return;
        }

        RecoveryMode recoveryMode = getGame().getRecoveryMode();

        if (recoveryMode.isEnabled()) {
            recoveryMode.setEnabled(false);
            recoveryMode.setPlayers(-1);
            getPlugin().getVariableLoader().getVariable("hg.combatlog_kill").setValue(false);
        } else {
            Bukkit.getPluginManager().registerEvents(new RecoveryListener(), getPlugin());
            recoveryMode.setEnabled(true);
            recoveryMode.setPlayers(getPlugin().getUserStorage().getAliveUsers().size());
            getPlugin().getKitStorage().unregister();
            getPlugin().getVariableLoader().getVariable("hg.combatlog_kill").setValue(true);
        }

        new GameRecoveryModeToggleEvent(getGame()).fire();

        broadcast((recoveryMode.isEnabled() ? "hg.game.recovery_mode.enable" : "hg.game.recovery_mode.disable"));
    }
}
