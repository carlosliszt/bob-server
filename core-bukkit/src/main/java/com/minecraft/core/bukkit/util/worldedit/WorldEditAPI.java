/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.worldedit;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.*;

public class WorldEditAPI {

    @Getter
    private static final WorldEditAPI instance = new WorldEditAPI();

    public List<Location> makePyramid(Location position, int size, boolean filled) {
        Set<Location> location = new HashSet<>();

        int height = size;

        for (int y = 0; y <= height; ++y) {
            size--;
            for (int x = 0; x <= size; ++x) {
                for (int z = 0; z <= size; ++z) {

                    if ((filled && z <= size && x <= size) || z == size || x == size) {

                        location.add(position.clone().add(x, y, z));
                        location.add(position.clone().add(-x, y, z));
                        location.add(position.clone().add(x, y, -z));
                        location.add(position.clone().add(-x, y, -z));
                    }
                }
            }
        }
        return new ArrayList<>(location);
    }


    public List<Location> makeSphere(Location pos, double radiusX, double radiusY, double radiusZ, boolean filled) {
        Set<Location> location = new HashSet<>();

        radiusX += 0.5;
        radiusY += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break;
                    }

                    if (!filled) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    location.add(pos.clone().add(x, y, z));
                    location.add(pos.clone().add(-x, y, z));
                    location.add(pos.clone().add(x, -y, z));
                    location.add(pos.clone().add(x, y, -z));
                    location.add(pos.clone().add(-x, -y, z));
                    location.add(pos.clone().add(x, -y, -z));
                    location.add(pos.clone().add(-x, y, -z));
                    location.add(pos.clone().add(-x, -y, -z));
                }
            }
        }
        return new ArrayList<>(location);
    }

    public List<Location> makeCylinder(Location pos, double radiusX, double radiusZ, int height, boolean filled) {
        Set<Location> location = new HashSet<>();

        radiusX += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break;
                }

                if (!filled) {
                    if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                        continue;
                    }
                }

                for (int y = 0; y < height; ++y) {

                    location.add(pos.clone().add(x, y, z));
                    location.add(pos.clone().add(-x, y, z));
                    location.add(pos.clone().add(x, y, -z));
                    location.add(pos.clone().add(-x, y, -z));
                }
            }
        }
        return new ArrayList<>(location);
    }


    public List<Location> getBlocksBetween(Location loc1, Location loc2) {

        if (loc1 == null || loc2 == null)
            return Collections.emptyList();

        List<Location> locations = new ArrayList<>();
        int lowerX = Math.min(loc1.getBlockX(), loc2.getBlockX()), lowerY = Math.min(loc1.getBlockY(), loc2.getBlockY()), lowerZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int higherX = Math.max(loc1.getBlockX(), loc2.getBlockX()), higherY = Math.max(loc1.getBlockY(), loc2.getBlockY()), higherZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = lowerX; x <= higherX; x++) {
            for (int y = lowerY; y <= higherY; y++) {
                for (int z = lowerZ; z <= higherZ; z++) {
                    locations.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }
        return locations;
    }

    public List<Location> getWalls(Location loc1, Location loc2) {
        List<Location> locations = new ArrayList<>();
        int lowerX = Math.min(loc1.getBlockX(), loc2.getBlockX()), lowerY = Math.min(loc1.getBlockY(), loc2.getBlockY()), lowerZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int higherX = Math.max(loc1.getBlockX(), loc2.getBlockX()), higherY = Math.max(loc1.getBlockY(), loc2.getBlockY()), higherZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = lowerX; x <= higherX; x++) {
            for (int y = lowerY; y <= higherY; y++) {
                for (int z = lowerZ; z <= higherZ; z++) {
                    if (x == lowerX || z == lowerZ || -z == -lowerZ || -x == -lowerX || x == higherX || z == higherZ || -z == -higherZ || -x == -higherX)
                        locations.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }
        return locations;
    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }

    public void setBlock(Location location, Material material, byte data) {
        setBlock(location, material, data, true);
    }

    public void setBlock(Location location, Material material, byte data, boolean forceLight) {

        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        if (y >= location.getWorld().getMaxHeight() || y < 0)
            return;

        BlockPosition blockPosition = new BlockPosition(x, y, z);

        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(material.getId() + (data << 12));
        ChunkSection cs = nmsChunk.getSections()[y >> 4];
        if (cs == null) {
            cs = new ChunkSection(y >> 4 << 4, true);
            nmsChunk.getSections()[y >> 4] = cs;
        }
        cs.setType(x & 15, y & 15, z & 15, ibd);

        if (forceLight) {
            setLightLevel(location, 15);
        }

        nmsWorld.notify(blockPosition);
    }

    public void setLightLevel(Location location, final int level) {
        final net.minecraft.server.v1_8_R3.World w = ((CraftWorld) location.getWorld()).getHandle();
        final BlockPosition bp = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        w.a(EnumSkyBlock.BLOCK, bp, level);
        w.a(EnumSkyBlock.SKY, bp, level);
    }
}
