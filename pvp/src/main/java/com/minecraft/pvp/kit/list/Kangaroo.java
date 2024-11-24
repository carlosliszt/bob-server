/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Kangaroo extends Kit {

    public Kangaroo() {
        setIcon(new ItemStack(Material.FIREWORK));
        setItems(new ItemFactory(Material.FIREWORK).setName("§aBoost").setDescription("§7Kit Kangaroo").getStack());
        setCategory(KitCategory.MOVEMENT);
        setCooldown(7);
        setPrice(30000);
        setCombatCooldown(true);
        setDefaultRank(Rank.MEMBER);
    }

    private final Set<UUID> jumpCooldown = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL && isUser(event.getPlayer())) {
            if (event.hasItem()) {
                if (isItem(event.getPlayer())) {
                    event.setCancelled(true);

                    Player player = event.getPlayer();

                    if (jumpCooldown.contains(player.getUniqueId()))
                        return;

                    if (isCombat(player)) {
                        dispatchCooldown(player);
                        return;
                    }

                    Vector vector = player.getEyeLocation().getDirection();
                    if (player.isSneaking()) {
                        vector = vector.multiply(1.91F).setY(0.5F);
                    } else {
                        vector = vector.multiply(0.5F).setY(1F);
                    }

                    player.setFallDistance(-1);
                    player.setVelocity(vector);
                    jumpCooldown.add(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (jumpCooldown.contains(player.getUniqueId())) {
            if (player.isOnGround() || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
                jumpCooldown.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (isUser((Player) event.getEntity()))
                event.setDamage(Math.min(event.getDamage(), 4));
        }
    }

    @Override
    public long getCombatTime() {
        return 6;
    }

    @Override
    public void resetAttributes(User user) {
        jumpCooldown.remove(user.getUniqueId());
    }

}