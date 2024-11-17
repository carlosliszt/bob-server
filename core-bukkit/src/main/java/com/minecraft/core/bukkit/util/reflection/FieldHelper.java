/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.reflection;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("WeakerAccess")
public class FieldHelper {

    public static <T> T getValue(Object instance, String fieldName) {
        return getValue(instance.getClass(), instance, fieldName);
    }

    public static <T> T getValue(Class<?> clazz, Object instance, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValue(Object instance, String fieldName, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeNonFinal(Field field) throws Exception {
        field.setAccessible(true);

        VarHandle modifiersHandle = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup())
                .findVarHandle(Field.class, "modifiers", int.class);

        int mods = (int) modifiersHandle.get(field);
        modifiersHandle.set(field, mods & ~Modifier.FINAL);
    }
}