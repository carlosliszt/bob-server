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
public enum PrefixType {

    DEFAULT("dMjgl", Rank.MEMBER, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + tag.getName().toUpperCase() + " " + tag.getColor()),
    BRACES("LRBwT", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "{" + tag.getName() + "} "),
    BRACKETS("sJvjZ", Rank.ULTRA_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "[" + tag.getName() + "] "),
    BRACKETS_UPPER("fHYat", Rank.ULTRA_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "[" + tag.getName().toUpperCase() + "] "),
    COLOR("xOEsP", Rank.ULTRA_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getFormattedColor()),
    PARENTHESIS("bvjLy", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "(" + tag.getName() + ") "),
    VANILLA("EDhtE", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "<" + tag.getName() + "> "),
    DEFAULT_BOLD("XspJC", Rank.ULTRA_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + tag.getName().toUpperCase() + " "),
    DEFAULT_GRAY("bWJnm", Rank.ULTRA_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + tag.getName().toUpperCase() + " §7"),
    DEFAULT_LOWER("wtFLH", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + tag.getName() + " "),
    DEFAULT_WHITE("YnRcF", Rank.ULTRA_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + tag.getName().toUpperCase() + " §f");

    private final String uniqueCode;
    private final Rank rank;
    private final Formatter formatter;

    @Getter
    private static final PrefixType[] values;

    static {
        values = values();
    }

    public static PrefixType fromString(String string) {
        return Arrays.stream(getValues()).filter(prefixType -> prefixType.name().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    public static PrefixType fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(prefixType -> prefixType.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public interface Formatter {
        String format(Tag tag);
    }
}