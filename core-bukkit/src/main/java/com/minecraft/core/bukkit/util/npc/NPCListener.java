/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.npc;

import com.minecraft.core.bukkit.BukkitGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
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
public class NPCListener implements Listener {

    private boolean loaded;

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (NPC npc : BukkitGame.getEngine().getNPCProvider().getNpcs()) {
            if (npc.getLocation() == null || isSameChunk(npc.getLocation(), event.getChunk()) || !npc.isHidden())
                continue;
            Player viewer = npc.getViewer();
            if (!viewer.getWorld().getUID().equals(npc.getLocation().getWorld().getUID()))
                continue;
            double distanceSquared = viewer.getLocation().distanceSquared(npc.getLocation());
            if (distanceSquared <= (2500) || distanceSquared <= (viewer.spigot().getViewDistance() << 4))
                npc.spawn(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)

    public void onChunkUnload(ChunkUnloadEvent event) {
        for (NPC npc : BukkitGame.getEngine().getNPCProvider().getNpcs()) {
            if (npc.getLocation() == null || isSameChunk(npc.getLocation(), event.getChunk()) || npc.isHidden())
                continue;
            Player viewer = npc.getViewer();
            if (!viewer.getWorld().getUID().equals(npc.getLocation().getWorld().getUID()))
                continue;
            npc.destroy(false);
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
        World from = event.getFrom();
        for (NPC npc : BukkitGame.getEngine().getNPCProvider().getPlayerHumans(event.getPlayer())) {
            if (!npc.isHidden() && npc.getLocation().getWorld().getUID().equals(from.getUID()))
                npc.destroy(false);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (NPC npc : BukkitGame.getEngine().getNPCProvider().getPlayerHumans(player)) {
            if (!npc.isHidden() && npc.getLocation().getWorld().getUID().equals(player.getWorld().getUID()))
                npc.destroy(false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BukkitGame.getEngine().getNPCProvider().remove(event.getPlayer());
    }

    void handleMove(Player player) {
        for (NPC npc : BukkitGame.getEngine().getNPCProvider().getPlayerHumans(player)) {
            if (npc.isHidden() && npc.inRangeOf(player) && npc.inViewOf(player)) {
                npc.spawn(false);
            } else if (!npc.isHidden() && !npc.inRangeOf(player)) {
                npc.destroy(false);
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

        Iterator<NPC> NPCIterator = BukkitGame.getEngine().getNPCProvider().getNpcs().iterator();

        while (NPCIterator.hasNext()) {
            NPC hologram = NPCIterator.next();
            NPCIterator.remove();
            hologram.destroy(false);
        }
    }
}