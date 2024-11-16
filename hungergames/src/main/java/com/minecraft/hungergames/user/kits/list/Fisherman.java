/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFish;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class Fisherman extends Kit {

    public Fisherman(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.FISHING_ROD));
        setItems(new ItemFactory(Material.FISHING_ROD).setDescription("§7Kit Fisherman").setName("§aFish").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setPrice(35000);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(final PlayerFishEvent event) {
        if (event.getCaught() instanceof LivingEntity && isUser(event.getPlayer())) {

            final Location location = event.getPlayer().getLocation().clone();
            final Location locationCaught = event.getCaught().getLocation();

            location.setPitch(locationCaught.getPitch());
            location.setYaw(locationCaught.getYaw());

            event.getCaught().teleport(location);

            if (event.getHook() != null) {
                event.getHook().setBounce(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof FishHook && isPlayer(e.getEntity())) {
            final FishHook o = (FishHook) e.getDamager();

            if (o.getShooter() instanceof Player) {

                final EntityFishingHook a = ((CraftFish) o).getHandle();
                a.hooked = ((CraftEntity) e.getEntity()).getHandle();

                PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(((CraftPlayer) e.getEntity()).getHandle(), (byte) 2);
                ((CraftPlayer) o.getShooter()).getHandle().playerConnection.sendPacket(packet);

                final Player shooter = (Player) o.getShooter();

                if (isUser(shooter)) {
                    Bukkit.getPluginManager().callEvent(new PlayerFishEvent((Player) e.getEntity(), a.hooked.getBukkitEntity(), null, PlayerFishEvent.State.CAUGHT_ENTITY));
                    if (shooter.getLocation().distanceSquared(a.hooked.getBukkitEntity().getLocation()) > 64)
                        shooter.sendMessage("§aVocê fisgou " + a.hooked.getBukkitEntity().getName());
                }

                e.setCancelled(true);
            }
        }
    }
}
