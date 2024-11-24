/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.argument.eval;

import com.minecraft.core.command.annotation.Optional;
import com.minecraft.core.command.argument.AdapterMap;
import com.minecraft.core.command.argument.Argument;
import com.minecraft.core.command.argument.TypeAdapter;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.exception.CommandException;
import com.minecraft.core.command.exception.NoSuchConverterException;
import com.minecraft.core.command.util.ArrayUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * The ArgumentEvaluator is the main method
 * validator of the framework.
 *
 * <p>It validates if the method contains the correct
 * parameters and creates an {@link Optional} parameter
 */
@RequiredArgsConstructor
public class MethodEvaluator {

    private final AdapterMap adapterMap;

    @SuppressWarnings("unchecked")
    public List<Argument<?>> evaluateMethod(Method method) {
        final List<Argument<?>> argumentList = new ArrayList<>();

        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            Class type = parameter.getType();
            boolean isArray = type.isArray();
            final Argument.ArgumentBuilder builder = Argument
                    .builder()
                    .name(parameter.getName())
                    .type(type)
                    .isArray(isArray);

            if (Context.class.isAssignableFrom(type)) {
                argumentList.add(builder.build());
                continue;
            }

            if (isArray) {
                if (i != parameters.length - 1) {
                    throw new CommandException("Arrays must be the last parameter in a command, " + method.getName());
                }

                builder.type(type = type.getComponentType());
            }

            builder.adapter(adapterMap.get(type));

            final Optional optional = parameter.getDeclaredAnnotation(Optional.class);
            if (optional == null) {
                argumentList.add(builder.build());
                continue;
            }

            argumentList.add(createOptional(method, type, isArray, optional.def(), builder));
        }

        return argumentList;
    }

    @SuppressWarnings("unchecked")
    private Argument createOptional(Method method, Class type, boolean isArray,
                                    String[] def, Argument.ArgumentBuilder builder) {

        if (type.isPrimitive() && def.length == 0) {
            throw new IllegalArgumentException("Use wrappers instead of primitive types for nullability, "
                    + method.getName());
        }

        final TypeAdapter<?> adapter = adapterMap.get(type);
        if (adapter == null) throw new NoSuchConverterException(type);

        builder.isNullable(true);

        if (isArray && def.length != 0) {
            builder.defaultValue(createArray(type, adapter, def));
        } else if (def.length != 0) {
            builder.defaultValue(adapter.convertNonNull(def[0]));
        }

        return builder.build();
    }

    private Object[] createArray(Class type, TypeAdapter<?> adapter, String[] def) {
        Object[] value = (Object[]) Array.newInstance(type, 0);
        for (String arg : def) {
            value = ArrayUtil.add(value, adapter.convertNonNull(arg));
        }

        return value;
    }

}
