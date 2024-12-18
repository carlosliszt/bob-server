/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Medal {

    NITRO("Nitro", "Medalha exclusiva para boosters do Discord", "Nitro", "✦", "§d", "h3z8d", Rank.ADMINISTRATOR, true),
    BETACUP("Betacup", "Medalha exclusiva para os ganhadores da Betacup", "Betacup", "⚡", "§1", "wbt07", Rank.ADMINISTRATOR, true),
    CHALLENGER("Challenger", "Medalha exclusiva para os ganhadores das temporadas", "Challenger", "❂", "§4", "4gr67", Rank.ADMINISTRATOR, true),
    MASTER("Master", "Medalha exclusiva para Rank Elite", "Master", "✪", "§c", "mastr", Rank.ULTRA_PLUS, false),
    COOPERATOR("Cooperator", "Medalha exclusiva para jogadores que contribuem no desenvolvimento.", "Cooperator", "⚠", "§e", "cootr", Rank.ADMINISTRATOR, true),

    SUBSCRIBER("Subscriber", "Medalha exclusiva para assinantes da Twitch", "Subscriber", "✵", "§5", "hn83i", Rank.ADMINISTRATOR, true),

    TRYHARD("Tryhard", "Medalha para o clan ganhador do primeiro torneio de clans.", "Tryhard", "✖", "§c", "nfb0j", Rank.ADMINISTRATOR, true),

    HALLOWEEN("Halloween", "Medalha exclusiva adquirível no Halloween", "Halloween", "✞", "§5", "hlw21", Rank.ADMINISTRATOR, true),

    CHRISTMAS("Christmas", "Medalha exclusiva adquirível no Natal", "Christmas", "☃", "§f", "xma21", Rank.ADMINISTRATOR, true),

    CHRISTMAS_HUNTER("Christmas Hunter", "Medalha exclusiva obtida pelo evento Christmas Hunter", "Hunter", "❆", "§f", "xmh21", Rank.ADMINISTRATOR, true),

    COPA("Copa", "Medalha exclusiva para os participantes da CopaHG", "Copa", "Ⓒ", "§e", "cup21", Rank.ADMINISTRATOR, true),

    ELITE("Elite", "Medalha exclusiva para Rank Elite", "Elite", "✫", "§c", "zf137", Rank.ULTRA_PLUS, false),

    PROMOTER("Promotor", "Medalha exclusiva para promotores de Eventos", "Promotor", "❆", "§6§l", "81187", Rank.ADMINISTRATOR, false),

    STAFF("Staff", "Medalha exclusiva para Staff", "Staff", "✸", "§5", "stf21", Rank.HELPER, false),

    MONTHLY_STAFFER("Staff Destaque", "Medalha exclusiva para o Staffer do mês", "Destaque", "➸", "§b", "3gdis", Rank.ADMINISTRATOR, false),

    BOB("Bob", "Medalha exclusiva para Bob.", "Bob", "B", "§d§l", "spt21", Rank.HELPER, false),

    THORMENTO("Thormento", "Medalha exclusiva para o Thormento", "Thormento", "⚡", "§b", "thm21", Rank.DEVELOPER_ADMIN, false),

    GAY("Gay", "Medalha exclusiva para o abalar.", "Gay", "§c§kA§r§6§lG§e§lA§a§lY§r§b§kA§r", "§e", "uwu", Rank.BOB, false),

    CROWN("Crown", "Medalha exclusiva para Rank Pro", "Crown", "♚", "§6", "crown", Rank.PRO, false),

    TOXIC("Toxic", "Medalha exclusiva para Rank Beta", "Toxic", "☣", "§1", "toxic", Rank.BETA, false),

    TOP_JOGADAS("Top Jogadas", "Medalha para os ganhadores do Top Jogadas.", "Jogadas", "✳", "§a", "topjg", Rank.ADMINISTRATOR, true),

    NONE("Nenhuma", "Medalha padrão", "Nenhuma", "", "§7", "TaAEd", Rank.MEMBER, false);

    private final String displayName, description, name, icon, color, uniqueCode;
    private final Rank rank;
    private final boolean dedicated;

    @Getter
    private static final Medal[] values;

    static {
        values = values();
    }

    public static Medal fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(medal -> medal.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public static Medal fromString(String name) {
        return Arrays.stream(getValues()).filter(medal -> medal.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Medal getOrElse(String code, Medal m) {
        return Arrays.stream(getValues()).filter(medal -> medal.getUniqueCode().equals(code)).findFirst().orElse(m);
    }

    public String getFormattedName() {
        return getColor() + getDisplayName();
    }

}