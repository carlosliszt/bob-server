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

    /*
     * @param punishments is the amount of BANS and MUTES applied
     *
     * hardcoded amounts for now (can and WILL be changed)
     *
     */
    public static StaffPerformance calculatePerformance(int punishments) {

        if(punishments < 20) {
            return UNDEFINED;
        } else if(punishments >= 20 && punishments < 50) {
            return BAD;
        } else if(punishments >= 50 && punishments < 200) {
            return MID;
        } else if(punishments >= 200 && punishments < 400) {
            return GOOD;
        } else {
            return EXCELLENT;
        }

    }

}
