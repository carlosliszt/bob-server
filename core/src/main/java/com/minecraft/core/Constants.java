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

    /**
     * MySQL connection
     */
    public static MySQL mySQL;

    public static MySQL getMySQL() {
        return mySQL;
    }

    public static void setMySQL(MySQL mySQL) {
        Constants.mySQL = mySQL;
    }

    /**
     * Redis connection
     */
    public static Redis redis;

    public static Redis getRedis() {
        return redis;
    }


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
    public static final String SERVER_DISCORD = System.getProperty("server_discord", "discord.gg/blazemc");
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
    public static final AccountStorage accountStorage = new AccountStorage();
    public static final Pattern NICKNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");
    public static final MojangAPI mojangAPI = new MojangAPI();

    public static ServerType serverType = ServerType.UNKNOWN, lobbyType = ServerType.UNKNOWN;
    public static ServerStorage serverStorage;
    public static AccountDeposit accountDeposit;
    private static final PartyStorage partyStorage = new PartyStorage();
    private static final ClanService clanService = new ClanService();

    public static ServerCategory getServerCategory() {
        return serverType.getServerCategory();
    }

    public static PartyStorage getPartyStorage() {
        return partyStorage;
    }

    public static ClanService getClanService() {
        return clanService;
    }

    public static ServerStorage getServerStorage() {
        return serverStorage;
    }

    public static ServerType getLobbyType() {
        return lobbyType;
    }

    public static void setLobbyType(ServerType lobbyType) {
        Constants.lobbyType = lobbyType;
    }

    public static void setServerStorage(ServerStorage serverStorage) {
        Constants.serverStorage = serverStorage;
    }

    public static ServerType getServerType() {
        return serverType;
    }

    public static void setServerType(ServerType serverType) {
        Constants.serverType = serverType;
    }

    public static MojangAPI getMojangAPI() {
        return mojangAPI;
    }

    public static boolean isValid(String nickname) {
        return NICKNAME_PATTERN.matcher(nickname).matches();
    }

    public static void setAccountDeposit(AccountDeposit accountDeposit) {
        Constants.accountDeposit = accountDeposit;
    }

    public static AccountDeposit getAccountDeposit() {
        return accountDeposit;
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

    public static AccountStorage getAccountStorage() {
        return accountStorage;
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