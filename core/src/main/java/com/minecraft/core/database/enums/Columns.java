/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.database.enums;

import java.util.Arrays;

public enum Columns {

    UNIQUE_ID(null, "String", "unique_id", "VARCHAR(36)"),
    USERNAME("...", "String", "username", "VARCHAR(16)"),
    RANKS("[]", "JsonArray", "ranks", "longtext"),
    PERMISSIONS("[]", "JsonArray", "permissions", "longtext"),
    TAGS("[]", "JsonArray", "tags", "longtext"),
    PLUSCOLORS("[]", "JsonArray", "pluscolors", "longtext"),
    CLANTAGS("[]", "JsonArray", "clantags", "longtext"),
    MEDALS("[]", "JsonArray", "medals", "longtext"),
    PUNISHMENTS("[]", "JsonArray", "punishments", "longtext"),
    TITLES("[]", "JsonArray", "titles", "longtext"),
    CLAN(-1, "Int", "clan_id", "INT(100)"),
    FLAGS(0, "Int", "flags", "INT(100)"),
    PREMIUM(false, "Boolean", "premium", "VARCHAR(10)"),
    BANNED(false, "Boolean", "banned", "VARCHAR(10)"),
    MUTED(false, "Boolean", "muted", "VARCHAR(10)"),
    FIRST_LOGIN(0L, "Long", "firstLogin", "BIGINT"),
    LAST_LOGIN(0L, "Long", "lastLogin", "BIGINT"),
    ADDRESS("...", "String", "address", "VARCHAR(50)"),

    ACCESSORY("...", "String", "accessory", "VARCHAR(100)"),
    PARTICLE("...", "String", "particle", "VARCHAR(100)"),
    TITLE("NONE", "String", "title", "VARCHAR(100)"),

    PASSWORD("...", "String", "password", "VARCHAR(24)"),
    PASSWORD_LAST_UPDATE(0L, "Long", "lastUpdate", "BIGINT"),
    SESSION_EXPIRES_AT(0L, "Long", "session_expiresAt", "BIGINT"),
    SESSION_ADDRESS("...", "String", "session_address", "VARCHAR(50)"),
    REGISTERED_AT(0L, "Long", "registeredAt", "BIGINT"),

    NICK("...", "String", "nick", "VARCHAR(16)"),
    NICK_OBJECTS("[]", "JsonArray", "nick_objects", "longtext"),
    LAST_NICK("...", "String", "last_nick", "VARCHAR(16)"),
    TAG("EalNl", "String", "tag", "VARCHAR(6)"),
    CLANTAG("yQFBm", "String", "clantag", "VARCHAR(6)"),
    PREFIXTYPE("dMjgl", "String", "prefixtype", "VARCHAR(6)"),
    MEDAL("TaAEd", "String", "medal", "VARCHAR(6)"),
    PLUSCOLOR("a3ajc", "String", "pluscolor", "VARCHAR(6)"),
    LANGUAGE("VIxPa", "String", "language", "VARCHAR(6)"),
    PREFERENCES(0, "Int", "preferences", "INT(100)"),
    SKIN("{}", "JsonObject", "skin", "longtext"),
    DUELS_KILL_EFFECT(0, "Int", "duels_effect", "INT(100)"),
    DUELS_SPEC_PREFERENCE(true, "Boolean", "duels_specs", "VARCHAR(10)"),
    BLOCKEDS("[]", "JsonArray", "blocks", "longtext"),
    FRIENDS("[]", "JsonArray", "friends", "longtext"),
    FRIEND_STATUS("ONLINE", "String", "friend_status", "VARCHAR(16)"),
    SENT_FRIEND_REQUESTS("[]", "JsonArray", "sent_friend_requests", "longtext"),
    RECEIVED_FRIEND_REQUESTS("[]", "JsonArray", "received_friend_requests", "longtext"),
    ULTRA_PLUS_MONTHS(0, "Int", "ultra_plus_months", "INT(100)"),

