package com.minecraft.thebridge.game.cage.list;

import com.minecraft.core.bukkit.server.thebridge.CageRarity;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.thebridge.TheBridge;
import com.minecraft.thebridge.game.cage.Cage;
import org.bukkit.Material;

public class End extends Cage {

    public End(TheBridge theBridge) {
        super(theBridge);

        setDisplayName("End");
        setRank(Rank.ULTRA_PLUS);
        setRarity(CageRarity.LEGENDARY);
        setPrice(10000);

        setIcon(Pattern.of(Material.ENDER_STONE));

    }
}
