/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.command;

import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.command.command.CommandHolder;

import java.util.Optional;

/**
 * The BukkitChildCommand is an inherited command from
 * the parent command {@link BukkitCommand}
 *
 * <p>As a example, /help is a {@link BukkitCommand} but in /help list,
 * the list argument is a Child command. The /ħelp continues to be a parent command.</p>
 */
public class BukkitChildCommand extends BukkitCommand {

    private final BukkitCommand parentCommand;

    /**
     * Creates a new Child command with the name and parent command provided.
     *
     * @param frame         BukkitFrame
     * @param name          String
     * @param parentCommand BukkitCommand
     */
    public BukkitChildCommand(BukkitFrame frame, String name, BukkitCommand parentCommand) {
        super(frame, name, parentCommand.getPosition() + 1);
        this.parentCommand = parentCommand;
    }

    @Override
    public String getFancyName() {
        return parentCommand.getFancyName() + " " + getName();
    }

    public Optional<CommandHolder<?, ?>> getParentCommand() {
        return Optional.of(parentCommand);
    }
}
