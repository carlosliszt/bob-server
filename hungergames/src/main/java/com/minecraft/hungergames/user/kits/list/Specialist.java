/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Specialist extends Kit {

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);
    private final Location enchantmentTable;

    public Specialist(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.BOOK));
        setKitCategory(KitCategory.COMBAT);
        setItems(new ItemFactory(Material.BOOK).setName("§aEncantar itens").setDescription("§7Kit Specialist").getStack());

        new Location(getGame().getWorld(), 0, 2, 0).getBlock().setType(Material.AIR);

        this.enchantmentTable = new Location(getGame().getWorld(), 0, 1, 0);
        this.enchantmentTable.getBlock().setType(Material.ENCHANTMENT_TABLE);

        setPrice(35000);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.hasItem() && ACCEPTABLES_INTERACT.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            if (!enchantmentTable.getChunk().isLoaded())
                enchantmentTable.getChunk().load(true);

            event.getPlayer().openEnchanting(this.enchantmentTable, true);
        }
    }

    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        int[] costs = event.getExpLevelCostsOffered();
        costs[0] = 1;
        costs[1] = 2 + Constants.RANDOM.nextInt(3);
        costs[2] = 3 + Constants.RANDOM.nextInt(5);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExpThrownEvent(ExpBottleEvent event) {

        ThrownExpBottle bottle = event.getEntity();
        Player player = (Player) bottle.getShooter();

        if (event.getExperience() >= player.getExpToLevel())
            event.setExperience((player.getExpToLevel() / 2) + 1);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        final Player killer = event.getEntity().getKiller();

        if (killer == null)
            return;

        if (isUser(killer)) {
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (isUser(event.getPlayer()))
            event.setExpToDrop(0);
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        event.getKiller().getPlayer().giveExp(Constants.RANDOM.nextInt(4) + 6);
    }

}
