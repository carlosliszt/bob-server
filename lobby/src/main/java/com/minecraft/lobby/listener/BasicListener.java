/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Clantag;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.user.User;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySnowball;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSnowball;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BasicListener implements Listener {

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {

        Account account = event.getAccount();

        if (account.hasClan() && !account.hasCustomName()) {
            Clan clan = Constants.getClanService().fetch(account.getData(Columns.CLAN).getAsInt());

            if (clan == null) return;

            Clantag clantag = account.getProperty("account_clan_tag").getAs(Clantag.class);
            String tag = " " + ChatColor.valueOf(clan.getColor());

            event.getTeam().setSuffix(tag + "[" + clan.getTag().toUpperCase() + "]");
        }
    }

    @EventHandler
    public void onBreakBlocks(BlockBreakEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("lobby.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSlimeMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        User user = User.fetch(p.getUniqueId());
        Location loc = Lobby.getLobby().getHall().getSpawn().clone();
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SLIME_BLOCK) {
            Block blockAboveSlime = e.getTo().getBlock();
            if (!(blockAboveSlime.getType() == Material.CARPET && blockAboveSlime.getData() == DyeColor.GREEN.getWoolData())) {
                Vector v = loc.getDirection().multiply(2.9).setY(0.66);
                p.setVelocity(v);
                p.playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 2.5F, 2.5F);
            }
        }

        if (user.isPlateKillSwitch()) {
            user.setPlateKillSwitch(false);
        }

        if (e.getTo().getY() <= 24) {
            if (user.isParkourMode()) {
                e.setTo(user.getHighestCheckpoint().getLocation());
                p.sendMessage("§b§lPARKOUR§a Você foi teleportado para o checkpoint #" + user.getHighestCheckpoint().getId() + "!");
                user.setPlateKillSwitch(true);
            } else {
                e.setTo(user.getHall().getSpawn());
            }
            return;
        }
    }

    @EventHandler
    public void onPlaceBlocks(BlockPlaceEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("lobby.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("lobby.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("lobby.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).getAccount().getProperty("lobby.build", false).getAsBoolean())
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    private final Map<UUID, Hook> grapplerHooks = new HashMap<>();

    @EventHandler
    public void onPlayerInteractGrappler(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.hasItem() && player.getItemInHand().getType() == Material.LEASH) {

            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

                if (grapplerHooks.containsKey(player.getUniqueId()))
                    grapplerHooks.get(player.getUniqueId()).remove();

                Hook hook = new Hook(((CraftPlayer) player).getHandle());

                Vector vector = player.getLocation().getDirection();

                hook.spawn(player.getEyeLocation().clone().add(vector.getX(), vector.getY(), vector.getZ()));

                grapplerHooks.put(player.getUniqueId(), hook);
            } else {
                if (!grapplerHooks.containsKey(player.getUniqueId()))
                    return;

                Hook hook = grapplerHooks.get(player.getUniqueId());

                if (!hook.isHooked()) {
                    hook.move(hook.motX, hook.motY, hook.motZ);
                    return;
                }

                hook.move(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeashEntity(PlayerLeashEntityEvent event) {
        if (event.getPlayer().getItemInHand().getType() == Material.LEASH) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
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
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(EntityChangeBlockEvent event) {
        if (!event.getBlock().getType().hasGravity()) return;
            event.setCancelled(true);
    }

    @Getter
    public static class Hook extends EntityFishingHook {

        protected static final ImmutableSet<EntityType> NOT_ACCEPTABLE_TYPES = Sets.immutableEnumSet(EntityType.FISHING_HOOK, EntityType.DROPPED_ITEM, EntityType.FIREWORK, EntityType.ARROW, EntityType.SNOWBALL, EntityType.EGG);

        private final EntityHuman entityHuman;
        private Snowball snowball;
        private EntitySnowball controller;
        private Entity attached;
        private boolean lastControllerDead;
        private boolean hooked;

        public Hook(final EntityHuman entityHuman) {
            super(entityHuman.world, entityHuman);
            this.entityHuman = entityHuman;
        }

        @Override
        public void t_() {
            if (!lastControllerDead || this.controller.dead) {
                for (Entity entity : snowball.getNearbyEntities(0.5, 0.8, 0.5)) {
                    if (!NOT_ACCEPTABLE_TYPES.contains(entity.getType())) {

                        if (entity.getEntityId() == entityHuman.getId())
                            continue;

                        if (entity instanceof Player)
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
            (snowball = this.entityHuman.getBukkitEntity().launchProjectile(Snowball.class)).setMetadata("cosmetic.grappler.hook", new FixedMetadataValue(Lobby.getLobby(), true));
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
