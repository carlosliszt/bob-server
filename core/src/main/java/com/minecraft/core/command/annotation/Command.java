/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.annotation;

import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * Defines the command name, sub-commands are split with dots
     * <p><p>
     * <b>Example:</b><p>
     * parentcommand<p>
     * parentcommand.subcommand<p>
     *
     * @return the command name
     */
    String name();

    /**
     * @return the command aliases
     */
    String[] aliases() default {};

    /**
     * @return the command description
     */
    String description() default "";

    /**
     * @return the command usage example
     */
    String usage() default "";

    /**
     * @return the required rank to execute the command
     */
    Rank rank() default Rank.MEMBER;

    /**
     * @return the command platform
     */
    Platform platform() default Platform.BOTH;

    /**
     * Tells the executor how to run the command,
     * some implementations might ignore this option as they are async by default.
     * This option requires an executor.
     *
     * @return whether the command should be ran asynchronously
     */
    boolean async() default false;
}
