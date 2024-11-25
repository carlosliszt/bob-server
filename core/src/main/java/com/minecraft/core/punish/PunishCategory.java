/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.punish;

import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PunishCategory {

    CHARGEBACK("Estorno", "Chargeback", true, "chargeback", Rank.ADMINISTRATOR, Collections.singletonList(PunishType.BAN)),
    BADBUILD("Construção Inapropriada", "Inappropriate construction", false, "badbuild", Rank.TRIAL_MODERATOR, Collections.singletonList(PunishType.BAN), "Construção Inapropriada"),
    EXTREMISM("Extremismo", "Extremism", true, "extremism", Rank.TRIAL_MODERATOR, Collections.singletonList(PunishType.BAN), "Nazismo"),
    BADNICK("Nickname Inapropriado", "Inappropriate Nickname", true, "badnick", Rank.TRIAL_MODERATOR, Collections.singletonList(PunishType.BAN), "Nick Inapropriado"),
    CHEATING("Uso de Trapaças", "Cheating is forbidden.", false, "cheating", Rank.PARTNER_PLUS, Collections.singletonList(PunishType.BAN), "reach", "velocity", "killaura", "forcefield", "fly", "flight", "autosoup", "regen", "antikb", "nofall", "glide", "noslowdown", "highjump", "triggerbot", "aimbot", "criticals", "nuker", "xray", "togglesneak", "bowaimbot", "autoclick", "fastfall", "tpaura", "noclip", "fastbow", "sneak", "spider"),
    RACISM("Racismo", "Racism", true, "racism", Rank.HELPER, Collections.singletonList(PunishType.MUTE), "Racismo"),
    HOMOFOBIA("Homofobia", "Homophobia", true, "homofobia", Rank.HELPER, Collections.singletonList(PunishType.MUTE), "Homofobia"),
    COMMUNITY("Violação das Diretrizes da Comunidade", "Violation of Community Guidelines", false, "community", Rank.HELPER, Arrays.asList(PunishType.MUTE, PunishType.BAN), "ofensa", "divulgação"),
    BLACKLIST("Blacklist de eventos", "Event blacklist", false, "blacklist", Rank.SECONDARY_MOD, Collections.singletonList(PunishType.EVENT), "panela", "interferir"),
    REPORT("Mau uso do report", "Bad usage of Report", false, "report", Rank.TRIAL_MODERATOR, Collections.singletonList(PunishType.MUTE)),
    GHOST("Ghost em livestreams", "Ghost in livestream", false, "ghost", Rank.SECONDARY_MOD, Collections.singletonList(PunishType.STREAM)), NONE("None", "None", false, "none", Rank.ADMINISTRATOR, Arrays.asList(PunishType.values()));

    private final String portuguese, english;
    @Getter
    private final boolean inexcusable;
    @Getter
    private final String name;
    @Getter
    private final Rank rank;
    @Getter
    private final List<PunishType> applicablePunishments;
    @Getter
    private final List<String> suggestions;

    PunishCategory(String portuguese, String english, boolean inexcusable, String name, Rank rank, List<PunishType> applicablePunishments, String... suggestions) {
        this.portuguese = portuguese;
        this.english = english;
        this.inexcusable = inexcusable;
        this.name = name;
        this.rank = rank;
        this.applicablePunishments = applicablePunishments;
        this.suggestions = Arrays.asList(suggestions);
    }

    @Getter
    private static final PunishCategory[] values;

    static {
        values = values();
    }

    public boolean isApplicable(PunishType punishType) {
        return applicablePunishments.contains(punishType);
    }

    public String getDisplay(Language language) {
        return language == Language.PORTUGUESE ? this.portuguese : this.english;
    }

    public static PunishCategory fromString(String value) {
        return Arrays.stream(values()).filter(c -> c.getName().equalsIgnoreCase(value)).findFirst().orElse(null);
    }
}