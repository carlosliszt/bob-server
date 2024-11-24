/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Anchor extends Kit {

    protected final Set<UUID> uuidSet = new HashSet<>();

    public Anchor(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.ANVIL));
        setKitCategory(KitCategory.COMBAT);
        setActive(false, false);
        setPrice(25000);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent e) {
        if (uuidSet.contains(e.getPlayer().getUniqueId())) {
            e.setVelocity(new Vector(0, -1, 0));
            uuidSet.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnchor(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {

            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            User target = User.fetch(player.getUniqueId());
            User attacker = User.fetch(damager.getUniqueId());

            Kit kit = getKit("Neo");

            if (kit.isUser(target) || kit.isUser(attacker))
                return;

            if (isUser(target) || isUser(damager)) {
                player.setVelocity(new Vector(0, -1, 0));
                uuidSet.add(player.getUniqueId());
                Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> player.setVelocity(new Vector(0, -1, 0)));
            }
        }
    }

}