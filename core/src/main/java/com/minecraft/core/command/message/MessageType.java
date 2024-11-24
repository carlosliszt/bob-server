/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.command.message;

import com.minecraft.core.command.command.CommandHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The MessageType contains the default messages for the errors
 * as well that values can be edited.
 *
 * <p>Placeholders can be put on each of those errors</p>
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    /**
     * Used when a error is thrown, the {error} can be
     * used to send the message of error
     */
    ERROR("unexpected_error") {
        @Override
        public String getDefault(CommandHolder<?, ?> commandHolder) {
            return "";
        }
    },
    /**
     * Used when a player doesn't have a permission,
     * the {permission} can be used to send the permission
     */
    NO_PERMISSION("command.insufficient_permission") {
        @Override
        public String getDefault(CommandHolder<?, ?> commandHolder) {
            return "";
        }
    },
    /**
     * Used when a player doesn't use the command correctly,
     * the {usage} can be used to send the correct usage
     */
    INCORRECT_USAGE("command.usage") {
        @Override
        public String getDefault(CommandHolder<?, ?> commandHolder) {
            return "/" + commandHolder.getUsage();
        }
    },
    /**
     * Used when a player doesn't use the platform correctly,
     * the {platform} can be used to send the correct platform
     */
    INCORRECT_TARGET("command.incompatible_platform") {
        @Override
        public String getDefault(CommandHolder<?, ?> commandHolder) {
            return commandHolder.getCommandInfo().getPlatform().name().toLowerCase();
        }
    };

    private final String messageKey;

    public abstract String getDefault(CommandHolder<?, ?> commandHolder);
}