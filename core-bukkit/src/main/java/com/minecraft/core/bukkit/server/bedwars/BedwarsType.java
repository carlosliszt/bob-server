package com.minecraft.core.bukkit.server.bedwars;

import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum BedwarsType {

    BEDWARS_SOLO(8, Tables.BEDWARS, "Bedwars (Solo)", Material.BED),
    BEDWARS_DUO(16, Tables.BEDWARS, "Bedwars (Duplas)", Material.BED);

    @Getter
    private static final BedwarsType[] values;

    static {
        values = values();
    }

    private final int maxPlayers;
    private final Tables table;
    private final String name;
    private final Material material;

    public static BedwarsType fromName(String name) {
        return Arrays.stream(getValues()).filter(bedwarsType -> bedwarsType.name().equals(name)).findFirst().orElse(null);
    }
}