    HG_KILLS(0, "Int", "hg_kills", "INT(100)"),
    HG_DEATHS(0, "Int", "hg_deaths", "INT(100)"),
    HG_WINS(0, "Int", "hg_wins", "INT(100)"),
    HG_GAMES(0, "Int", "hg_played_games", "INT(100)"),
    HG_MAX_GAME_KILLS(0, "Int", "hg_max_game_kills", "INT(100)"),

    SCRIM_KILLS(0, "Int", "scrim_kills", "INT(100)"),
    SCRIM_DEATHS(0, "Int", "scrim_deaths", "INT(100)"),
    SCRIM_WINS(0, "Int", "scrim_wins", "INT(100)"),
    SCRIM_GAMES(0, "Int", "scrim_played_games", "INT(100)"),
    SCRIM_MAX_GAME_KILLS(0, "Int", "scrim_max_game_kills", "INT(100)"),

    HG_RANK(1, "Int", "hg_rank", "INT(100)"),
    HG_RANK_EXP(0, "Int", "hg_rank_exp", "INT(100)"),

    SCRIM_RANK(1, "Int", "scrim_rank", "INT(100)"),
    SCRIM_RANK_EXP(0, "Int", "scrim_rank_exp", "INT(100)"),

    HG_COINS(0, "Int", "coins", "INT(100)"),
    HG_KITS("[]", "JsonArray", "kits", "longtext"),
    HG_DAILY_KITS("{}", "JsonObject", "dailyKit", "longtext"),

    PVP_ARENA_KILLS(0, "Int", "arena_kills", "INT(100)"),
    PVP_ARENA_DEATHS(0, "Int", "arena_deaths", "INT(100)"),
    PVP_ARENA_KILLSTREAK(0, "Int", "arena_killstreak", "INT(100)"),
    PVP_ARENA_MAX_KILLSTREAK(0, "Int", "arena_max_killstreak", "INT(100)"),

    PVP_FPS_KILLS(0, "Int", "fps_kills", "INT(100)"),
    PVP_FPS_DEATHS(0, "Int", "fps_deaths", "INT(100)"),
    PVP_FPS_KILLSTREAK(0, "Int", "fps_killstreak", "INT(100)"),
    PVP_FPS_MAX_KILLSTREAK(0, "Int", "fps_max_killstreak", "INT(100)"),

    PVP_DAMAGE_SETTINGS("[]", "JsonArray", "damage_settings", "longtext"),

    PVP_DAMAGE_EASY(0, "Int", "damage_easy", "INT(100)"),
    PVP_DAMAGE_MEDIUM(0, "Int", "damage_medium", "INT(100)"),
    PVP_DAMAGE_HARD(0, "Int", "damage_hard", "INT(100)"),
    PVP_DAMAGE_EXTREME(0, "Int", "damage_extreme", "INT(100)"),

    PVP_RANK(1, "Int", "rank", "INT(100)"),
    PVP_RANK_EXP(0, "Int", "rank_exp", "INT(100)"),

    PVP_COINS(0, "Int", "coins", "INT(100)"),
    PVP_KITS("[]", "JsonArray", "kits", "longtext"),

    DUELS_SOUP_WINS(0, "Int", "soup_wins", "INT(100)"),
    DUELS_SOUP_LOSSES(0, "Int", "soup_losses", "INT(100)"),
    DUELS_SOUP_WINSTREAK(0, "Int", "soup_winstreak", "INT(100)"),
    DUELS_SOUP_MAX_WINSTREAK(0, "Int", "soup_max_winstreak", "INT(100)"),
    DUELS_SOUP_GAMES(0, "Int", "soup_games", "INT(100)"),
    DUELS_SOUP_RATING(1500, "Int", "soup_rating", "INT(100)"),
    DUELS_SOUP_INVENTORY("...", "String", "soup_inventory", "VARCHAR(5000)"),

