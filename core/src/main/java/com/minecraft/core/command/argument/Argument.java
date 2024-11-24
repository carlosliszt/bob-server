/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.argument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * The model for each argument in the command.
 * <p>It contains the main information of each argument
 * such as it's type and name.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class Argument<T> {

    private final String name;

    private final Class<T> type;
    private final TypeAdapter<T> adapter;

    private final T defaultValue;

    private final boolean isNullable;
    private final boolean isArray;

}
