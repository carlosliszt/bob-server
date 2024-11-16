/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Kangaroo extends Kit {

    private final Set<UUID> jumpCooldown = new HashSet<>();

    public Kangaroo(HungerGames hungerGames) {
        super(hungerGames);
        setItems(new ItemFactory(Material.FIREWORK).setName("§aBoost").setDescription("§7Kit Kangaroo").getStack());
        setIcon(Pattern.of(Material.FIREWORK));
        setKitCategory(KitCategory.MOVEMENT);
        setCombatCooldown(true);
        setPrice(50000);
    }

    @Variable(name = "hg.kit.kangaroo.exhaustion", permission = Rank.ADMINISTRATOR)
    private float exhaustion = 0.75F;

    @Variable(name = "hg.kit.kangaroo.vector.sneaking_multiply", permission = Rank.ADMINISTRATOR)
    private float sneaking_multiply = 1.91F;

    @Variable(name = "hg.kit.kangaroo.vector.sneaking_y", permission = Rank.ADMINISTRATOR)
    private float sneaking_y = 0.5F;

    @Variable(name = "hg.kit.kangaroo.vector.default_multiply", permission = Rank.ADMINISTRATOR)
    private float multiply = 0.5F;

    @Variable(name = "hg.kit.kangaroo.vector.default_y", permission = Rank.ADMINISTRATOR)
    private float y = 1F;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL && isUser(event.getPlayer())) {
            if (event.hasItem()) {
                if (isItem(event.getItem())) {
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
                        vector = vector.multiply(sneaking_multiply).setY(sneaking_y);
                    } else {
                        vector = vector.multiply(multiply).setY(y);
                    }

                    player.setFallDistance(-1);
                    player.setVelocity(vector);
                    player.setExhaustion(player.getExhaustion() + exhaustion);
                    jumpCooldown.add(player.getUniqueId());
                }
            }
        }
    }

    @Variable(name = "hg.kit.kangaroo.combat_cooldown", permission = Rank.ADMINISTRATOR)
    private double duration = 6;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (jumpCooldown.contains(player.getUniqueId())) {
            if (player.getLocation().getBlock()/*.getRelative(BlockFace.DOWN)*/.getType() != Material.AIR || player.isOnGround())
                jumpCooldown.remove(player.getUniqueId());
        }
    }

    @Variable(name = "hg.kit.kangaroo.fall_protection", permission = Rank.ADMINISTRATOR)
    public boolean fallProtection = true;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        if (isPlayer(event.getEntity())) {
            Player player = (Player) event.getEntity();
            if (fallProtection && isUser(player)) {
                if ((player.getHealth() - event.getDamage()) <= 0) {
                    event.setCancelled(true);
                    player.setHealth(1);
                    player.damage(0);
                }
            }
        }
    }

    @Override
    public double getCombatTime() {
        return duration;
    }
}
