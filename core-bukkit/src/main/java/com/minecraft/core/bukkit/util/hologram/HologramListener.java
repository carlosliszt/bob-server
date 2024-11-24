/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.hologram;

import com.minecraft.core.bukkit.BukkitGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Iterator;

@Getter
@Setter
public class HologramListener implements Listener {

    private boolean loaded;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Hologram hologram : BukkitGame.getEngine().getHologramProvider().getHologramsList()) {

            if (hologram.getLocation() == null || isSameChunk(hologram.getLocation(), event.getChunk()) || !hologram.isHidden())
                continue;

            Player viewer = hologram.getTarget();

            if (!viewer.getWorld().getUID().equals(hologram.getLocation().getWorld().getUID()))
                continue;

            double distanceSquared = viewer.getLocation().distanceSquared(hologram.getLocation());

            if (distanceSquared <= (2500) || distanceSquared <= (viewer.spigot().getViewDistance() << 4))
                hologram.show();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Hologram hologram : BukkitGame.getEngine().getHologramProvider().getHologramsList()) {

            if (hologram.getLocation() == null || isSameChunk(hologram.getLocation(), event.getChunk()) || hologram.isHidden())
                continue;

            Player viewer = hologram.getTarget();

            if (!viewer.getWorld().getUID().equals(hologram.getLocation().getWorld().getUID()))
                continue;

            hologram.hide();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
            handleMove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        BukkitGame.getEngine().getHologramProvider().changeWorld(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (Hologram hologram : BukkitGame.getEngine().getHologramProvider().getPlayerHolograms(event.getEntity())) {
            if (!hologram.isHidden() && hologram.getLocation().getWorld().getUID().equals(player.getWorld().getUID()))
                hologram.hide();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BukkitGame.getEngine().getHologramProvider().removePlayerHologram(event.getPlayer());
    }

    void handleMove(Player player) {
        for (Hologram hologram : BukkitGame.getEngine().getHologramProvider().getPlayerHolograms(player)) {
            if (hologram.isHidden() && hologram.inRangeOf(player) && hologram.inViewOf(player)) {
                hologram.show();
            } else if (!hologram.isHidden() && !hologram.inRangeOf(player)) {
                hologram.hide();
            }
        }
    }

    private static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    private static boolean isSameChunk(Location loc, Chunk chunk) {
        return (chunk.getWorld().getUID().equals(loc.getWorld().getUID()) && getChunkCoordinate(loc.getBlockX()) != chunk.getX()) || getChunkCoordinate(loc.getBlockZ()) != chunk.getZ();
    }

    public void unload() {
        this.loaded = false;
        HandlerList.unregisterAll(this);

        Iterator<Hologram> hologramIterator = BukkitGame.getEngine().getHologramProvider().getHologramsList().iterator();

        while (hologramIterator.hasNext()) {
            Hologram hologram = hologramIterator.next();
            hologramIterator.remove();
            hologram.hide();
        }
    }
}