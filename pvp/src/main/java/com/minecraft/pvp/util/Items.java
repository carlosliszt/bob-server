/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.util;

import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.translation.Language;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.game.selectors.arena.Store;
import com.minecraft.pvp.listeners.InventoryListeners;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Items {

    PORTUGUESE(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.CHEST).setName("§aSelecionar kit 1").getStack(), execute(player -> InventoryListeners.openSelector(User.fetch(player.getUniqueId()), Type.PRIMARY))), new InteractableItem(new ItemFactory(Material.CHEST).setName("§aSelecionar kit 2").getStack(), execute(player -> InventoryListeners.openSelector(User.fetch(player.getUniqueId()), Type.SECONDARY))), new InteractableItem(new ItemFactory(Material.EMERALD).setName("§aComprar kits").getStack(), execute(player -> new Store(User.fetch(player.getUniqueId())).build().open()))}, new Integer[]{0, 1, 4}),
    ENGLISH(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.CHEST).setName("§aSelect kit 1").getStack(), execute(player -> InventoryListeners.openSelector(User.fetch(player.getUniqueId()), Type.PRIMARY))), new InteractableItem(new ItemFactory(Material.CHEST).setName("§aSelect kit 2").getStack(), execute(player -> InventoryListeners.openSelector(User.fetch(player.getUniqueId()), Type.SECONDARY))), new InteractableItem(new ItemFactory(Material.EMERALD).setName("§aBuy kits").getStack(), execute(player -> new Store(User.fetch(player.getUniqueId())).build().open()))}, new Integer[]{0, 1, 4}),
    COMPASS(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.COMPASS).getStack(), execute(player -> searchPlayer(User.fetch(player.getUniqueId()))))}, new Integer[]{8});

    private final InteractableItem[] items;
    private final Integer[] slots;

    Items(InteractableItem[] items, Integer[] slots) {
        this.items = items;
        this.slots = slots;
    }

    public InteractableItem getItem(int id) {
        return id <= items.length - 1 ? items[id] : items[0];
    }

    private static InteractableItem.Interact execute(Consumer<Player> playerConsumer) {
        return new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                playerConsumer.accept(player);
                return true;
            }
        };
    }

    public void build(Player player) {
        if (slots != null) {
            int id = 0;
            for (Integer slot : slots) {
                player.getInventory().setItem(slot, getItem(id).getItemStack());
                id++;
            }
        } else {
            for (int i = 0; i < items.length; i++) {
                player.getInventory().addItem(getItem(i).getItemStack());
            }
        }
    }

    public static Items find(Language language) {
        if (language.equals(Language.MEOW)) {
            return PORTUGUESE;
        }
        return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(language.name())).findFirst().orElse(null);
    }

    public static void searchPlayer(User user) {
        Player comparator = null;
        Player consumer = user.getPlayer();
        Game game = user.getGame();

        for (User other : game.getPlayingUsers()) {
            if (other.getUniqueId().equals(user.getUniqueId()) || other.isKept())
                continue;
            Player player = other.getPlayer();
            if (player.getLocation().distanceSquared(consumer.getLocation()) >= 225) {
                if (comparator == null || comparator.getLocation().distance(consumer.getLocation()) > player.getLocation().distance(consumer.getLocation())) {
                    comparator = player;
                }
            }
        }

        if (comparator == null) {
            consumer.sendMessage("§cNenhum jogador encontrado.");
            consumer.setCompassTarget(game.getWorld().getSpawnLocation());
        } else {
            consumer.setCompassTarget(comparator.getLocation());
            consumer.sendMessage(user.getAccount().getLanguage().translate("hg.game.user.compass_pointing_to", comparator.getName()));
        }
    }

}