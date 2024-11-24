/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.executor;

import com.minecraft.core.command.command.Context;

import java.util.List;

@FunctionalInterface
public interface CompleterExecutor<S> {

    List<String> execute(Context<S> context);

}