    DUELS_SIMULATOR_WINS(0, "Int", "simulator_wins", "INT(100)"),
    DUELS_SIMULATOR_LOSSES(0, "Int", "simulator_losses", "INT(100)"),
    DUELS_SIMULATOR_WINSTREAK(0, "Int", "simulator_winstreak", "INT(100)"),
    DUELS_SIMULATOR_MAX_WINSTREAK(0, "Int", "simulator_max_winstreak", "INT(100)"),
    DUELS_SIMULATOR_GAMES(0, "Int", "simulator_games", "INT(100)"),
    DUELS_SIMULATOR_RATING(1500, "Int", "simulator_rating", "INT(100)"),
    DUELS_SIMULATOR_INVENTORY("...", "String", "simulator_inventory", "VARCHAR(5000)"),

    DUELS_UHC_WINS(0, "Int", "uhc_wins", "INT(100)"),
    DUELS_UHC_LOSSES(0, "Int", "uhc_losses", "INT(100)"),
    DUELS_UHC_WINSTREAK(0, "Int", "uhc_winstreak", "INT(100)"),
    DUELS_UHC_MAX_WINSTREAK(0, "Int", "uhc_max_winstreak", "INT(100)"),
    DUELS_UHC_GAMES(0, "Int", "uhc_games", "INT(100)"),
    DUELS_UHC_RATING(1500, "Int", "uhc_rating", "INT(100)"),
    DUELS_UHC_INVENTORY("...", "String", "uhc_inventory", "VARCHAR(5000)"),

    DUELS_SUMO_WINS(0, "Int", "sumo_wins", "INT(100)"),
    DUELS_SUMO_LOSSES(0, "Int", "sumo_losses", "INT(100)"),
    DUELS_SUMO_WINSTREAK(0, "Int", "sumo_winstreak", "INT(100)"),
    DUELS_SUMO_MAX_WINSTREAK(0, "Int", "sumo_max_winstreak", "INT(100)"),
    DUELS_SUMO_GAMES(0, "Int", "sumo_games", "INT(100)"),
    DUELS_SUMO_RATING(1500, "Int", "sumo_rating", "INT(100)"),
    DUELS_SUMO_INVENTORY("...", "String", "sumo_inventory", "VARCHAR(5000)"),

    DUELS_SCRIM_WINS(0, "Int", "scrim_wins", "INT(100)"),
    DUELS_SCRIM_LOSSES(0, "Int", "scrim_losses", "INT(100)"),
    DUELS_SCRIM_WINSTREAK(0, "Int", "scrim_winstreak", "INT(100)"),
    DUELS_SCRIM_MAX_WINSTREAK(0, "Int", "scrim_max_winstreak", "INT(100)"),
    DUELS_SCRIM_GAMES(0, "Int", "scrim_games", "INT(100)"),
    DUELS_SCRIM_RATING(1500, "Int", "scrim_rating", "INT(100)"),
    DUELS_SCRIM_INVENTORY("...", "String", "scrim_inventory", "VARCHAR(5000)"),

    DUELS_GLADIATOR_WINS(0, "Int", "gladiator_wins", "INT(100)"),
    DUELS_GLADIATOR_LOSSES(0, "Int", "gladiator_losses", "INT(100)"),
    DUELS_GLADIATOR_WINSTREAK(0, "Int", "gladiator_winstreak", "INT(100)"),
    DUELS_GLADIATOR_MAX_WINSTREAK(0, "Int", "gladiator_max_winstreak", "INT(100)"),
    DUELS_GLADIATOR_GAMES(0, "Int", "gladiator_games", "INT(100)"),
    DUELS_GLADIATOR_RATING(1500, "Int", "gladiator_rating", "INT(100)"),
    DUELS_GLADIATOR_INVENTORY("...", "String", "gladiator_inventory", "VARCHAR(5000)"),
    DUELS_GLADIATOR_OLD_RATING(1500, "Int", "old_soup_rating", "INT(100)"),
    DUELS_GLADIATOR_OLD_INVENTORY("...", "String", "old_gladiator_inventory", "VARCHAR(5000)"),

