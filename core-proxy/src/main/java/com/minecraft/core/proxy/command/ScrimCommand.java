/*
 * Copyright (C) BobMC, All Rights Reserved
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
public class ScrimCommand implements ProxyInterface {

    @Command(name = "scrim", aliases = {"bobinho"}, platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context) {
        BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "play scrim");
    }

}