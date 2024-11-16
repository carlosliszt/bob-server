/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.entity.Player;

public class BuildCommand {

    @Command(name = "build", platform = Platform.PLAYER, rank = Rank.PRIMARY_MOD)
    public void handleCommand(Context<Player> context) {
        Account account = context.getAccount();

        boolean bool = !account.getProperty("pvp.build", false).getAsBoolean();
        account.setProperty("pvp.build", bool);

        context.sendMessage("§6self_build §ealterado para §b" + bool);
    }

}