    DUELS_BOXING_WINS(0, "Int", "boxing_wins", "INT(100)"),
    DUELS_BOXING_LOSSES(0, "Int", "boxing_losses", "INT(100)"),
    DUELS_BOXING_WINSTREAK(0, "Int", "boxing_winstreak", "INT(100)"),
    DUELS_BOXING_MAX_WINSTREAK(0, "Int", "boxing_max_winstreak", "INT(100)"),
    DUELS_BOXING_GAMES(0, "Int", "boxing_games", "INT(100)"),
    DUELS_BOXING_RATING(1500, "Int", "boxing_rating", "INT(100)"),
    DUELS_BOXING_INVENTORY("...", "String", "boxing_inventory", "VARCHAR(5000)"),

    TIOGERSON_WINS(0, "Int", "tiogerson_wins", "INT(100)"),
    TIOGERSON_LOSSES(0, "Int", "tiogerson_losses", "INT(100)"),
    TIOGERSON_WINSTREAK(0, "Int", "tiogerson_winstreak", "INT(100)"),
    TIOGERSON_MAX_WINSTREAK(0, "Int", "tiogerson_max_winstreak", "INT(100)"),
    TIOGERSON_GAMES(0, "Int", "tiogerson_games", "INT(100)"),

    BRIDGE_SOLO_WINS(0, "Int", "solo_wins", "INT(100)"),
    BRIDGE_SOLO_LOSSES(0, "Int", "solo_losses", "INT(100)"),
    BRIDGE_SOLO_KILLS(0, "Int", "solo_kills", "INT(100)"),
    BRIDGE_SOLO_DEATHS(0, "Int", "solo_deaths", "INT(100)"),
    BRIDGE_SOLO_POINTS(0, "Int", "solo_points", "INT(100)"),
    BRIDGE_SOLO_ROUNDS(0, "Int", "solo_rounds", "INT(100)"),
    BRIDGE_SOLO_WINSTREAK(0, "Int", "solo_winstreak", "INT(100)"),
    BRIDGE_SOLO_MAX_WINSTREAK(0, "Int", "solo_max_winstreak", "INT(100)"),

    BRIDGE_DOUBLES_WINS(0, "Int", "doubles_wins", "INT(100)"),
    BRIDGE_DOUBLES_LOSSES(0, "Int", "doubles_losses", "INT(100)"),
    BRIDGE_DOUBLES_KILLS(0, "Int", "doubles_kills", "INT(100)"),
    BRIDGE_DOUBLES_DEATHS(0, "Int", "doubles_deaths", "INT(100)"),
    BRIDGE_DOUBLES_POINTS(0, "Int", "doubles_points", "INT(100)"),
    BRIDGE_DOUBLES_ROUNDS(0, "Int", "doubles_rounds", "INT(100)"),
    BRIDGE_DOUBLES_WINSTREAK(0, "Int", "doubles_winstreak", "INT(100)"),
    BRIDGE_DOUBLES_MAX_WINSTREAK(0, "Int", "doubles_max_winstreak", "INT(100)"),

    BRIDGE_COINS(0, "Int", "coins", "INT(100)"),
    BRIDGE_RANK(1, "Int", "rank", "INT(100)"),
    BRIDGE_RANK_EXP(0, "Int", "rank_exp", "INT(100)"),
    BRIDGE_DATA("[]", "JsonArray", "data", "longtext"),
    BRIDGE_CAGE("default", "String", "cage", "VARCHAR(100)"),
    BRIDGE_INVENTORY("...", "String", "inventory", "VARCHAR(5000)"),

    STAFF_WEEKLY_BANS(0, "Int", "weekly_bans", "INT(100)"),
    STAFF_WEEKLY_MUTES(0, "Int", "weekly_mutes", "INT(100)"),
    STAFF_WEEKLY_EVENTS(0, "Int", "weekly_events", "INT(100)"),

    STAFF_MONTHLY_BANS(0, "Int", "monthly_bans", "INT(100)"),
    STAFF_MONTHLY_MUTES(0, "Int", "monthly_mutes", "INT(100)"),
    STAFF_MONTHLY_EVENTS(0, "Int", "monthly_events", "INT(100)"),

