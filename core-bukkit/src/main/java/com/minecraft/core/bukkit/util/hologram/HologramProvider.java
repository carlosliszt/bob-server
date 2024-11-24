/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.hologram;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.protocol.PacketListener;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HologramProvider {

    private static final Set<Hologram> hologramsList = new HashSet<>(); // Avoid duplicate entries.
    private final HologramListener hologramListener;

    public HologramProvider(BukkitGame plugin) {
        this.hologramListener = new HologramListener();
        hologramListener.setLoaded(true);
        plugin.getServer().getPluginManager().registerEvents(hologramListener, plugin);
        PacketListener.setHologramListener(hologramListener);
    }

    public List<Hologram> getPlayerHolograms(Player player) {
        return hologramsList.stream().filter(c -> c.getTarget().getUniqueId().equals(player.getUniqueId())).collect(Collectors.toList());
    }

    public void removePlayerHologram(Player p) {
        hologramsList.removeIf(c -> c.getTarget().getEntityId() == p.getEntityId());
    }

    public void changeWorld(Player player) {
        hologramsList.removeIf(c -> {

            if (player.getEntityId() == c.getTarget().getEntityId() && c.getLocation().getWorld() != player.getWorld()) {

                if (!c.isHidden())
                    c.hide();

                return true;
            }

            return false;
        });
    }


    public Set<Hologram> getHologramsList() {
        return hologramsList;
    }

    public HologramListener getHologramListener() {
        return hologramListener;
    }
}