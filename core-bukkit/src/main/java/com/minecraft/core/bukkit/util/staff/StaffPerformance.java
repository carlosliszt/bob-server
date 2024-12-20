package com.minecraft.core.bukkit.util.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StaffPerformance {

    UNDEFINED("§7..."),
    BAD("§cRuim"),
    MID("§eMédio"),
    GOOD("§aBom"),
    EXCELLENT("§2Excelente");

    private String text;

}
