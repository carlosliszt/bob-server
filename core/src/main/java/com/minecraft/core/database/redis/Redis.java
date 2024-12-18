/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.database.redis;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.system.AccountDeposit;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.function.Consumer;

@Getter
public class Redis implements AutoCloseable {

    public static String PROFILE_UPDATE_CHANNEL = "a";
    public static String SERVER_REDIRECT_CHANNEL = "b";
    public static String NICK_DISGUISE_CHANNEL = "c";
    public static String LANGUAGE_UPDATE_CHANNEL = "d";
    public static String RANK_UPDATE_CHANNEL = "e";
    public static String FLAG_UPDATE_CHANNEL = "f";
    public static String SERVER_COMMUNICATION_CHANNEL = "g";
    public static String PROXY_COUNT_CHANNEL = "h";
    public static String SKIN_CHANGE_CHANNEL = "i";
    public static String OPEN_EVENT_CHANNEL = "j";
    public static String PREFERENCES_UPDATE_CHANNEL = "k";
    public static String CLAN_INTEGRATION_CHANNEL = "l";
    public static String NICK_ADD_CHANNEL = "m";
    public static String CLAN_TAG_UPDATE = "n";
    public static String FRIEND_UPDATE_CHANNEL = "o";

    public static int DATABASE_CACHE = 1;
    public static int SERVER_CACHE = 2;
    public static int GIFTCODE_CACHE = 3;

    private JedisPool jedisPool;

    public Redis() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(32);
        jedisPoolConfig.setMaxWaitMillis(10000);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setMinIdle(5);
        jedisPoolConfig.setBlockWhenExhausted(false);

        this.jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379, 5000);
        Constants.setAccountDeposit(new AccountDeposit(getResource()));
    }

    public void publish(String channel, String type, Consumer<JsonObject> jsonConsumer) {
        JsonObject message = new JsonObject();
        message.addProperty("type", type);

        JsonObject data = new JsonObject();
        jsonConsumer.accept(data);
        message.add("data", data);

        try (Jedis jedis = getResource()) {
            jedis.publish(channel, message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = getResource()) {
            jedis.publish(channel, message);
        }
    }

    @Override
    public void close() throws Exception {
        jedisPool.close();
        jedisPool = null;
    }

    public Jedis getResource() {
        return jedisPool.getResource();
    }

    public Jedis getResource(int dbIndex) {
        Jedis jedis = this.getResource();
        jedis.select(dbIndex);
        return jedis;
    }

}