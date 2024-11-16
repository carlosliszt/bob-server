/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Switcher extends Kit {

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    public Switcher(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SNOW_BALL));
        setItems(new ItemFactory(Material.SNOW_BALL).setName("§aSwitch").setDescription("§7Kit Switcher").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setCooldown(5);
        setPrice(20000);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && ACCEPTABLES_INTERACT.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            player.updateInventory();

            if (checkInvincibility(player))
                return;

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setMetadata("kit.switcher", new GameMetadata(true));
            addCooldown(player.getUniqueId());
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball && isPlayer(event.getEntity())) {

            Player affected = (Player) event.getEntity();
            Snowball snowball = (Snowball) event.getDamager();

            if (!snowball.hasMetadata("kit.switcher"))
                return;


            Player shooter = (Player) snowball.getShooter();

            if (affected == shooter) {
                affected.sendMessage("§cBump!");
                return;
            }

            if (!User.fetch(affected.getUniqueId()).isAlive())
                return;

            Location shooterLocation = shooter.getLocation();
            shooter.teleport(affected.getLocation());
            affected.teleport(shooterLocation);
        }
    }
}
