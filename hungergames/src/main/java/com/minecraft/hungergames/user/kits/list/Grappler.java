/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySnowball;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSnowball;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Grappler extends Kit {

    private final Map<UUID, Hook> grapplerHooks = new HashMap<>();

    public Grappler(HungerGames hungerGames) {
        super(hungerGames);
        setCombatCooldown(true);
        setIcon(Pattern.of(Material.LEASH));
        setItems(new ItemFactory(Material.LEASH).setName("§aHook").setDescription("§7Kit Grappler").getStack());
        setPrice(50000);
        setKitCategory(KitCategory.MOVEMENT);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.hasItem() && isUser(player) && isItem(event.getItem())) {

            if (isCombat(player)) {
                dispatchCooldown(player);
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

                if (grapplerHooks.containsKey(player.getUniqueId()))
                    grapplerHooks.get(player.getUniqueId()).remove();

                Hook hook = new Hook(((CraftPlayer) player).getHandle());

                Vector vector = player.getLocation().getDirection();

                hook.spawn(player.getEyeLocation().clone().add(vector.getX(), vector.getY(), vector.getZ()));

            /*
                hook.move(vector.getX() * 7.0, vector.getY() * 7.0, vector.getZ() * 7.0);
                Totally unnecessary method, who did this shit?
             */

                grapplerHooks.put(player.getUniqueId(), hook);
            } else {
                if (!grapplerHooks.containsKey(player.getUniqueId()))
                    return;

                Hook hook = grapplerHooks.get(player.getUniqueId());

                if (!hook.isHooked()) {
                    hook.move(hook.motX, hook.motY, hook.motZ);
                    return;
                }

                Gladiator gladiator = (Gladiator) getKit("Gladiator");

                if (hook.locY > 128 && !gladiator.isGladiator(player))
                    return;

                hook.move(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeashEntity(PlayerLeashEntityEvent event) {
        if (isItem(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("hg.kit.grappler.hook"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (grapplerHooks.containsKey(player.getUniqueId()))
            grapplerHooks.remove(player.getUniqueId()).remove();
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (grapplerHooks.containsKey(player.getUniqueId()))
            grapplerHooks.remove(player.getUniqueId()).remove();
    }

    @EventHandler
    public void onLivingUserDie(LivingUserDieEvent event) {
        Player player = event.getUser().getPlayer();

        if (grapplerHooks.containsKey(player.getUniqueId()))
            grapplerHooks.remove(player.getUniqueId()).remove();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata("hg.kit.grappler.hook"))
            event.setCancelled(true);
    }

    @Variable(name = "hg.kit.grappler.combat_cooldown", permission = Rank.ADMINISTRATOR)
    public double duration = 5;

    @Variable(name = "hg.kit.grappler.attach_entities", permission = Rank.ADMINISTRATOR)
    @Getter
    public boolean attachEntities = true;

    @Override
    public double getCombatTime() {
        return duration;
    }

    @Getter
    public static class Hook extends EntityFishingHook {

        protected static final ImmutableSet<EntityType> NOT_ACCEPTABLE_TYPES = Sets.immutableEnumSet(EntityType.FISHING_HOOK, EntityType.DROPPED_ITEM, EntityType.FIREWORK, EntityType.ARROW, EntityType.SNOWBALL, EntityType.EGG);
        private final Grappler kit;
        private final EntityHuman entityHuman;
        private Snowball snowball;
        private EntitySnowball controller;
        private Entity attached;
        private boolean lastControllerDead;
        private boolean hooked;

        public Hook(final EntityHuman entityHuman) {
            super(entityHuman.world, entityHuman);
            this.kit = (Grappler) HungerGames.getInstance().getKitStorage().getKit("Grappler");
            this.entityHuman = entityHuman;
        }

        @Override
        public void t_() {
            if (!lastControllerDead || this.controller.dead) {
                if (kit.isAttachEntities()) {
                    for (Entity entity : snowball.getNearbyEntities(0.5, 0.8, 0.5)) {
                        if (!NOT_ACCEPTABLE_TYPES.contains(entity.getType())) {

                            if (entity.getEntityId() == entityHuman.getId())
                                continue;

                            if (entity instanceof Player && !User.fetch(entity.getUniqueId()).isAlive())
                                continue;

                            this.controller.die();
                            this.attached = entity;
                            this.hooked = true;
                            final Location loc = entity.getLocation().clone().add(0.0, 0.5, 0.0);
                            this.locX = loc.getX();
                            this.locY = loc.getY() + 1.2;
                            this.locZ = loc.getZ();
                            this.motX = 0.0;
                            this.motY = 0.04;
                            this.motZ = 0.0;
                            break;
                        }
                    }
                }
            }

            lastControllerDead = controller.dead;
            try {
                this.locX = attached.getLocation().getX();
                this.locY = attached.getLocation().getY();
                this.locZ = attached.getLocation().getZ();
                this.motX = 0.0;
                this.motY = 0.05;
                this.motZ = 0.0;
                hooked = true;
            } catch (Exception exception) {
                if (controller.dead)
                    hooked = true;

                this.locX = controller.locX;
                this.locY = controller.locY;
                this.locZ = controller.locZ;
            }
        }

        public void spawn(final Location location) {
            (snowball = this.entityHuman.getBukkitEntity().launchProjectile(Snowball.class)).setMetadata("hg.kit.grappler.hook", new GameMetadata(true));
            this.controller = ((CraftSnowball) snowball).getHandle();
            controller.setInvisible(true);
            snowball.setVelocity(snowball.getVelocity().multiply(1.42));
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.controller.getId());
            Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));
            ((CraftWorld) location.getWorld()).getHandle().addEntity(this);
        }

        public void move(Player player) {
            if (getAttached() instanceof LivingEntity) {

                double velocity_y, multiply = getBukkitEntity().getLocation().distance(player.getLocation());
                double velocity_x = (1.0 + 0.1 * multiply) * (getBukkitEntity().getLocation().getX() - player.getLocation().getX()) / multiply;
                double velocity_z = (1.0 + 0.1 * multiply) * (getBukkitEntity().getLocation().getZ() - player.getLocation().getZ()) / multiply;
                boolean multiplyVector = getAttached().getLocation().getBlock().getY() >= player.getLocation().getBlockY();
                double multiplyValue = Math.min(2, Math.abs(getBukkitEntity().getLocation().getY() - player.getLocation().getBlockY()));

                Vector vector = player.getLocation().getDirection();
                if (getBukkitEntity().getLocation().getBlockY() < player.getLocation().getBlockY()) {
                    velocity_y = -1.0;
                } else {
                    velocity_y = (0.3 + 0.09 * multiply) * (getBukkitEntity().getLocation().getY() - player.getLocation().getY()) / multiply;
                }
                velocity_y += 0.1;

                if (multiplyVector)
                    velocity_y *= multiplyValue;

                vector.setX(velocity_x);
                vector.setY(velocity_y);
                vector.setZ(velocity_z);
                player.setVelocity(vector);

            } else {
                double velocity_y, multiply = getBukkitEntity().getLocation().distance(player.getLocation());
                double velocity_x = (1.0 + 0.03500000000000002 * multiply) * (getBukkitEntity().getLocation().getX() - player.getLocation().getX()) / multiply;
                double velocity_z = (1.0 + 0.03500000000000002 * multiply) * (getBukkitEntity().getLocation().getZ() - player.getLocation().getZ()) / multiply;
                boolean multiplyVector = getBukkitEntity().getLocation().getBlock().getY() >= player.getLocation().getBlockY();
                double multiplyValue = Math.min(2, Math.abs(getBukkitEntity().getLocation().getY() - player.getLocation().getBlockY()));

                Vector vector = player.getLocation().getDirection();

                if (getBukkitEntity().getLocation().getBlockY() < player.getLocation().getBlockY())
                    velocity_y = 0.15;
                else
                    velocity_y = (0.7 + 0.03 * multiply) * (getBukkitEntity().getLocation().getY() - player.getLocation().getY()) / multiply;

                velocity_y += 0.1;

                if (multiplyVector)
                    velocity_y *= multiplyValue;

                vector.setX(velocity_x);
                vector.setY(velocity_y);
                vector.setZ(velocity_z);
                player.setVelocity(vector);
            }
            if (Math.round(locY) > player.getLocation().getBlockY() && player.getFallDistance() > 20.0F) {
                player.setVelocity(new Vector());
                player.setNoDamageTicks(0);
            }
            player.setFallDistance(0.0F);
        }

        public void remove() {
            super.die();
        }

        public void die() {
        }
    }
}
