/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.datas;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkinData {

    private String name, value, signature;
    private Source source = Source.UNDEFINED;
    private long updatedAt;

    @AllArgsConstructor
    @Getter
    public enum Source {
        ACCOUNT("Conta"),
        LIBRARY("Biblioteca"),
        CUSTOM("Customizada"),
        FORCED("Forçada"),
        UNDEFINED("...");

        private final String display;
    }

    public JsonObject toJson() {

        JsonObject object = new JsonObject();

        object.addProperty("name", this.name);
        object.addProperty("value", this.value);
        object.addProperty("signature", this.signature);
        object.addProperty("source", this.source.name());
        object.addProperty("updatedAt", updatedAt);
        return object;
    }

    public static SkinData fromJson(JsonObject value) {
        return Constants.GSON.fromJson(value, SkinData.class);
    }

    @Override
    public String toString() {
        return "SkinData{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }

    public boolean isInvalid() {
        return getValue() == null || getSignature() == null || getValue().isEmpty() || getSignature().isEmpty();
    }
}