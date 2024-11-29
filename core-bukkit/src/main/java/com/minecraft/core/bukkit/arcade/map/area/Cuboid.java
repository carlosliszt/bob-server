package com.minecraft.core.bukkit.arcade.map.area;

import com.minecraft.core.bukkit.arcade.map.synthetic.SyntheticLocation;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class Cuboid {

    protected final double lowX, lowY, lowZ;
    protected final double highX, highY, highZ;

    public Cuboid(SyntheticLocation pos1, SyntheticLocation pos2) {
        this.lowX = Math.min(pos1.getX(), pos2.getX());
        this.lowY = Math.min(pos1.getY(), pos2.getY());
        this.lowZ = Math.min(pos1.getZ(), pos2.getZ());
        this.highX = Math.max(pos1.getX(), pos2.getX());
        this.highY = Math.max(pos1.getY(), pos2.getY());
        this.highZ = Math.max(pos1.getZ(), pos2.getZ());
    }

    public Cuboid(Location pos1, Location pos2) {
        this.lowX = Math.min(pos1.getX(), pos2.getX());
        this.lowY = Math.min(pos1.getY(), pos2.getY());
        this.lowZ = Math.min(pos1.getZ(), pos2.getZ());
        this.highX = Math.max(pos1.getX(), pos2.getX());
        this.highY = Math.max(pos1.getY(), pos2.getY());
        this.highZ = Math.max(pos1.getZ(), pos2.getZ());
    }

    public boolean isInside(Location location) {
        return isInside(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean isInside(int x, int y, int z) {
        return x >= this.lowX && x <= this.highX && y >= this.lowY && y <= this.highY && z >= this.lowZ && z <= this.highZ;
    }

    public boolean isOutside(int x, int y, int z) {
        return !isInside(x, y, z);
    }

    public boolean isOutside(Location location) {
        return isOutside(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Set<Chunk> getChunks(World world) {
        Set<Chunk> chunks = new HashSet<>();

        int x1 = (int) lowX & ~0xf;
        int x2 = (int) highX & ~0xf;
        int z1 = (int) lowZ & ~0xf;
        int z2 = (int) highZ & ~0xf;

        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                chunks.add(world.getChunkAt(x >> 4, z >> 4));
            }
        }

        return chunks;
    }

    public Set<Block> getBlocks(World world) {
        Set<Block> blocks = new HashSet<>();

        for (int x = (int) lowX; x <= (int) highX; x++) {
            for (int y = (int) lowY; y <= (int) highY; y++) {
                for (int z = (int) lowZ; z <= (int) highZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        return blocks;
    }

}