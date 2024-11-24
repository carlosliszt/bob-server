/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Deprecated
public class EventCommand implements ProxyInterface {

    @Command(name = "evento", aliases = {"eventos", "event"}, platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context) {
        BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "play evento");
    }
}