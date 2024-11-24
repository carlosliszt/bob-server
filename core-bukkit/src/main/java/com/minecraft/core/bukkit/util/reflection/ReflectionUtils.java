/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.reflection;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@ParametersAreNonnullByDefault
@ThreadSafe
public final class ReflectionUtils {
    @Nonnull
    public static List<Method> getMethodsWithAnnotations(final Class<?> type, final Class<? extends Annotation>... annotations) {
        final Predicate<Method> predicate = containsAllAnnotations(annotations);
        final List<Method> unfilteredMethods = Lists.newArrayList(type.getMethods());
        final Iterable<Method> filteredMethods = (Iterable<Method>) Iterables.filter((Iterable) unfilteredMethods, (Predicate) predicate);
        return (List<Method>) Lists.newArrayList((Iterable) filteredMethods);
    }

    public static Predicate<Method> containsAllAnnotations(final Class<? extends Annotation>... annotations) {
        Predicate<Method> predicate = Predicates.alwaysTrue();
        for (final Class<? extends Annotation> annotation : annotations) {
            predicate = (Predicate<Method>) Predicates.and((Predicate) predicate, (Predicate) containsAnnotation(annotation));
        }
        return predicate;
    }

    public static Predicate<Method> containsAnnotation(final Class<? extends Annotation> annotation) {
        Preconditions.checkNotNull((Object) annotation, (Object) "annotation cannot be null");
        return (Predicate<Method>) new Predicate<Method>() {
            public boolean apply(@Nullable final Method method) {
                return method.getAnnotation(annotation) != null;
            }
        };
    }

    public static Field[] getFieldsUpTo(@Nonnull final Class<?> type, @Nullable final Class<?> exclusiveParent) {
        Field[] result = type.getDeclaredFields();
        final Class<?> parentClass = type.getSuperclass();
        if (parentClass != null && !parentClass.equals(exclusiveParent)) {
            final Field[] parentClassFields = getFieldsUpTo(parentClass, exclusiveParent);
            result = (Field[]) ObjectArrays.concat(result, (Object[]) parentClassFields, (Class) Field.class);
        }
        return result;
    }

    public static Method[] getMethodsUpTo(@Nonnull final Class<?> type, @Nullable final Class<?> exclusiveParent) {
        Method[] result = type.getDeclaredMethods();
        final Class<?> parentClass = type.getSuperclass();
        if (parentClass != null && !parentClass.equals(exclusiveParent)) {
            final Method[] parentClassFields = getMethodsUpTo(parentClass, exclusiveParent);
            result = (Method[]) ObjectArrays.concat((Object[]) result, (Object[]) parentClassFields, (Class) Method.class);
        }
        return result;
    }

    private ReflectionUtils() {
    }
}
