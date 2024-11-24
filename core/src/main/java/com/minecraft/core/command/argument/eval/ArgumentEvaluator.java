/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.argument.eval;

import com.minecraft.core.command.argument.Argument;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.exception.CommandException;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.util.ArrayUtil;
import com.minecraft.core.command.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ArgumentEvaluator is the main argument
 * parser of the framework, it reads the argument
 * and provides a useful object to invoke the methods.
 *
 * @param <S> Argument
 */
@Getter
@RequiredArgsConstructor
public class ArgumentEvaluator<S> {

    private final List<Argument<?>> argumentList;

    public Object[] parseArguments(Context<S> context) {
        Object[] parameters = new Object[0];
        AtomicInteger currentArg = new AtomicInteger(0);

        for (Argument<?> argument : argumentList) {
            if (Context.class.isAssignableFrom(argument.getType())) {
                parameters = ArrayUtil.add(parameters, context);
                continue;
            }

            String arg = readFullString(currentArg, context);
            if (arg == null) {
                if (!argument.isNullable()) {
                    throw new CommandException(MessageType.INCORRECT_USAGE, null);
                }

                parameters = ArrayUtil.add(parameters, argument.getDefaultValue());
                currentArg.incrementAndGet();
                continue;
            }

            Object object;
            if (argument.isArray()) {
                object = Array.newInstance(argument.getType(), 0);
                do {
                    object = ArrayUtil.add(
                            (Object[]) object,
                            argument.getAdapter().convertNonNull(arg)
                    );
                } while ((arg = readFullString(currentArg, context)) != null);
            } else {
                object = argument.getAdapter().convert(arg);
            }

            parameters = ArrayUtil.add(parameters, object);
        }

        return parameters;
    }

    private String readFullString(AtomicInteger currentArg, Context<S> context) {
        String arg = context.getArg(currentArg.get());
        if (arg == null) return null;

        currentArg.incrementAndGet();
        if (arg.charAt(0) == '"') {
            final StringBuilder builder = new StringBuilder(arg.substring(1));
            while ((arg = context.getArg(currentArg.get())) != null) {
                builder.append(" ");
                currentArg.incrementAndGet();

                final int length = arg.length();
                if (arg.charAt(length - 1) == '"' && (length == 1 || arg.charAt(length - 2) != '\\')) {
                    builder.append(arg, 0, length - 1);
                    break;
                }

                builder.append(arg);
            }

            return builder.toString().replace("\\\"", "\"");
        }

        return arg;
    }


    public String buildUsage(String name) {
        final StringBuilder builder = new StringBuilder(name);
        for (Argument<?> argument : argumentList) {
            if (Context.class.isAssignableFrom(argument.getType())) continue;

            builder.append(argument.isNullable() ? " [" : " <");
            builder.append(StringUtil.uncapitalize(argument.getType().getSimpleName()));

            if (argument.isArray()) builder.append("...");

            builder.append(argument.isNullable() ? "]" : ">");
        }

        return builder.toString();
    }

}
