/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.executor;

import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.exception.CommandException;
import com.minecraft.core.command.executor.CompleterExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.List;

public class BukkitCompleterExecutor implements CompleterExecutor<CommandSender> {

    private final Method method;
    private final Object holder;

    public BukkitCompleterExecutor(Method method, Object holder) {
        final Class<?> returnType = method.getReturnType();
        final Class<?>[] parameters = method.getParameterTypes();

        if (!List.class.isAssignableFrom(returnType)) {
            throw new CommandException("Illegal return type, '" + method.getName());
        }

        if (parameters.length > 1 || (parameters.length == 1 && !Context.class.isAssignableFrom(parameters[0]))) {
            throw new CommandException("Illegal parameter type, '" + method.getName());
        }

        this.method = method;
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> execute(Context<CommandSender> context) {
        final Class<?>[] types = method.getParameterTypes();
        try {
            if (types.length == 0) {
                return (List<String>) method.invoke(holder);
            }

            if (types.length == 1 && types[0] == Context.class) {
                return (List<String>) method.invoke(holder, context);
            }

            return null;
        } catch (Exception exception) {
            return null;
        }
    }

}
