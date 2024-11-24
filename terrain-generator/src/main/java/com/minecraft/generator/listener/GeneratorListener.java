/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.generator.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.generator.Generator;
import com.minecraft.generator.util.file.FileUtil;
import com.minecraft.generator.util.schematic.Schematic;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;

import java.io.File;
import java.nio.file.Files;
import java.util.Random;
import java.util.UUID;

public class GeneratorListener implements Listener {

    private static final int maxGeneration = 410;
    private static final File saveDirectory = new File(File.separatorChar == '\\' ? "C:\\Generator\\Maps" : System.getProperty("user.home") + File.separator + "misc" + File.separator + "hg" + File.separator + "maps");
    private static final File worldBorderDirectory = new File(File.separatorChar == '\\' ? "C:\\Generator\\Border" : System.getProperty("user.home") + File.separator + "misc" + File.separator + "hg" + File.separator + "structures" + File.separator + "border");

    @org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(WorldInitEvent event) {

        final long process = System.currentTimeMillis();

        final World world = event.getWorld();

        final WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(new Location(world, 0, 60, 0));
        worldBorder.setSize(817);

        System.out.println("Generating chunks...");

        for (int x = -maxGeneration; x <= maxGeneration; x += 16) {
            for (int z = -maxGeneration; z <= maxGeneration; z += 16) {
                Chunk chunk = event.getWorld().getChunkAt(x >> 4, z >> 4);
                chunk.load(true);
            }
        }

        System.out.println("Chunks loaded! Generating border...");

        generateBorder(world, new File(worldBorderDirectory, "border.schematic"));

        System.out.println("Border pasted! Saving...");

        world.save();
        System.out.println("World saved! Saving file...");

        final File worldOutput = new File(saveDirectory, "map-" + UUID.randomUUID());

        try {
            FileUtil.copy(world.getWorldFolder(), worldOutput, (file) -> {
                String fileName = file.getFileName().toString();
                if (Files.isDirectory(file)) {
                    return fileName.equals("region");
                } else {
                    return file.getParent().getFileName().toString().equals("region") || fileName.equals("level.dat");
                }
            });
        } catch (Exception e) {
            Bukkit.shutdown();
            e.printStackTrace();
        }

        System.out.println("Done! Generation proccess took " + ((System.currentTimeMillis() - process) / 1000) + "s.");
        Bukkit.getScheduler().runTaskLater(Generator.getInstance(), Bukkit::shutdown, 200L);
    }

    private final Random random = new Random();

    private final ImmutableSet<EntityType> ACCEPTABLES_TYPES = Sets.immutableEnumSet(EntityType.BLAZE, EntityType.HORSE, EntityType.CHICKEN, EntityType.COW, EntityType.PIG, EntityType.ENDERMAN, EntityType.CREEPER, EntityType.ZOMBIE, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BAT, EntityType.OCELOT, EntityType.SHEEP, EntityType.WOLF, EntityType.WITCH, EntityType.SLIME, EntityType.VILLAGER);

    @org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(EntitySpawnEvent event) {
        if (ACCEPTABLES_TYPES.contains(event.getEntityType()))
            event.setCancelled(random.nextInt(3) <= 1);
        else
            event.setCancelled(true);
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }

    public void generateBorder(World world, File file) {

        if (!file.exists()) {
            System.out.println("Can not find World border directory!");
            return;
        }

        try {
            Schematic schematic = new Schematic(file);
            schematic.paste(new Location(world, 0, 2, 0), true);
        } catch (Exception e) {
            e.printStackTrace();
            Generator.getInstance().getLogger().info("Failed to spawn world border object.");
        }
    }
}
