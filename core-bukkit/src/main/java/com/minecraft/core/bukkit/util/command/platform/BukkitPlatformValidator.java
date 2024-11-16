/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.platform;

import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.command.platform.PlatformValidator;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * The BukkitPlatformValidator validates if the Platform
 * is a correct and usable {@link Platform}
 */
public final class BukkitPlatformValidator implements PlatformValidator {

    public static final BukkitPlatformValidator INSTANCE = new BukkitPlatformValidator();

    /**
     * Tries to validate the Command platform and Sender object.
     * <p> Returns false if it wasn't validated</p>
     *
     * @param platform Platform
     * @param object   Object
     * @return Boolean
     */
    @Override
    public boolean validate(Platform platform, Object object) {
        if (platform == Platform.BOTH) {
            return true;
        }

        if (platform == Platform.PLAYER && object instanceof Player) {
            return true;
        }

        return platform == Platform.CONSOLE && object instanceof ConsoleCommandSender;
    }

    /**
     * Returns the Platform by the Sender object
     * <p>Example: The Player object returns a {@link Platform} of PLAYER</p>
     *
     * @param object Object
     * @return Platform
     */
    @Override
    public Platform fromSender(Object object) {
        if (object instanceof Player) {
            return Platform.PLAYER;
        }

        if (object instanceof ConsoleCommandSender) {
            return Platform.CONSOLE;
        }

        return Platform.BOTH;
    }

}
