/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.database.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.database.enums.Columns;

public class Data {

    private final Columns column;
    private Object data;
    private boolean changed;

    public Data(final Columns column, final Object data) {
        this.data = data;
        this.column = column;
        this.changed = false;
    }

    /**
     * Defines the value for the data.
     */
    public void setData(final Object data) {
        if (!this.data.equals(data))
            changed = true;
        this.data = data;
    }

    public boolean isDefaultValue() {
        return column.getDefaultValue().equals(this.data);
    }

    /**
     * Cast the data as an Object
     *
     * @return Object
     */
    public Object getAsObject() {
        return this.data;
    }

    /**
     * Cast the data as a String
     *
     * @return String
     */
    public String getAsString() {
        return (String) this.data;
    }

    /**
     * Cast the data as an Integer
     *
     * @return Integer
     */
    public Integer getAsInt() {
        return (Integer) this.data;
    }

    /**
     * Cast the data as an int and format
     *
     * @return String
     */
    public String getAsInteger() {
        return Constants.DECIMAL_FORMAT.format((int) this.data);
    }

    /**
     * Cast the data as a Long
     *
     * @return Long
     */
    public Long getAsLong() {
        Object value = this.data;
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else {
            throw new ClassCastException("Cannot cast " + value.getClass() + " to Long");
        }
    }

    /**
     * Cast the data as a Boolean
     *
     * @return Boolean
     */
    public Boolean getAsBoolean() {
        return (Boolean) this.data;
    }

    /**
     * Cast the data as a JsonObject
     *
     * @return JsonObject
     */
    public JsonObject getAsJsonObject() {
        return (JsonObject) this.data;
    }

    /**
     * Cast the data as a JsonArray
     *
     * @return JsonArray
     */
    public JsonArray getAsJsonArray() {
        return (JsonArray) this.data;
    }

    @Override
    public String toString() {
        if (this.data == null) {
            return "null";
        } else if (data instanceof JsonArray || data instanceof JsonObject) {
            return Constants.GSON.toJson(data);
        } else {
            return data.toString();
        }
    }

    public boolean hasChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Columns getColumn() {
        return column;
    }
}