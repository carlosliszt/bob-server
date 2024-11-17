/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.geodata;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.database.redis.Redis;
import lombok.Getter;
import lombok.NonNull;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DataResolver {
    @Getter
    private final static DataResolver instance = new DataResolver("991z09-4341q8-776803-4qw930");

    private static String key;
    private static final AddressData BLANK = new AddressData("...", true, false, "...", "...", "...", "...", 0);
    private static final String API = "http://proxycheck.io/v2/%s?key=%s&risk=1&vpn=1&asn=1";

    public DataResolver(String f) {
        key = f;
    }

    @Getter
    private final LoadingCache<String, Optional<AddressData>> cache = CacheBuilder.newBuilder().expireAfterWrite(12L, TimeUnit.HOURS)
            .build(new CacheLoader<String, Optional<AddressData>>() {
                @Override
                public Optional<AddressData> load(@NonNull String address) {
                    return Optional.ofNullable(handleData(address));
                }
            });

    public AddressData getData(String address) {
        try {
            return cache.get(address).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BLANK;
    }

    private static AddressData handleData(String address) {
        AddressData addressData = null;

        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            if (jedis.exists(address + "-data"))
                addressData = Constants.GSON.fromJson(jedis.get(address + "-data"), AddressData.class);
        }

        if (addressData == null) {
            addressData = request(address);
        }

        return addressData;
    }

    private static AddressData request(String address) {

        String statement = String.format(API, address, key);
        HttpRequest request = HttpRequest.get(statement).connectTimeout(5000).readTimeout(5000).userAgent("Administrator/1.0.0").acceptJson();

        if (request.ok()) {
            JsonObject object = Constants.JSON_PARSER.parse(request.reader("UTF-8")).getAsJsonObject();

            if (!object.get("status").getAsString().equals("ok")) {
                return BLANK;
            }

            JsonObject resolve = object.get(address).getAsJsonObject();
            AddressData addressData = AddressData.resolve(address, resolve);

            try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
                jedis.setex(address + "-data", 604800, Constants.GSON.toJson(addressData));
            }

            return addressData;
        }
        return BLANK;
    }
}
