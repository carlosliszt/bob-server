package com.minecraft.thebridge.game.cage.list;

import com.minecraft.core.bukkit.server.thebridge.CageRarity;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.thebridge.TheBridge;
import com.minecraft.thebridge.game.cage.Cage;
import org.bukkit.Material;

public class Nether extends Cage {

    public Nether(TheBridge theBridge) {
        super(theBridge);

        setDisplayName("Nether");
        setRank(Rank.BLAZE_PLUS);
        setRarity(CageRarity.LEGENDARY);
        setPrice(10000);

        setIcon(Pattern.of(Material.NETHERRACK));

    }
}
