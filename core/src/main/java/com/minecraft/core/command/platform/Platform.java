/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.platform;

public enum Platform {
    /**
     * The BOTH platform can be all senders listed below,
     * that means that it can receive command from
     * PLAYER and CONSOLE.
     */
    BOTH,
    /**
     * The PLAYER platform only accepts Players
     * to execute that command
     */
    PLAYER,
    /**
     * The CONSOLE platform only accept the Console
     * to execute that command
     */
    CONSOLE
}