/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.worldedit;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class WorldEditProvider {

    private final Map<UUID, Position> uuidPositionMap = new HashMap<>();

    private final InteractableItem wandItem = new InteractableItem(new ItemFactory(Material.WOOD_AXE).setName("§dMachado Mágico").setUnbreakable().addItemFlag(ItemFlag.values()).getStack(), new InteractableItem.Interact() {
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            this.setInteractType(InteractableItem.InteractType.CLICK);
            if (block != null) {
                Account account = Account.fetch(player.getUniqueId());
                if (action == InteractableItem.InteractAction.LEFT) {

                    if (getFirstPosition(player) != null && getFirstPosition(player).equals(block.getLocation()))
                        return true;

                    setFirstPosition(player, block.getLocation());
                    player.sendMessage(account.getLanguage().translate("worldedit.position_set", 1, block.getX(), block.getY(), block.getZ()));
                } else {
                    if (getSecondPosition(player) != null && getSecondPosition(player).equals(block.getLocation()))
                        return true;

                    setSecondPosition(player, block.getLocation());
                    player.sendMessage(account.getLanguage().translate("worldedit.position_set", 2, block.getX(), block.getY(), block.getZ()));
                }
            }
            return true;
        }
    });

    public void addUndo(Player player, Map<Location, BlockState> map) {
        uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).addUndo(map);
    }

    public void removeUndo(Player player, Map<Location, BlockState> map) {
        uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).removeUndo(map);
    }

    public void giveWand(Player player) {
        player.getInventory().addItem(wandItem.getItemStack());
    }

    public void setFirstPosition(Player player, Location location) {
        uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).setFirstLocation(location);
    }

    public void setSecondPosition(Player player, Location location) {
        uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).setSecondLocation(location);
    }

    public boolean hasFirstPosition(Player player) {
        return uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).hasFirstLocation();
    }

    public boolean hasSecondPosition(Player player) {
        return uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).hasSecondLocation();
    }

    public boolean hasAvailableUndo(Player player) {
        return !uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getUndoList().isEmpty();
    }

    public Location getFirstPosition(Player player) {
        return uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getFirstLocation();
    }

    public Location getSecondPosition(Player player) {
        return uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getSecondLocation();
    }

    public List<Map<Location, BlockState>> getUndoList(Player player) {
        return uuidPositionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getUndoList();
    }

    @Getter
    public static class Position {

        @Setter
        private Location firstLocation;
        @Setter
        private Location secondLocation;

        private final List<Map<Location, BlockState>> undoList;

        public Position() {
            undoList = new ArrayList<>();
        }

        public void addUndo(Map<Location, BlockState> map) {
            this.undoList.add(map);
        }

        public void removeUndo(Map<Location, BlockState> map) {
            this.undoList.remove(map);
        }

        public boolean hasFirstLocation() {
            return firstLocation != null;
        }

        public boolean hasSecondLocation() {
            return firstLocation != null;
        }
    }

}