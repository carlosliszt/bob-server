/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.inventory;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Setter
@Getter
public class Selector implements Cloneable {

    public String name;
    public int size;
    public List<ItemStack> items;
    public BiConsumer<Player, ItemStack> onChooseItem;
    public int nextPageSlot, previousPageSlot;
    public ItemStack nextPageItem, previousPageItem;
    public HashMap<Integer, ItemStack> customItems;
    public List<Integer> allowedSlots;
    public HashMap<Integer, Inventory> pages;
    public int backSlot;
    public ItemStack backItem;
    public Consumer<Player> backConsumer;
    public Updater updater;
    public List<Player> players;

    public Selector(Builder builder) {
        name = builder.name;
        size = builder.size;
        items = builder.items;
        onChooseItem = builder.onChooseItem;
        nextPageSlot = builder.nextPageSlot;
        previousPageSlot = builder.previousPageSlot;
        nextPageItem = builder.nextPageItem;
        previousPageItem = builder.previousPageItem;
        customItems = builder.customItems;
        allowedSlots = builder.allowedSlots;
        backSlot = builder.backSlot;
        backItem = builder.backItem;
        backConsumer = builder.backConsumer;
        updater = builder.updater;
        this.pages = new HashMap<>();
        this.players = new ArrayList<>();
        build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void build() {
        if (items.isEmpty()) {
            Inventory inventory = Bukkit.createInventory(new Holder(this, 1), size, StringUtils.replace(name, "{page}", "1/1"));
            if (backSlot != -1) {
                inventory.setItem(backSlot, backItem);
            }
            customItems.forEach(inventory::setItem);
            pages.put(1, inventory);
            return;
        }
        List<List<ItemStack>> lists = getPages(items, allowedSlots.size());
        int page = 1;
        for (List<ItemStack> list : lists) {
            Inventory inventory = Bukkit.createInventory(new Holder(this, page), size, StringUtils.replace(name, "{page}", "1/" + lists.size()));
            int slot = 0;
            for (ItemStack it : list) {
                inventory.setItem(allowedSlots.get(slot), it);
                slot++;
            }
            customItems.forEach(inventory::setItem);
            inventory.setItem(previousPageSlot, editItem(previousPageItem.clone(), page - 1));

            inventory.setItem(nextPageSlot, editItem(nextPageItem.clone(), page + 1));
            if (backSlot != -1)
                inventory.setItem(backSlot, backItem);

            pages.put(page, inventory);
            page++;
        }
        pages.get(1).setItem(previousPageSlot, new ItemStack(Material.AIR));

        pages.get(pages.size()).setItem(nextPageSlot, new ItemStack(Material.AIR));

    }

    public ItemStack editItem(ItemStack item, int page) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(StringUtils.replace(item.getItemMeta().getDisplayName(), "<page>", page + ""));
        item.setItemMeta(meta);
        return item;
    }

    public <T> List<List<T>> getPages(Collection<T> c, Integer pageSize) {
        List<T> list = new ArrayList<T>(c);
        if (pageSize == null || pageSize <= 0 || pageSize > list.size())
            pageSize = list.size();
        int numPages = (int) Math.ceil((double) list.size() / (double) pageSize);
        List<List<T>> pages = new ArrayList<List<T>>(numPages);
        for (int pageNum = 0; pageNum < numPages; )
            pages.add(list.subList(pageNum * pageSize, Math.min(++pageNum * pageSize, list.size())));
        return pages;
    }

    public int getTotalPages() {
        return pages.size();
    }

    public boolean hasPage(int page) {
        return pages.containsKey(page);
    }

    public void removePlayer(Player p) {
        this.players.remove(p);
    }

    public void open(Player player) {
        open(player, 1);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public void open(Player player, int page) {
        player.openInventory(pages.get(page));
        players.add(player);
    }

    public static final class Builder {

        public final static List<Integer> ALLOWED_SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
        public String name;
        public int size;
        public List<ItemStack> items;
        public BiConsumer<Player, ItemStack> onChooseItem;
        public int nextPageSlot;
        public int previousPageSlot;
        public ItemStack nextPageItem;
        public Updater updater;
        public ItemStack previousPageItem;
        public int backSlot;
        public ItemStack backItem;
        public Consumer<Player> backConsumer;
        public HashMap<Integer, ItemStack> customItems;
        public List<Integer> allowedSlots;

        public Builder() {
            this.name = "";
            this.size = 45;
            this.items = new ArrayList<>();
            this.onChooseItem = (player, item) -> {
            };
            this.nextPageSlot = 53;
            this.previousPageSlot = 45;
            this.customItems = new HashMap<>();
            this.allowedSlots = ALLOWED_SLOTS;
            this.updater = selector -> {
            };
            this.backSlot = -1;
            this.backConsumer = player -> {
            };
            this.backItem = getBackItem();
            this.nextPageItem = getNextItem();
            this.previousPageItem = getNextItem();
        }

        public ItemStack getBackItem() {
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Voltar");
            item.setItemMeta(meta);
            return item;
        }

        public ItemStack getNextItem() {
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Página <page>");
            item.setItemMeta(meta);
            return item;
        }

        public Builder withBackItem(int backSlot, ItemStack backItem, Consumer<Player> player) {
            this.backItem = backItem;
            this.backSlot = backSlot;
            this.backConsumer = player;
            return this;
        }

        public Builder withBackItem(int backSlot, Consumer<Player> player) {
            this.backSlot = backSlot;
            this.backConsumer = player;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSize(int size) {
            this.size = size;
            return this;
        }

        public Builder withItems(List<ItemStack> items) {
            this.items = items;
            return this;
        }

        public Builder withOnChooseItem(BiConsumer<Player, ItemStack> onChooseItem) {
            this.onChooseItem = onChooseItem;
            return this;
        }

        public Builder withNextPageSlot(int nextPageSlot) {
            this.nextPageSlot = nextPageSlot;
            return this;
        }

        public Builder withPreviousPageSlot(int previousPageSlot) {
            this.previousPageSlot = previousPageSlot;
            return this;
        }

        public Builder withNextPageItem(ItemStack nextPageItem) {
            this.nextPageItem = nextPageItem;
            return this;
        }

        public Builder withPreviousPageItem(ItemStack previousPageItem) {
            this.previousPageItem = previousPageItem;
            return this;
        }

        public Builder withCustomItem(int slot, ItemStack item) {
            this.customItems.put(slot, item);
            return this;
        }

        public Builder withUpdater(Updater updater) {
            this.updater = updater;
            return this;
        }

        public Builder withAllowedSlots(List<Integer> allowedSlots) {
            this.allowedSlots = allowedSlots;
            return this;
        }

        public Selector build() {
            return new Selector(this);
        }
    }

    public static final class Holder implements InventoryHolder {

        public Selector selector;
        public int page;

        public Holder(Selector selector, int page) {
            this.selector = selector;
            this.page = page;
        }

        public int getPage() {
            return page;
        }

        public Selector getSelector() {
            return selector;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public interface Updater {
        void update(Selector selector);
    }
}