/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hub.util;

import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.translation.Language;
import com.minecraft.hub.Hub;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Items {

    PORTUGUESE(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.COMPASS).setName("§aSelecionar jogo").getStack(), execute(player -> Hub.getInstance().getGameSelector().getInventory().open(player))), new InteractableItem(new ItemFactory(Material.CHEST).setName("§aColecionáveis").getStack(), execute(player -> player.sendMessage("§7Colecione."))), new InteractableItem(new ItemFactory(Material.NETHER_STAR).setName("§aSelecionar lobby").getStack(), execute(player -> Hub.getInstance().getLobbySelector().getInventory().open(player)))}, new Integer[]{0, 4, 8}),
    ENGLISH(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.COMPASS).setName("§aSelect a game").getStack(), execute(player -> Hub.getInstance().getGameSelector().getInventory().open(player))), new InteractableItem(new ItemFactory(Material.CHEST).setName("§aCollectibles").getStack(), execute(player -> player.sendMessage("§7Colecione."))), new InteractableItem(new ItemFactory(Material.NETHER_STAR).setName("§aSelect a lobby").getStack(), execute(player -> Hub.getInstance().getLobbySelector().getInventory().open(player)))}, new Integer[]{0, 4, 8});

    private final InteractableItem[] items;
    private final Integer[] slots;

    Items(InteractableItem[] items, Integer[] slots) {
        this.items = items;
        this.slots = slots;
    }

    @Getter
    private static final Items[] values;

    static {
        values = values();
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
        return Arrays.stream(getValues()).filter(c -> c.name().equalsIgnoreCase(language.name())).findFirst().orElse(null);
    }

}