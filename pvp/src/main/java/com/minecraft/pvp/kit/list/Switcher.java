/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.GameMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Switcher extends Kit {

    public Switcher() {
        setIcon(new ItemStack(Material.SNOW_BALL));
        setCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.SNOW_BALL).setName("§aSwitch").setDescription("§7Kit Switcher").getStack());
        setPrice(15000);
    }

    @Override
    public void resetAttributes(User user) {

    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && ACCEPTABLES_INTERACT.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getPlayer())) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            player.updateInventory();

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 3);

            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setMetadata("kit.switcher", new GameMetadata(true));
            snowball.setShooter(player);
            snowball.setVelocity(snowball.getVelocity().multiply(2));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball && event.getEntity() instanceof Player) {
            Snowball snowball = (Snowball) event.getDamager();

            if (!snowball.hasMetadata("kit.switcher"))
                return;

            Player shooter = (Player) snowball.getShooter();
            Player affected = (Player) event.getEntity();

            User affectedUser = User.fetch(affected.getUniqueId());

            if (affected == shooter || affectedUser == null || Vanish.getInstance().isVanished(affected.getUniqueId()) || !affected.getWorld().getName().equals(shooter.getWorld().getName()) || affectedUser.isKept())
                return;

            Location shooterLocation = shooter.getLocation();

            shooter.teleport(affected.getLocation());
            affected.teleport(shooterLocation);

            affectedUser.addCombat(shooter.getUniqueId());
            User.fetch(shooter.getUniqueId()).addCombat(affectedUser.getUniqueId());
        }
    }

}