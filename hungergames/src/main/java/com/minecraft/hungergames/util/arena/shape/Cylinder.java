/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.arena.shape;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.hungergames.util.arena.Arena;
import com.minecraft.hungergames.util.bo3.BO3;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

@Getter
public class Cylinder extends Arena {

    private final List<Pattern> patterns;
    private final boolean roof;

    public Cylinder(int size, int height, List<Pattern> pattern, boolean roof) {
        super(size, height);
        this.patterns = pattern;
        this.roof = roof;
    }

    public void spawn(Location location, BO3.BlockHandle blockHandle) {

        WorldEditAPI.getInstance().makeCylinder(location, getSize(), getSize(), 1, true).forEach(block -> {

            Pattern pattern = patterns.get(Constants.RANDOM.nextInt(patterns.size()));

            Material material = pattern.getMaterial();
            byte data = pattern.getData();

            if (blockHandle.canPlace(block, pattern))
                WorldEditAPI.getInstance().setBlock(block, material, data);
        });

        WorldEditAPI.getInstance().makeCylinder(location.clone().add(0, 1, 0), getSize(), getSize(), getHeight(), false).forEach(block -> {

            Pattern pattern = patterns.get(Constants.RANDOM.nextInt(patterns.size()));

            Material material = pattern.getMaterial();
            byte data = pattern.getData();

            if (blockHandle.canPlace(block, pattern))
                WorldEditAPI.getInstance().setBlock(block, material, data);
        });

        if (roof) {
            WorldEditAPI.getInstance().makeCylinder(location.clone().add(0, getHeight() + 1, 0), getSize(), getSize(), 1, true).forEach(block -> {

                Pattern pattern = patterns.get(Constants.RANDOM.nextInt(patterns.size()));

                Material material = pattern.getMaterial();
                byte data = pattern.getData();

                if (blockHandle.canPlace(block, pattern))
                    WorldEditAPI.getInstance().setBlock(block, material, data);
            });
        }
    }
}