    STAFF_LIFETIME_BANS(0, "Int", "lifetime_bans", "INT(100)"),
    STAFF_LIFETIME_MUTES(0, "Int", "lifetime_mutes", "INT(100)"),
    STAFF_LIFETIME_EVENTS(0, "Int", "lifetime_events", "INT(100)"),

    MAIN_LOBBY_PARKOUR_RECORD(0, "Int", "main_parkour_record", "INT(100)"),
    DUELS_LOBBY_PARKOUR_RECORD(0, "Int", "duels_parkour_record", "INT(100)"),
    HUNGERGAMES_LOBBY_PARKOUR_RECORD(0, "Int", "hg_parkour_record", "INT(100)"),
    PVP_LOBBY_PARKOUR_RECORD(0, "Int", "pvp_parkour_record", "INT(100)"),
    THE_BRIDGE_LOBBY_PARKOUR_RECORD(0, "Int", "bridge_parkour_record", "INT(100)"),

    BEDWARS_WINS(0, "Int", "wins", "INT(100)"),
    BEDWARS_LOSSES(0, "Int", "losses", "INT(100)"),
    BEDWARS_KILLS(0, "Int", "kills", "INT(100)"),
    BEDWARS_DEATHS(0, "Int", "deaths", "INT(100)"),
    BEDWARS_FINAL_KILLS(0, "Int", "final_kills", "INT(100)"),
    BEDWARS_FINAL_DEATHS(0, "Int", "final_deaths", "INT(100)"),
    BEDWARS_BEDS_BROKEN(0, "Int", "beds_broken", "INT(100)"),
    BEDWARS_BEDS_LOST(0, "Int", "beds_lost", "INT(100)"),
    BEDWARS_GAMES(0, "Int", "games_played", "INT(100)"),
    BEDWARS_WINSTREAK(0, "Int", "winstreak", "INT(100)"),
    BEDWARS_MAX_WINSTREAK(0, "Int", "max_winstreak", "INT(100)"),
    BEDWARS_RANK(1, "Int", "rank", "INT(100)"),
    BEDWARS_RANK_EXP(0, "Int", "rank_exp", "INT(100)"),

    BEDWARS_SOLO_WINS(0, "Int", "solo_wins", "INT(100)"),
    BEDWARS_SOLO_LOSSES(0, "Int", "solo_losses", "INT(100)"),
    BEDWARS_SOLO_KILLS(0, "Int", "solo_kills", "INT(100)"),
    BEDWARS_SOLO_DEATHS(0, "Int", "solo_deaths", "INT(100)"),
    BEDWARS_SOLO_FINAL_KILLS(0, "Int", "solo_finals", "INT(100)"),
    BEDWARS_SOLO_FINAL_DEATHS(0, "Int", "solo_final_deaths", "INT(100)"),
    BEDWARS_SOLO_GAMES(0, "Int", "solo_games_played", "INT(100)"),
    BEDWARS_SOLO_WINSTREAK(0, "Int", "solo_winstreak", "INT(100)"),
    BEDWARS_SOLO_MAX_WINSTREAK(0, "Int", "solo_max_winstreak", "INT(100)"),
    BEDWARS_SOLO_BEDS_BROKEN(0, "Int", "solo_beds_broken", "INT(100)"),
    BEDWARS_SOLO_BEDS_LOST(0, "Int", "solo_beds_lost", "INT(100)"),
    ;

    private final Object defaultValue;
    private final String classExpected, field, columnType;

    Columns(Object defaultValue, String classExpected, String field, String columnType) {
        this.defaultValue = defaultValue;
        this.classExpected = classExpected;
        this.field = field;
        this.columnType = columnType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getClassExpected() {
        return classExpected;
    }

    public String getField() {
        return field;
    }

    public String getColumnType() {
        return columnType;
    }

    public Tables getTable() {
        return Arrays.stream(Tables.values()).filter(table -> Arrays.asList(table.getColumns()).contains(this)).findFirst().orElse(null);
    }
}