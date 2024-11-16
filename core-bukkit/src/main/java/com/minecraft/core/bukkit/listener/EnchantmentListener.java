/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.listener;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;

public class EnchantmentListener extends DynamicListener {

    /* Register it manually in the plugin that you need to avoid unnecessary processing. */

    private final ItemStack enchantmentDepend = new ItemFactory(Material.INK_SACK).setDurability(4).setAmount(64).getStack();

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() instanceof EnchantingInventory)
            event.getInventory().setItem(1, enchantmentDepend);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() instanceof EnchantingInventory)
            event.getInventory().setItem(1, null);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() instanceof EnchantingInventory) {
            if (e.getSlot() == 1)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent e) {
        e.getInventory().setItem(1, enchantmentDepend);
    }

}