/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.bo3;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class BO3 {

    private final Set<BO3Block> blocks;
    private boolean hidden;

    public BO3() {
        this.blocks = new HashSet<>();
    }

    public Set<BO3Block> getBlocks() {
        return blocks;
    }

    public void spawn(final Location location, final BlockHandle blockHandle) {

        for (BO3Block binaryBlock : getBlocks()) {
            Location block = location.clone().add(binaryBlock.getX(), binaryBlock.getY(), binaryBlock.getZ());
            Pattern pattern = binaryBlock.getPattern();

            if (blockHandle.canPlace(block, pattern)) {
                WorldEditAPI.getInstance().setBlock(block, pattern.getMaterial(), pattern.getData());
            }
        }
    }

    public interface BlockHandle {
        boolean canPlace(Location location, Pattern pattern);
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void addBlock(BO3Block bo3Block) {
        this.blocks.add(bo3Block);
    }
}
