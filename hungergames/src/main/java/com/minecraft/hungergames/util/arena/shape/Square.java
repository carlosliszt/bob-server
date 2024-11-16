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

import java.util.List;

@Getter
public class Square extends Arena {

    private final List<Pattern> patterns;
    private final boolean roof, ground;

    public Square(int size, int height, List<Pattern> pattern, boolean roof, boolean ground) {
        super(size, height);
        this.patterns = pattern;
        this.roof = roof;
        this.ground = ground;
    }

    @Override
    public void spawn(Location location, BO3.BlockHandle blockHandle) {

        int size = getSize();

        for (int y = 0; y <= getHeight(); y++) {
            if (ground && y == 0) {
                for (int x = -size; x <= size; x++) {
                    for (int z = -size; z <= size; z++) {
                        Location loc = location.clone().add(x, y, z);
                        Pattern pattern = generatePattern();

                        if (blockHandle.canPlace(loc, pattern))
                            WorldEditAPI.getInstance().setBlock(loc, pattern.getMaterial(), pattern.getData());
                    }
                }
            } else {
                for (int x = -size; x <= size; x++) {
                    Location loc = location.clone().add(x, y, size);
                    Location loc2 = location.clone().add(x, y, -size);

                    Pattern pattern = generatePattern();

                    if (blockHandle.canPlace(loc, pattern))
                        WorldEditAPI.getInstance().setBlock(loc, pattern.getMaterial(), pattern.getData());

                    pattern = generatePattern();

                    if (blockHandle.canPlace(loc2, pattern))
                        WorldEditAPI.getInstance().setBlock(loc2, pattern.getMaterial(), pattern.getData());
                }
                for (int z = -size; z <= size; z++) {
                    Location loc = location.clone().add(size, y, z);
                    Location loc2 = location.clone().add(-size, y, z);

                    Pattern pattern = generatePattern();

                    if (blockHandle.canPlace(loc, pattern))
                        WorldEditAPI.getInstance().setBlock(loc, pattern.getMaterial(), pattern.getData());

                    pattern = generatePattern();

                    if (blockHandle.canPlace(loc2, pattern))
                        WorldEditAPI.getInstance().setBlock(loc2, pattern.getMaterial(), pattern.getData());
                }
                if (roof && y == getHeight()) {
                    for (int x = -size; x <= size; x++) {
                        for (int z = -size; z <= size; z++) {
                            Location loc = location.clone().add(x, y, z);
                            Pattern pattern = generatePattern();

                            if (blockHandle.canPlace(loc, pattern))
                                WorldEditAPI.getInstance().setBlock(loc, pattern.getMaterial(), pattern.getData());
                        }
                    }
                }
            }
        }
    }

    private Pattern generatePattern() {
        return patterns.get(Constants.RANDOM.nextInt(patterns.size()));
    }
}
