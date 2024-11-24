/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.variable.loader;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.reflection.FieldHelper;
import com.minecraft.core.bukkit.util.reflection.ReflectionUtils;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.ExecutableVariable;
import com.minecraft.core.bukkit.util.variable.object.SimpleVariable;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.variable.object.VariableValidation;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class VariableLoader {

    private final Set<SimpleVariable> variables = new HashSet<>();

    public void load(VariableStorage... variableStorages) {
        for (VariableStorage variableStorage : variableStorages) {
            Class<?> clazz = variableStorage.getClass();

            for (final Field field : ReflectionUtils.getFieldsUpTo(clazz, null)) {
                if (field.isAnnotationPresent(Variable.class)) {

                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    try {
                        FieldHelper.makeNonFinal(field);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Variable var = field.getAnnotation(Variable.class);

                    SimpleVariable variable = getVariable(var.name());

                    if (variable != null) {
                        BukkitGame.getEngine().getLogger().info("Variable '" + var.name() + "' already exists, updating field...");
                        variable.setField(field);
                        variable.setVariableStorage(variableStorage);
                        return;
                    }


                    variable = new SimpleVariable(var.name(), variableStorage, field, null, var.permission(), true, var.announce());
                    variables.add(variable);

                    BukkitGame.getEngine().getLogger().info("Loaded variable " + variable.getName() + "!");
                }
            }

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(VariableValidation.class)) {

                    if (method.getParameterCount() != 1) {
                        BukkitGame.getEngine().getLogger().info("O metodo " + method.getName() + " deve ter somente 1 parametro.");
                        continue;
                    } else if (!method.getReturnType().getName().equals("boolean")) {
                        BukkitGame.getEngine().getLogger().info("O metodo " + method.getName() + " deve retornar um boolean");
                        continue;
                    }

                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    VariableValidation var = method.getAnnotation(VariableValidation.class);

                    for (String name : var.value()) {
                        SimpleVariable simpleVariable = getVariable(name);

                        if (simpleVariable == null)
                            continue;

                        simpleVariable.setValidator(method);
                    }
                } else if (method.isAnnotationPresent(Variable.class)) {

                    if (method.getParameterCount() > 1)
                        return;

                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    Variable var = method.getAnnotation(Variable.class);

                    ExecutableVariable variable = (ExecutableVariable) getVariable(var.name());

                    if (variable != null) {
                        BukkitGame.getEngine().getLogger().info("Executable variable '" + var.name() + "' already exists, updating field...");
                        variable.setExecutor(method);
                        variable.setVariableStorage(variableStorage);
                        return;
                    }

                    variable = new ExecutableVariable(var.name(), variableStorage, method, var.permission(), true);
                    variables.add(variable);
                    BukkitGame.getEngine().getLogger().info("Loaded executable variable " + variable.getName() + "!");
                }
            }
        }
    }

    public SimpleVariable getVariable(String str) {
        return getVariables().stream().filter(c -> c.getName().equalsIgnoreCase(str)).findFirst().orElse(null);
    }

    public List<SimpleVariable> getVariables(VariableStorage storage) {
        return getVariables().stream().filter(c -> c.getVariableStorage() == storage).collect(Collectors.toList());
    }

}