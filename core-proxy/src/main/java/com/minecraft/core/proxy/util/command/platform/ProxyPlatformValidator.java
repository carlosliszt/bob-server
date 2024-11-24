/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command.platform;

import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.command.platform.PlatformValidator;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ProxyPlatformValidator implements PlatformValidator {

    public static final ProxyPlatformValidator INSTANCE = new ProxyPlatformValidator();

    @Override
    public boolean validate(Platform platform, Object object) {
        if (platform == Platform.CONSOLE) {
            return !(object instanceof ProxiedPlayer);
        }

        if (platform == Platform.PLAYER) {
            return object instanceof ProxiedPlayer;
        }

        return true;
    }

    @Override
    public Platform fromSender(Object object) {
        if (object instanceof ProxiedPlayer) {
            return Platform.PLAYER;
        }

        if (object instanceof CommandSender) {
            return Platform.CONSOLE;
        }

        return Platform.BOTH;
    }
}
