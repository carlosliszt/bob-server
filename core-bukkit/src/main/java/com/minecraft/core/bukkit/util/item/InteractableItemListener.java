/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.item;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractableItemListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() == Material.AIR)
            return;

        ItemStack stack = event.getItem();

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        if (nmsStack == null || nmsStack.getTag() == null || !nmsStack.getTag().hasKey("interactable"))
            return;

        int id = nmsStack.getTag().getInt("interactable");

        InteractableItem.Interact handler = InteractableItem.getInteract(id);

        if (handler == null || handler.getInteractType() == InteractableItem.InteractType.PLAYER)
            return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        event.setCancelled(handler.onInteract(player, null, event.getClickedBlock(), stack, action.name().contains("RIGHT") ? InteractableItem.InteractAction.RIGHT : InteractableItem.InteractAction.LEFT));
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR)
            return;

        ItemStack stack = event.getPlayer().getItemInHand();

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        if (nmsStack == null || nmsStack.getTag() == null || !nmsStack.getTag().hasKey("interactable"))
            return;

        int id = nmsStack.getTag().getInt("interactable");

        InteractableItem.Interact handler = InteractableItem.getInteract(id);

        if (handler == null || handler.getInteractType() == InteractableItem.InteractType.CLICK)
            return;
        Player player = event.getPlayer();

        event.setCancelled(handler.onInteract(player, event.getRightClicked(), null, stack, InteractableItem.InteractAction.CLICK));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        if (event.getClickedInventory() == event.getWhoClicked().getInventory())
            return;

        ItemStack stack = event.getCurrentItem();

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        if (nmsStack == null || nmsStack.getTag() == null || !nmsStack.getTag().hasKey("interactable"))
            return;

        int id = nmsStack.getTag().getInt("interactable");
        InteractableItem.Interact handler = InteractableItem.getInteract(id);

        if (handler == null || handler.getInteractType() == InteractableItem.InteractType.PLAYER)
            return;

        Player player = (Player) event.getWhoClicked();

        event.setCancelled(handler.onInteract(player, null, null, stack, InteractableItem.InteractAction.LEFT));
    }
}