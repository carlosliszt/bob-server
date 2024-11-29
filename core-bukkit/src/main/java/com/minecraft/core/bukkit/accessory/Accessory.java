package com.minecraft.core.bukkit.accessory;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @Setter
public abstract class Accessory implements Listener {
    private final String uniqueCode;
    private final String name;
    private final String description;
    private final String permission;
    private final Rank rank;
    private final ItemStack itemStack;
    private final boolean persistent;
    private final AccessoryType type;
    private final AccessoryRarity rarity;
    private final Columns accessoryData;

    public Accessory(String uniqueCode, String name, String permission, Rank rank, ItemStack itemStack, String description, boolean persistent, AccessoryType type, AccessoryRarity rarity) {
        this.uniqueCode = uniqueCode;
        this.name = name;
        this.permission = permission;
        this.rank = rank;
        this.itemStack = itemStack;
        this.description = description;
        this.persistent = persistent;
        this.type = type;
        this.rarity = rarity;
        this.accessoryData = Columns.ACCESSORY;
    }

    public abstract void updateAsync(Player player, ServerHeartbeatEvent event);
    public abstract void give(Player player);
    public abstract void remove(Player player);

    public String getUniqueCode() {
        return uniqueCode;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public Rank getRank() {
        return rank;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public AccessoryType getType() {
        return type;
    }

    public AccessoryRarity getRarity() {
        return rarity;
    }

    public Accessory getActiveAccessory(UUID uniqueId) {
        return BukkitGame.getEngine().getAccessoryStorage().getAccessoryList().get(Account.fetch(uniqueId).getData(accessoryData).getAsString().toLowerCase());
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
        System.out.println("[DEBUG] Loaded '" + this.getName() + "' acessory!");
    }

    public boolean hasActiveAccessory(UUID uniqueId) {
        Accessory activeAccessory = BukkitGame.getEngine().getAccessoryStorage().getAccessoryList().get(Account.fetch(uniqueId).getData(accessoryData).getAsString());
        return activeAccessory != null && activeAccessory.equals(this);
    }

    public void setPlayerAccessory(UUID uniqueId, Accessory accessory) {
        Account.fetch(uniqueId).getData(accessoryData).setData(accessory.getName().toLowerCase());
    }
}