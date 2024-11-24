/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.listeners;

import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.types.Fps;
import com.minecraft.pvp.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemFrameListener implements Listener {

    @EventHandler
    public void onPlayerOpenFrame(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame))
            return;
        ItemFrame frame = (ItemFrame) event.getRightClicked();
        Player p = event.getPlayer();
        event.setCancelled(true);

        if (frame.getItem().getType() == Material.AIR && User.fetch(p.getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean()) {
            frame.setItem(p.getItemInHand());
            frame.setRotation(Rotation.NONE);
            return;
        }

        if (frame.getItem().getType() == Material.MUSHROOM_SOUP) {
            p.openInventory(soup());
        } else if (frame.getItem().getType() == Material.RED_MUSHROOM) {
            UUID game = User.fetch(p.getUniqueId()).getGame().getUniqueId();
            if (game.equals(PvP.getPvP().getGameStorage().getGame(Fps.class).getUniqueId()))
                p.openInventory(recraft());
            else
                p.openInventory(recraftLava());
        }
    }

    @EventHandler
    public void onHanging(HangingBreakEvent event) {
        if (event.getCause() != RemoveCause.ENTITY) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHanging(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame))
            return;
        if (!(event.getRemover() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        Player p = (Player) event.getRemover();
        if (!User.fetch(p.getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            event.setCancelled(true);
            return;
        }
        Player p = (Player) event.getDamager();
        if (!User.fetch(p.getUniqueId()).getAccount().getProperty("pvp.build", false).getAsBoolean()) {
            event.setCancelled(true);
        }
    }

    private Inventory soup() {
        Inventory inventory = Bukkit.createInventory(null, 27, "Soup");

        ItemStack itemStack = new ItemStack(Material.MUSHROOM_SOUP);

        for (int i = 0; i < 27; i++)
            inventory.setItem(i, itemStack);

        return inventory;
    }

    private Inventory recraft() {
        Inventory recraft = Bukkit.createInventory(null, 27, "Recraft");

        ItemStack itemStack1 = new ItemStack(Material.RED_MUSHROOM, 64);
        ItemStack itemStack2 = new ItemStack(Material.BROWN_MUSHROOM, 64);
        ItemStack itemStack3 = new ItemStack(Material.BOWL, 64);

        for (int i = 0; i < 9; i++) {
            recraft.setItem(i, itemStack1);
        }

        for (int i = 9; i < 18; i++) {
            recraft.setItem(i, itemStack2);
        }

        for (int i = 18; i < 27; i++) {
            recraft.setItem(i, itemStack3);
        }

        return recraft;
    }

    private Inventory recraftLava() {
        Inventory inventory = Bukkit.createInventory(null, 27, "Recraft");

        ItemStack RED_MUSHROOM = new ItemStack(Material.RED_MUSHROOM, 64);
        ItemStack BROWN_MUSHROOM = new ItemStack(Material.BROWN_MUSHROOM, 64);
        ItemStack BOWL = new ItemStack(Material.BOWL, 64);
        ItemStack INK_SACK = new ItemStack(Material.INK_SACK, 64, (short) 3);

        inventory.setItem(1, BOWL);
        inventory.setItem(2, BROWN_MUSHROOM);
        inventory.setItem(3, RED_MUSHROOM);

        inventory.setItem(5, BOWL);
        inventory.setItem(6, INK_SACK);
        inventory.setItem(7, INK_SACK);

        inventory.setItem(10, BOWL);
        inventory.setItem(11, BROWN_MUSHROOM);
        inventory.setItem(12, RED_MUSHROOM);

        inventory.setItem(14, BOWL);
        inventory.setItem(15, INK_SACK);
        inventory.setItem(16, INK_SACK);

        inventory.setItem(19, BOWL);
        inventory.setItem(20, BROWN_MUSHROOM);
        inventory.setItem(21, RED_MUSHROOM);

        inventory.setItem(23, BOWL);
        inventory.setItem(24, INK_SACK);
        inventory.setItem(25, INK_SACK);

        return inventory;
    }

}