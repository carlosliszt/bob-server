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
import org.bukkit.inventory.ItemStack;

public class Fisherman extends Kit {

    public Fisherman() {
        setIcon(new ItemStack(Material.FISHING_ROD));
        setCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.FISHING_ROD).setDescription("§7Kit Fisherman").setName("§aFish").getStack());
        setPrice(20000);
        setDefaultRank(Rank.MEMBER);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(final PlayerFishEvent event) {
        event.setExpToDrop(0);
        if (event.getCaught() instanceof LivingEntity && isUser(event.getPlayer())) {
            event.getPlayer().getItemInHand().setDurability((short) 0);

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
        if (e.getDamager() instanceof FishHook && e.getEntity() instanceof Player) {
            final FishHook o = (FishHook) e.getDamager();

            if (o.getShooter() instanceof Player) {

                final EntityFishingHook a = ((CraftFish) o).getHandle();
                a.hooked = ((CraftEntity) e.getEntity()).getHandle();

                PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(((CraftPlayer) e.getEntity()).getHandle(), (byte) 2);
                ((CraftPlayer) o.getShooter()).getHandle().playerConnection.sendPacket(packet);

                if (isUser((Player) o.getShooter())) {
                    final PlayerFishEvent fish = new PlayerFishEvent((Player) e.getEntity(), a.hooked.getBukkitEntity(), null, PlayerFishEvent.State.CAUGHT_ENTITY);
                    Bukkit.getPluginManager().callEvent(fish);
                }

                e.setCancelled(true);
            }
        }
    }

    @Override
    public void resetAttributes(User user) {

    }

}