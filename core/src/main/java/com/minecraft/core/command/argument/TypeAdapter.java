/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.argument;

public interface TypeAdapter<T> {

    T convert(String raw);

    default T convertNonNull(String raw) {
        final T result = convert(raw);

        if (result == null) {
            throw new NullPointerException();
        }

        return result;
    }
}
