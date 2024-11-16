/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.structure;

import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

public class Stairs {

    private final Location location;
    private final byte data;
    private final List<Location> ignoreBlocks;

    public Stairs(final byte data, final Location location, final List<Location> ignoreBlocks) {
        this.data = data;
        this.location = location;
        this.ignoreBlocks = ignoreBlocks;
    }

    public void spawnStructure() {
        final BlockFace blockFace = this.convertBlockToBlockFace(this.data).getOppositeFace();
        int up;
        byte id;
        Block originBlock;
        if (this.location.getBlock().getRelative(blockFace.getModX(), 1, blockFace.getModZ()).getType() == Material.AIR) {
            up = -1;
            id = this.convertBlockFaceStairs(blockFace.getOppositeFace());
        } else {
            up = 1;
            id = this.convertBlockFaceStairs(blockFace);
        }
        originBlock = this.location.getBlock();
        this.setBlock(originBlock, id);
        this.fill(originBlock, up == 1);
        if (id == -1) {
            return;
        }
        Block block = originBlock;

        boolean b = true;

        while (b) {
            final Block relative = block.getRelative(blockFace.getModX(), up, blockFace.getModZ());
            block = relative;
            if (calculeUpOrDown(relative, up == 1)) {
                setBlock(block, id);
                fill(block, up == 1);
            } else
                b = false;
        }
    }

    private void setBlock(final Block block, final byte id) {
        WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.COBBLESTONE_STAIRS, id);
    }

    private void setBlock(final World world, int x, int y, int z, Material material, byte data) {
        WorldEditAPI.getInstance().setBlock(new Location(world, x, y, z), material, data);
    }

    private void fill(final Block block, final boolean up) {
        if (up) {
            for (int i = block.getY(); i < block.getY() + 20; ++i) {

                Location l1 = new Location(block.getWorld(), block.getX(), i + 1, block.getZ());

                if (isIgnore(l1))
                    continue;

                setBlock(block.getWorld(), block.getX(), i + 1, block.getZ(), Material.AIR, (byte) 0);
            }
        } else {
            for (int i = 0; i < 25; ++i) {
                final Block relative = block.getRelative(0, -i - 1, 0);
                if (relative.getType().isTransparent() && relative.getY() > 0) {
                    setBlock(relative.getWorld(), relative.getX(), relative.getY(), relative.getZ(), Material.COBBLESTONE, (byte) 5);
                }
            }
        }
    }

    private boolean calculeUpOrDown(final Block block, final boolean up) {
        if (up) {
            return block.getType().isSolid();
        }
        return !block.getType().isSolid();
    }

    private byte convertBlockFaceStairs(final BlockFace blockFace) {
        switch (blockFace) {
            case EAST: {
                return 0;
            }
            case WEST: {
                return 1;
            }
            case SOUTH: {
                return 2;
            }
            case NORTH: {
                return 3;
            }
            default: {
                return -1;
            }
        }
    }

    private BlockFace convertBlockToBlockFace(final byte data) {
        switch (data) {
            case 1: {
                return BlockFace.WEST;
            }
            case 2: {
                return BlockFace.SOUTH;
            }
            case 3: {
                return BlockFace.NORTH;
            }
            default: {
                return BlockFace.EAST;
            }
        }
    }

    public Location getLocation() {
        return this.location;
    }

    private boolean isIgnore(Location location) {
        return ignoreBlocks.stream().anyMatch(location::equals);
    }


}
