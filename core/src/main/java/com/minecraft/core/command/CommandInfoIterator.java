/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command;

import com.minecraft.core.command.command.CommandHolder;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;

/**
 * The CommandInfoIterator is a Iterator for
 * CommandHolder, it can access the next command from the list.
 */
@RequiredArgsConstructor
public class CommandInfoIterator implements Iterator<CommandHolder<?, ?>> {

    private final CommandHolder<?, ?> root;

    private int index = -1;
    private CommandInfoIterator current;

    @Override
    public boolean hasNext() {
        return (current != null && current.hasNext()) || index < root.getChildCommandList().size();
    }

    @Override
    public CommandHolder<?, ?> next() {
        if (index == -1) {
            index++;
            return root;
        }

        if (current == null || !current.hasNext()) {
            current = new CommandInfoIterator(root.getChildCommandList().get(index));
            index++;
        }

        return current.next();
    }

}
