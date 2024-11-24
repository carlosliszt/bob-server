/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.variable.object;

import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.parameter.NoParameters;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
public class ExecutableVariable extends SimpleVariable {

    private Method executor;

    public ExecutableVariable(String name, VariableStorage variableStorage, Method executor, Rank defaultRank, boolean active) {
        super(name, variableStorage, null, null, defaultRank, active, false);
        this.executor = executor;
    }

    @Override
    public Class<?> getParameter() {
        if (this.getExecutor().getParameterCount() == 0) {
            return NoParameters.class;
        }
        return this.getExecutor().getParameterTypes()[0];
    }

    @Override
    public Object getValue() throws IllegalAccessException {
        return "unknown";
    }

    @Override
    public boolean validate(Object value) {
        return true;
    }
}
