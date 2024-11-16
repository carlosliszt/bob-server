/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.CommandSender;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class CacheuuidCommand implements ProxyInterface {

    @Command(name = "cacheuuid", platform = Platform.BOTH, rank = Rank.ADMINISTRATOR, usage = "cacheuuid <player...>")
    public void handleCommand(Context<CommandSender> context, String[] users) {
        async(() -> {
            context.sendMessage("§cA API que está sendo utilizada é da Europa, o processo pode demorar um pouco...");

            for (String username : users) {

                long l1 = System.currentTimeMillis();

                UUID uniqueId = request(username);

                if (uniqueId == null) {
                    context.sendMessage("§c" + username + " não é registrado no Minecraft.");
                    return;
                }

                context.sendMessage("§aVocê adicionou %s(%s) ao cache! [%sms]", username, uniqueId.toString(), System.currentTimeMillis() - l1);
            }
        });
    }

    protected UUID request(String name) {
        HttpRequest request = HttpRequest.get("https://api.minetools.eu/uuid/" + name).connectTimeout(6500).readTimeout(6500).userAgent("Administrator/1.0.0").acceptJson();

        if (request.ok()) {

            JsonObject object = Constants.JSON_PARSER.parse(request.reader()).getAsJsonObject();

            if (!object.get("status").getAsString().equals("OK"))
                return null;

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