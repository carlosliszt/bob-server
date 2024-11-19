/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.tiogerson.util.enums;

import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.translation.Language;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

public enum Items {

    PORTUGUESE(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.PAPER).setName("§aJogar novamente").getStack(), execute(player -> {

        if (CooldownProvider.getGenericInstance().hasCooldown(player, "play_again"))
            return;

        User user = User.fetch(player.getUniqueId());

        if (!user.getRouteContext().hasDefinedGame()) {
            user.lobby();
        } else {
            Room room = ArcadeMain.getInstance().getRoomStorage().get();

            if (room == null) {
                player.sendMessage(user.getAccount().getLanguage().translate("arcade.room.not_found"));
                user.lobby();
                return;
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "play_again", 1, false);
            room.join(user, user.getRouteContext().getPlayMode(), true);

        }
    })), new InteractableItem(new ItemFactory(Material.COMPASS).setName("§cVoltar ao lobby").getStack(), execute(player -> User.fetch(player.getUniqueId()).lobby()))}, new Integer[]{0, 1}),
    ENGLISH(new InteractableItem[]{new InteractableItem(new ItemFactory(Material.PAPER).setName("§aPlay again").getStack(), execute(player -> {

        if (CooldownProvider.getGenericInstance().hasCooldown(player, "play_again"))
            return;

        User user = User.fetch(player.getUniqueId());

        if (!user.getRouteContext().hasDefinedGame()) {
            user.lobby();
        } else {
            Room room = ArcadeMain.getInstance().getRoomStorage().get();

            if (room == null) {
                player.sendMessage(user.getAccount().getLanguage().translate("arcade.room.not_found"));
                user.lobby();
                return;
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "play_again", 1, false);
            room.join(user, user.getRouteContext().getPlayMode(), true);
        }

    })), new InteractableItem(new ItemFactory(Material.COMPASS).setName("§cBack to lobby").getStack(), execute(player -> User.fetch(player.getUniqueId()).lobby()))}, new Integer[]{0, 1});

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