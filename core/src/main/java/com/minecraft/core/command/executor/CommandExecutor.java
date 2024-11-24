/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.executor;

import com.minecraft.core.command.command.Context;

/**
 * The BukkitCommandExecutor is the main executor of each
 * method that is listed as a Command, it invokes the method
 * and executes everything inside.
 */
@FunctionalInterface
public interface CommandExecutor<S> {

    /**
     * Executes the command with the provided context
     * <p>Returns false if the execution wasn't successful</p>
     *
     * @param context Context
     * @return boolean
     */
    boolean execute(Context<S> context);

}
