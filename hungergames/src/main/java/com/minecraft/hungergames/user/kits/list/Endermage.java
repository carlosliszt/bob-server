/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Endermage extends Kit {

    public Endermage(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.ENDER_PORTAL_FRAME));
        setItems(new ItemFactory(Material.ENDER_PORTAL_FRAME).setName("§aPuxar").setDescription("§7Kit Endermage").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
        setCooldown(3);
    }

    @Variable(name = "hg.kit.endermage.max_distance", permission = Rank.ADMINISTRATOR)
    public double distance = 3.5;
    @Variable(name = "hg.kit.endermage.invincible_time", permission = Rank.ADMINISTRATOR)
    public int invincibleTime = 5;

    private final Map<UUID, EndermagePortal> portalMap = new HashMap<>();
    private final Set<UUID> uuidSet = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction() == Action.RIGHT_CLICK_BLOCK && isUser(event.getPlayer()) && isItem(event.getItem())) {

            Player player = event.getPlayer();

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            event.setCancelled(true);

            Block block = event.getClickedBlock();

            player.updateInventory();

            if (block.getType() == Material.BEDROCK || block.getType() == Material.ENDER_PORTAL_FRAME)
                return;

            if (portalMap.containsKey(player.getUniqueId()))
                return;

            Gladiator gladiator = (Gladiator) getKit("Gladiator");

            if (gladiator.isGladiator(player))
                return;

            EndermagePortal endermagePortal = new EndermagePortal(getUser(player.getUniqueId()), block);
            portalMap.put(player.getUniqueId(), endermagePortal);
            endermagePortal.start();
        }
    }

    @Getter
    private class EndermagePortal {

        private final User holder;
        private final Block block;
        private final Pattern rollbackBlock;
        private int time;

        public EndermagePortal(User player, Block block) {
            this.time = 20;
            this.holder = player;
            this.block = block;
            this.rollbackBlock = Pattern.of(block.getType(), block.getData());
        }

        public void start() {
            block.setType(Material.ENDER_PORTAL_FRAME);
            internalStart();
        }

        protected void internalStart() {

            new BukkitRunnable() {

                boolean cancel = false;

                public void run() {
                    time--;

                    if (!holder.isOnline() || holder.getPlayer().getLocation().distanceSquared(block.getLocation()) > 240 || block.getType() != Material.ENDER_PORTAL_FRAME || !isUser(holder)) {
                        cancel = true;
                    } else {

                        Set<User> candidates = getNearbyUsers();

                        if (!candidates.isEmpty()) {

                            final Location teleport = block.getLocation().clone().add(0.5, 1.1, 0.5);

                            for (User user : candidates) {
                                Player player = user.getPlayer();

                                player.teleport(teleport);
                                uuidSet.add(player.getUniqueId());

                                Bukkit.getScheduler().runTaskLater(getHungerGames(), () -> uuidSet.remove(player.getUniqueId()), 100L);
                            }

                            Player holderPlayer = holder.getPlayer();

                            holderPlayer.teleport(teleport);
                            uuidSet.add(holderPlayer.getUniqueId());

                            teleport.getWorld().playSound(teleport, Sound.ENDERMAN_TELEPORT, 1F, 1F);
                            Bukkit.getScheduler().runTaskLater(getHungerGames(), () -> uuidSet.remove(holderPlayer.getUniqueId()), 100L);

                            this.cancel = true;
                        }

                        if (time == 0)
                            this.cancel = true;
                    }

                    if (cancel) {
                        this.cancel();
                        finish();
                    } else {
                        if (time % 5 == 0) {
                            for (int angle = 0; angle < 360; angle += 18) {
                                double x = Math.sin(Math.toRadians(angle)) * 0.5;
                                double z = Math.cos(Math.toRadians(angle)) * 0.5;
                                block.getWorld().playEffect(block.getLocation().clone().add(x + 0.5, 1, z + 0.5), Effect.ENDER_SIGNAL, Material.ENDER_PORTAL_FRAME.getId());
                            }
                        }
                    }
                }
            }.runTaskTimer(getPlugin(), 0, 10);
        }

        public void finish() {
            if (block.getType() == Material.ENDER_PORTAL_FRAME) {
                block.setType(getRollbackBlock().getMaterial());
                block.setData(getRollbackBlock().getData());
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 120);
            }
            portalMap.remove(getHolder().getUniqueId());
        }

        public boolean isCandidate(Location player) {
            return absolute(block.getX() - player.getX()) < distance && Math.abs(block.getZ() - player.getZ()) < distance && Math.abs(block.getY() - player.getY()) > 3.5;
        }

        public Set<User> getNearbyUsers() {
            final Set<User> users = new HashSet<>();

            for (User user : getPlugin().getUserStorage().getAliveUsers()) {

                if (user.getUniqueId() == holder.getUniqueId())
                    continue;

                if (isUser(user))
                    continue;

                if (getKit("AntiTower").isUser(user))
                    continue;

                Location playerLocation = user.getPlayer().getLocation();

                if (!isCandidate(playerLocation))
                    continue;

                if (playerLocation.getY() > 127)
                    continue;

                users.add(user);
            }
            return users;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityDamageEvent(EntityDamageEvent event) {
        Entity damaged = event.getEntity();
        if (damaged.isDead())
            return;
        if (!(damaged instanceof Player))
            return;
        if (uuidSet.contains(damaged.getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;
        if (!(damager instanceof Player))
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        if (uuidSet.contains(damager.getUniqueId()))
            event.setCancelled(true);
    }

}
