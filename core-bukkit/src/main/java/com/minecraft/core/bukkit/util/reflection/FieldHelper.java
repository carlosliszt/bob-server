/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.reflection;

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
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
    }
}