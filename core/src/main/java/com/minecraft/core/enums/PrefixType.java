/*
 * Copyright (C) BlazeMC, All Rights Reserved
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

    DEFAULT("dMjgl", Rank.MEMBER, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + formatTagName(tag.getName().toUpperCase(), tag) + tag.getColor() + " "),
    BRACES("LRBwT", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "{" + formatTagName(tag.getName(), tag) + tag.getColor() + "} "),
    BRACKETS("sJvjZ", Rank.BLAZE_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "[" + formatTagName(tag.getName(), tag) + tag.getColor() + "] "),
    BRACKETS_UPPER("fHYat", Rank.BLAZE_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "[" + formatTagName(tag.getName().toUpperCase(), tag) + tag.getColor() + "] "),
    COLOR("xOEsP", Rank.BLAZE_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getFormattedColor()),
    PARENTHESIS("bvjLy", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "(" + formatTagName(tag.getName(), tag) + tag.getColor() + ") "),
    VANILLA("EDhtE", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "<" + formatTagName(tag.getName(), tag) + tag.getColor() + "> "),
    DEFAULT_BOLD("XspJC", Rank.BLAZE_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + formatTagName(tag.getName().toUpperCase(), tag) + tag.getColor() + " "),
    DEFAULT_GRAY("bWJnm", Rank.BLAZE_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + formatTagName(tag.getName().toUpperCase(), tag) + tag.getColor() + " §7"),
    DEFAULT_LOWER("wtFLH", Rank.PARTNER_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + formatTagName(tag.getName(), tag) + tag.getColor() + " "),
    DEFAULT_WHITE("YnRcF", Rank.BLAZE_PLUS, tag -> tag == Tag.ROSA ? tag.getColor() : tag.getColor() + "§l" + formatTagName(tag.getName().toUpperCase(), tag) + tag.getColor() + " §f");

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

    private static String formatTagName(String name, Tag tag) {
        if (tag != Tag.PARTNER_PLUS) {
            return name.replace("+", "#").replace("#1", "#").replace("#2", "#").replace("#3", "#");
        }
        return name;
    }
}