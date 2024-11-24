/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command;

import com.minecraft.core.command.argument.AdapterMap;
import com.minecraft.core.command.argument.TypeAdapter;
import com.minecraft.core.command.argument.eval.MethodEvaluator;
import com.minecraft.core.command.command.CommandHolder;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.executor.CompleterExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * The CommandFrame is the core of the framework,
 * it registers the commands, adapters {@link AdapterMap}
 */
public interface CommandFrame<P, S, C extends CommandHolder<S, ? extends C>> {

    P getPlugin();

    AdapterMap getAdapterMap();

    Map<String, C> getCommandMap();

    MethodEvaluator getMethodEvaluator();

    Executor getExecutor();

    C getCommand(String name);

    /**
     * Registers a new Adapter from that type.
     *
     * @param type    Class
     * @param adapter TypeAdapter
     * @param <T>     Generic value for the type
     */
    default <T> void registerAdapter(Class<T> type, TypeAdapter<T> adapter) {
        getAdapterMap().put(type, adapter);
    }

    /**
     * Registers multiple command object once
     *
     * @param objects Object...
     */
    void registerCommands(Object... objects);

    /**
     * Registers a single command with the CommandInfo and Executor
     *
     * @param commandInfo     CommandInfo
     * @param commandExecutor CommandExecutor
     */
    void registerCommand(CommandInfo commandInfo, CommandExecutor<S> commandExecutor);

    void registerCompleter(String name, CompleterExecutor<S> completerExecutor);

    /**
     * Unregisters the command with the provided name
     *
     * @param name String
     * @return Boolean
     */
    boolean unregisterCommand(String name);
}
