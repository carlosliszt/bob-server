package com.minecraft.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PlusColor {

    GOLDEN(0, "Dourado", "§6", "a3ajc"),
    YELLOW(1, "Amarelo", "§e", "axcjc"),
    RED(2, "Vermelho", "§c", "a3ijx"),
    DARK_RED(3, "Vermelho Escuro", "§4", "ad23z"),
    BLUE(4, "Azul", "§9", "a432c"),
    DARK_BLUE(5, "Azul Escuro", "§1", "x2a2c"),
    GREEN(6, "Verde", "§a", "r3j4a"),
    DARK_GREEN(7, "Verde Escuro", "§2", "a23dx"),
    AQUA(8, "Ciano", "§b", "29ej2"),
    DARK_AQUA(9, "Ciano Escuro", "§3", "a2d3c"),
    WHITE(10, "Branco", "§f", "s2xc2"),
    BLACK(11, "Preto", "§0", "c2fv2"),
    ;

    @Getter
    private static final PlusColor[] values;

    static {
        values = values();
    }

    private int id;
    private String name, color, uniqueCode;

    public static PlusColor fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(color -> color.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public static PlusColor fromString(String name) {
        return Arrays.stream(getValues()).filter(color -> color.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static PlusColor getOrElse(String code, PlusColor p) {
        return Arrays.stream(getValues()).filter(color -> color.getUniqueCode().equals(code)).findFirst().orElse(p);
    }

    public String getFormattedName() {
        return getColor() + getName();
    }

    public String getFormatted() {
        return getColor() + "+";
    }

}
