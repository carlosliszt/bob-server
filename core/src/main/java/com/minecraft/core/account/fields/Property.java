/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.account.fields;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Property {

    @Getter
    private final String key;
    private Object value;

    /**
     * Cast the property value as a specific Class
     *
     * @return object
     */
    public <T> T getAs(Class<T> object) {
        return object.cast(this.value);
    }

    /**
     * Cast the property value as a Boolean
     *
     * @return Boolean
     */
    public boolean getAsBoolean() {
        return this.value != null && (boolean) this.value;
    }

    /**
     * Cast the property value as a String
     *
     * @return String
     */
    public String getAsString() {
        return (String) this.value;
    }

    /**
     * Cast the property value as an Int
     *
     * @return Integer
     */
    public int getAsInt() {
        return (int) this.value;
    }

    /**
     * Cast the property value as a Long
     *
     * @return Long
     */
    public long getAsLong() {
        return (long) this.value;
    }

    /**
     * Cast the property value as an Object
     *
     * @return Object
     */
    public Object getAsObject() {
        return this.value;
    }

    /**
     * Defines the value for the property
     */
    public void setValue(Object object) {
        this.value = object;
    }

    /**
     * Builds the property
     */
    public static Property build(String key, Object value) {
        return new Property(key, value);
    }

}
