/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.minecraft.core.account.AccountStorage;
import com.minecraft.core.account.system.AccountDeposit;
import com.minecraft.core.clan.service.ClanService;
import com.minecraft.core.database.mojang.MojangAPI;
import com.minecraft.core.database.mysql.MySQL;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.party.PartyStorage;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.server.ServerType;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Constants {

    @Getter
    public static final AccountStorage accountStorage = new AccountStorage();

    public static void setMySQL(MySQL mySQL) {
        Constants.mySQL = mySQL;
    }
    @Getter
    public static final MojangAPI mojangAPI = new MojangAPI();


    public static void setRedis(Redis redis) {
        Constants.redis = redis;
    }


    /**
     * Asynchronous Thread
     */
    public static final ExecutorService ASYNC = Executors.newCachedThreadPool(new ThreadFactoryBuilder().build());

    /**
     * Default Strings
     */
    public static final String SERVER_NAME = System.getProperty("server_name", "BlazeMC");
    public static final String SERVER_WEBSITE = System.getProperty("server_website", "www.blazemc.com.br");
    public static final String SERVER_DISCORD = System.getProperty("server_discord", "discord.gg/mariaumfc");
    public static final String SERVER_STORE = System.getProperty("server_store", "loja.blazemc.com.br");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###,###,###,###.##");
    public static final DecimalFormat SIMPLE_DECIMAL_FORMAT = create();
    public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Default objects
     */
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final JsonParser JSON_PARSER = new JsonParser();
    @Getter
    public static final PartyStorage partyStorage = new PartyStorage();
    public static final Pattern NICKNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");
    @Getter
    private static final ClanService clanService = new ClanService();
    /**
     * MySQL connection
     */
    @Getter
    public static MySQL mySQL;
    /**
     * Redis connection
     */
    @Getter
    public static Redis redis;
    @Getter
    public static ServerType serverType = ServerType.UNKNOWN, lobbyType = ServerType.UNKNOWN;
    @Getter
    public static ServerStorage serverStorage;
    @Getter
    public static AccountDeposit accountDeposit;

    public static ServerCategory getServerCategory() {
        return serverType.getServerCategory();
    }

    public static void setLobbyType(ServerType lobbyType) {
        Constants.lobbyType = lobbyType;
    }

    public static void setServerStorage(ServerStorage serverStorage) {
        Constants.serverStorage = serverStorage;
    }

    public static void setServerType(ServerType serverType) {
        Constants.serverType = serverType;
    }

    public static boolean isValid(String nickname) {
        return NICKNAME_PATTERN.matcher(nickname).matches();
    }

    public static void setAccountDeposit(AccountDeposit accountDeposit) {
        Constants.accountDeposit = accountDeposit;
    }

    public static UUID getCrackedUniqueId(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username.toUpperCase()).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isUniqueId(String var1) {
        try {
            UUID.fromString(var1);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String KEY(int lenght, boolean specialChars) {
        String PATTERN = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        if (specialChars)
            PATTERN = PATTERN + "!@#$%¨&*()-_=";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lenght; i++) {
            double index = Math.random() * PATTERN.length();
            builder.append(PATTERN.charAt((int) index));
        }
        return builder.toString();
    }

    private static DecimalFormat create() {
        DecimalFormat df = new DecimalFormat("#.#");
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator(',');
        df.setDecimalFormatSymbols(sym);
        return df;
    }

}