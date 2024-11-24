/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.command;

import com.minecraft.core.command.CommandInfoIterator;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.executor.CompleterExecutor;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * The CommandHolder is the main implementation of the
 * Command, it contains the main information about
 * that command {@link CommandInfo} position and Parent/Child commands
 */
public interface CommandHolder<S, C extends CommandHolder<S, C>> extends Iterable<CommandHolder<?, ?>> {

    int getPosition();

    CommandExecutor<S> getCommandExecutor();

    CompleterExecutor<S> getCompleterExecutor();

    default Optional<CommandHolder<?, ?>> getParentCommand() {
        return Optional.empty();
    }

    List<C> getChildCommandList();

    C getChildCommand(String name);

    CommandInfo getCommandInfo();

    String getName();

    String getFancyName();

    List<String> getAliasesList();

    String getPermission();

    String getUsage();

    String getDescription();

    default boolean equals(String name) {
        if (getName().equalsIgnoreCase(name)) {
            return true;
        }

        for (String alias : getAliasesList()) {
            if (alias.equalsIgnoreCase(name)) return true;
        }

        return false;
    }

    @Override
    default Iterator<CommandHolder<?, ?>> iterator() {
        return new CommandInfoIterator(this);
    }
}
