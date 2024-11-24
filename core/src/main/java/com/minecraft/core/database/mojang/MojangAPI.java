/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.database.mojang;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.database.mojang.request.API;
import com.minecraft.core.database.mojang.request.list.MojangVanillaAPI;
import com.minecraft.core.database.mojang.request.list.PlayerDatabaseAPI;
import com.minecraft.core.database.redis.Redis;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MojangAPI {

    private final List<com.minecraft.core.database.mojang.request.API> availableAPI;

    public MojangAPI() {
        this.availableAPI = new ArrayList<>();
        availableAPI.add(new MojangVanillaAPI());
       // availableAPI.add(new PlayerDatabaseAPI());
    }

    public UUID getUniqueId(String name) {
        UUID uuid = null;

        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            if (jedis.exists(name.toLowerCase()))
                uuid = UUID.fromString(jedis.get(name.toLowerCase()));
        }

        if (uuid == null) {
            uuid = requestUUID(name);
        }

        return uuid;
    }

    public String getNickname(UUID uuid) {
        String username = null;

        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            if (jedis.exists(uuid.toString()))
                username = jedis.get(uuid.toString());
        }

        if (username == null) {
            username = requestName(uuid);
        }

        return username;
    }

    public Property getProperty(UUID uuid) {
        Property property = null;

        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            if (jedis.exists(uuid.toString() + ":textures")) {
                property = Constants.GSON.fromJson(jedis.get(uuid + ":textures"), Property.class);
            }
        }

        if (property == null) {
            property = requestTextures(uuid);
        }

        return property;
    }

    private UUID requestUUID(String name) {
        API api = availableAPI.get(Constants.RANDOM.nextInt(availableAPI.size()));
        return api.request(name);
    }

    private String requestName(UUID uuid) {
        HttpRequest request = HttpRequest.get("https://api.mojang.com/user/profiles/" + StringUtils.replace(uuid.toString(), "-", "") + "/names").connectTimeout(5000).readTimeout(5000).userAgent("Administrator/1.0.0").acceptJson();

        if (request.ok()) {
            JsonArray jsonArray = Constants.JSON_PARSER.parse(request.reader()).getAsJsonArray();
            JsonObject jsonObject = jsonArray.get(jsonArray.size() - 1).getAsJsonObject();
            String username = jsonObject.get("name").getAsString();

            try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
                jedis.setex(uuid.toString(), 57600, username);
            }

            request.disconnect();
            return username;
        }

        return null;
    }

    public List<Name> requestNames(UUID uuid) {
        HttpRequest request = HttpRequest.get("https://api.mojang.com/user/profiles/" + StringUtils.replace(uuid.toString(), "-", "") + "/names").connectTimeout(5000).readTimeout(5000).userAgent("Administrator/1.0.0").acceptJson();

        if (request.ok()) {
            JsonArray jsonArray = Constants.JSON_PARSER.parse(request.reader()).getAsJsonArray();
            Iterator<JsonElement> iterator = jsonArray.iterator();

            List<Name> names = new ArrayList<>();

            while (iterator.hasNext()) {
                JsonObject object = iterator.next().getAsJsonObject();

                String nickname = object.get("name").getAsString();

                if (nickname == null) {
                    iterator.remove();
                    continue;
                }

                long date = (object.has("changedToAt") ? object.get("changedToAt").getAsLong() : -1L);

                names.add(new Name(nickname, date));
            }

            request.disconnect();

            return names;
        }

        return null;
    }

    private Property requestTextures(UUID uuid) {
        HttpRequest request = HttpRequest.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false").connectTimeout(5000).readTimeout(5000).userAgent("Administrator/1.0.0").accept(HttpRequest.CONTENT_TYPE_JSON);

        if (request.ok()) {
            JsonObject json = request.json().getAsJsonObject();
            JsonObject prop = json.getAsJsonArray("properties").get(0).getAsJsonObject();
            Property property = Constants.GSON.fromJson(prop, Property.class);

            try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
                jedis.setex(uuid + ":textures", 10800, prop.toString());
            }

            request.disconnect();

            return property;
        }

        return null;
    }

    @Getter
    @AllArgsConstructor
    public static class Name {
        private String name;
        private long changedToAt;

        public boolean hasDate() {
            return changedToAt != -1;
        }
    }

}