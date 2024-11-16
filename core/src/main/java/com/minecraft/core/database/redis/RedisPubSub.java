/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.database.redis;

import com.minecraft.core.Constants;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

@Getter
public class RedisPubSub implements Runnable {

    private final JedisPubSub jedisPubSub;
    private final String[] channels;

    public RedisPubSub(JedisPubSub jedisPubSub, String... channels) {
        this.jedisPubSub = jedisPubSub;
        this.channels = channels;
    }

    @Override
    public void run() {
        boolean connected = true;

        try (Jedis jedis = Constants.getRedis().getResource()) {
            jedis.subscribe(this.jedisPubSub, channels);
        } catch (Exception exception) {
            connected = false;
        }

        if (!connected) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.run();
        }
    }

}