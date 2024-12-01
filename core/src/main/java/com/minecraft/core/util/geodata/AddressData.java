/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.geodata;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public class AddressData {

    private static final List<String> blockedCompanies =
            Arrays.asList("AS8075",
                    "AS16276", "AS35540", "AS14061",
                    "AS27715", "AS263590", "AS7162",
                    "AS13878", "AS53107", "AS15169", "AS262287");

    private final String address;
    private boolean trusted, banned;
    private final String organization;
    private final String city;
    private final String state;
    private final String country;
    @Setter
    private int register;

    public void setBanned(boolean bool) {
        this.banned = bool;
        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            jedis.setex(address + "-data", 604800, Constants.GSON.toJson(this));
            jedis.setex(address + "-punish", 1296000, (isBanned() ? "1" : "0"));
        }
    }

    public void completelyTrust() {
        this.trusted = true;
        this.banned = false;

        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            jedis.setex(address + "-data", 604800, Constants.GSON.toJson(this));
            jedis.setex(address + "-punish", 1296000, "0");
        }
    }

    public static AddressData resolve(String address, JsonObject jsonObject) {

        String organization = jsonObject.has("asn") ? jsonObject.get("asn").getAsString() : "...";
        String city = jsonObject.has("city") ? jsonObject.get("city").getAsString() : "...";
        String state = jsonObject.has("region") ? jsonObject.get("region").getAsString() : "...";
        String country = jsonObject.has("country") ? jsonObject.get("country").getAsString() : "...";

        boolean untrusted =
                jsonObject.get("proxy").getAsString().equals("yes")
                        || jsonObject.get("type").getAsString().equals("VPN")
                        || blockedCompanies.contains(organization);

        boolean banned = isPunished(address);

        return new AddressData(address, !untrusted, banned, organization, city, state, country, 0);
    }

    public void print(Context<?> context) {
        boolean isAdmin = context.getAccount().getProperty("isAdmin").getAsBoolean();

        context.sendMessage("§aLocalização:");
        context.sendMessage("  §7País: %s", getCountry());
        context.sendMessage("  §7Estado: %s", getState());

        if (context.getAccount().hasPermission(Rank.ADMINISTRATOR))
            context.sendMessage("  §7Cidade: %s", getCity());

        if (isAdmin) {
            context.sendMessage("  §7ASN: %s", getOrganization());
            context.sendMessage("  §7Confiável: %s", (isTrusted() && !isBanned() ? "Sim" : "Não"));
        }
    }

    private static boolean isPunished(String address) {
        try (Jedis jedis = Constants.getRedis().getResource(Redis.DATABASE_CACHE)) {
            return jedis.exists(address + "-punish") && jedis.get(address + "-punish").equals("1");
        }
    }
}
