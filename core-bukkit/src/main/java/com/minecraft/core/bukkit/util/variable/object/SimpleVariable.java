/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.variable.object;

import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
@Setter
public class SimpleVariable {

    private String name;
    private VariableStorage variableStorage;
    private Field field;
    private Method validator;
    private Rank defaultRank;
    private boolean active;
    private boolean annouce;

    public boolean hasMethod() {
        return validator != null;
    }

    public boolean hasField() {
        return field != null;
    }


    public void setValue(Object value) {
        try {
            getField().set(getVariableStorage(), value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getValue() throws IllegalAccessException {
        return getField().get(getVariableStorage());
    }

    public boolean validate(Object value) throws InvocationTargetException, IllegalAccessException {
        if (getValidator() == null)
            return true;
        else
            return (boolean) getValidator().invoke(getVariableStorage(), value);
    }

    public Class<?> getParameter() {
        return getField().getType();
    }
}
