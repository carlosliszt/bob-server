/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.scheduler;

import com.minecraft.core.Constants;
import com.minecraft.core.database.redis.Redis;
import net.md_5.bungee.api.ProxyServer;

public class CountWatchScheduler implements Runnable {

    private int lastCount;

    public void run() {

        int count = ProxyServer.getInstance().getOnlineCount();

        if (lastCount == count)
            return;

        Constants.getRedis().publish(Redis.PROXY_COUNT_CHANNEL, "" + lastCount);
        this.lastCount = count;
    }
}
