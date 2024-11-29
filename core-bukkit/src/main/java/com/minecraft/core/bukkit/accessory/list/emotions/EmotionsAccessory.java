package com.minecraft.core.bukkit.accessory.list.emotions;

import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EmotionsAccessory extends Accessory {

    public EmotionsAccessory(String uniqueCode, String name, String permission, Rank rank, ItemStack itemStack, String description, boolean persistent, AccessoryType type, AccessoryRarity rarity) {
        super(uniqueCode, name, permission, rank, itemStack, description, persistent, type, rarity);
    }

    @Override
    public void updateAsync(Player player, ServerHeartbeatEvent event) {

    }

    @Override
    public void give(Player player) {
        player.getInventory().setHelmet(getItemStack());
    }

    @Override
    public void remove(Player player) {
        player.getInventory().setHelmet(null);
    }
}