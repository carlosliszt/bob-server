/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KillCommand implements Assistance, BukkitInterface {

    @Command(name = "finalkill", platform = Platform.PLAYER, rank = Rank.SECONDARY_MOD, usage = "finalkill <target> [reason]")
    public void finalKillCommand(Context<Player> context, final String name) {

        if (!hasStarted()) {
            context.info("hg.game_did_not_started");
            return;
        }

        User target = User.getUser(name);

        if (target == null) {
            context.info("target.not_found");
            return;
        }

        User killer = null;
        DieCause dieCause = DieCause.SURRENDER;
        boolean countStats = false;

        if (target.getCombatTag().isTagged()) {
            killer = target.getCombatTag().getLastHit();
            dieCause = DieCause.COMBAT;
            countStats = true;
        }

        new LivingUserDieEvent(target, killer, countStats, dieCause, target.getInventoryContents(), target.getPlayer().getLocation()).fire();

        if (target.getPlayer() != context.getSender())
            log(context.getAccount(), context.getSender().getName() + " final killed " + target.getName());
    }

    @Completer(name = "finalkill")
    public List<String> killCompleter(Context<CommandSender> context) {
        return getOnlineNicknames(context);
    }
}
