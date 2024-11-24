/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.database.mojang.request.list;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.database.mojang.request.API;
import com.minecraft.core.database.redis.Redis;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class MojangVanillaAPI extends API {

    public MojangVanillaAPI() {
        setURL("https://api.mojang.com/users/profiles/minecraft/");
    }

    @Override
    public UUID request(String name) {
        HttpRequest request = HttpRequest.get(getURL() + name).connectTimeout(5000).readTimeout(6500).userAgent("Administrator/1.0.0").acceptJson();

        if (request.ok()) {
            JsonObject object = Constants.JSON_PARSER.parse(request.reader()).getAsJsonObject();
            String id = object.get("id").getAsString();
            String uniqueId = id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

            try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
                jedis.setex(name.toLowerCase(), 172800, uniqueId);
            }

            return UUID.fromString(uniqueId);
        } else if (request.notFound() || request.noContent())
            return null;
        else
            throw new NullPointerException("Failed to check player username! (" + name + ")");
    }
}
