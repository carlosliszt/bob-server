/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.command.argument;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountStorage;
import com.minecraft.core.enums.Rank;

import java.util.HashMap;

/**
 * The AdapterMap contains adapters such as
 * primitive values and such.
 *
 * <p>It can be created with the default primitive types
 * or totally empty, after other values can be added</p>
 */
public class AdapterMap extends HashMap<Class<?>, TypeAdapter<?>> {

    /**
     * Creates a new AdapterMap that can be empty
     * or registered with the default values.
     *
     * @param registerDefault Boolean
     */
    public AdapterMap(boolean registerDefault) {
        super();
        if (!registerDefault) return;

        put(String.class, String::valueOf);
        put(Character.class, argument -> argument.charAt(0));
        put(Integer.class, Integer::valueOf);
        put(Double.class, Double::valueOf);
        put(Float.class, Float::valueOf);
        put(Long.class, Long::valueOf);
        put(Boolean.class, this::getBoolean);
        put(Byte.class, Byte::valueOf);
        put(Account.class, str -> AccountStorage.getAccountByName(str, true));
        put(Rank.class, Rank::valueOf);

        put(Character.TYPE, argument -> argument.charAt(0));
        put(Integer.TYPE, Integer::parseInt);
        put(Double.TYPE, Double::parseDouble);
        put(Float.TYPE, Float::parseFloat);
        put(Long.TYPE, Long::parseLong);
        put(Boolean.TYPE, this::getBoolean);
        put(Byte.TYPE, Byte::parseByte);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> put(Class<T> key, TypeAdapter<T> value) {
        return (TypeAdapter<T>) super.put(key, value);
    }

    public boolean getBoolean(String str) {
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on"))
            return true;
        else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("off"))
            return false;
        throw new NumberFormatException("No boolean value found.");
    }
}
