/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.whitelist.Whitelist;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.command.CommandSender;

public class OpenEventCommand implements Assistance, BukkitInterface {

    @Command(name = "openevent", rank = Rank.TRIAL_MODERATOR)
    public void handleCommand(Context<CommandSender> context) {

        Whitelist whitelist = getPlugin().getWhitelist();

        if (whitelist.isActive())
            whitelist.setActive(false);

        if (context.argsCount() > 0) {

            async(() -> Constants.getRedis().publish(Redis.OPEN_EVENT_CHANNEL, createArgs(0, context.getArgs(), "undefined", true)));

        } else {
            context.info("command.openevent.successful_with_no_message");
        }
    }
}
