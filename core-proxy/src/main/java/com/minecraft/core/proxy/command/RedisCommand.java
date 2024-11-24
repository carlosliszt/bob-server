/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class RedisCommand {

    @Command(name = "register", usage = "redis", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void handleCommand(Context<ProxiedPlayer> context) {
    }
}
