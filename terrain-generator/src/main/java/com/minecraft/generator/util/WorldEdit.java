/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.generator.util;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

@Deprecated
public class WorldEdit {

    public static void setBlock(Location location, int id, byte data, boolean forceLight) {

        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        if (y >= location.getWorld().getMaxHeight() || y < 0)
            return;

        BlockPosition blockPosition = new BlockPosition(x, y, z);

        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(id + (data << 12));
        ChunkSection cs = nmsChunk.getSections()[y >> 4];
        if (cs == null) {
            cs = new ChunkSection(y >> 4 << 4, true);
            nmsChunk.getSections()[y >> 4] = cs;
        }
        cs.setType(x & 15, y & 15, z & 15, ibd);

        if (forceLight)
            setLightLevel(location, 15);
        nmsWorld.notify(blockPosition);
    }

    public static void setLightLevel(Location location, final int level) {
        final net.minecraft.server.v1_8_R3.World w = ((CraftWorld) location.getWorld()).getHandle();
        final BlockPosition bp = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        w.a(EnumSkyBlock.BLOCK, bp, level);
    }
}
