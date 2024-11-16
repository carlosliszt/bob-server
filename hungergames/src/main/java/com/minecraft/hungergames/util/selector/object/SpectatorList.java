/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.selector.object;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.selector.ClickableItem;
import com.minecraft.core.bukkit.util.selector.SmartInventory;
import com.minecraft.core.bukkit.util.selector.content.InventoryContents;
import com.minecraft.core.bukkit.util.selector.content.InventoryProvider;
import com.minecraft.core.bukkit.util.selector.content.Pagination;
import com.minecraft.core.bukkit.util.selector.content.SlotIterator;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.constructor.Assistance;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
public class SpectatorList implements BukkitInterface, Assistance, Listener, InventoryProvider {

    private final SmartInventory inventory = SmartInventory.builder()
            .id("spectator_list")
            .provider(this)
            .size(5, 9)
            .title("hg.teleporter")
            .closeable(true)
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {
        execute(player, contents);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        execute(player, contents);
    }

    public void execute(Player player, InventoryContents inventoryContents) {

        User user = User.fetch(player.getUniqueId());

        if (user.isAlive()) {
            player.sendMessage("§cVocê não pode estar vivo para usar este recurso.");
            return;
        }

        Account account = user.getAccount();

        if (!account.hasPermission(Rank.VIP)) {
            player.sendMessage("§cVocê não tem acesso a este recurso. Adquira VIP para espectar partidas.");
            return;
        }

        boolean showInfo = account.getRank().isStaffer();

        List<User> players = getPlugin().getUserStorage().getAliveUsers();

        Pagination pagination = inventoryContents.pagination();
        ClickableItem[] items = new ClickableItem[players.size()];

        for (int i = 0; i < players.size(); i++) {
            User target = players.get(i);
            items[i] = ClickableItem.of(createHead(target, showInfo), clickEvent -> clickEvent.getWhoClicked().teleport(target.getPlayer()));
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(21);

        if (!pagination.isFirst())
            inventoryContents.set(4, 0, ClickableItem.of(new ItemFactory(Material.ARROW).setName("§aPágina " + (pagination.getPage())).getStack(),
                    e -> inventory.open(player, pagination.previous().getPage(), null)));

        if (!pagination.isLast())
            inventoryContents.set(4, 8, ClickableItem.of(new ItemFactory(Material.ARROW).setName("§aPágina " + (pagination.getPage() + 2)).getStack(),
                    e -> inventory.open(player, pagination.next().getPage(), null)));

        SlotIterator slotIterator = inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1);
        slotIterator.blacklistBorders();

        pagination.addToIterator(slotIterator);
    }

    private ItemStack createHead(User user, boolean showInfo) {

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        Tag tag = user.getAccount().getProperty("account_tag").getAs(Tag.class);
        meta.setDisplayName(tag.getFormattedColor() + user.getName());

        List<String> lore = new ArrayList<>();

        if (showInfo) {

            Iterator<Kit> iterator = user.getKitList().stream().filter(kit -> !kit.isNone()).iterator();
            int kitCount = 0;

            while (iterator.hasNext()) {

                Kit kit = iterator.next();

                if (iterator.hasNext() || kitCount != 0) {
                    kitCount++;
                    lore.add("§aKit " + kitCount + ": §f" + kit.getDisplayName());
                } else
                    lore.add("§aKit: §f" + kit.getDisplayName());
            }

            lore.add("§aKills: §f" + user.getKills());
        }

        lore.add("§eClique para se teleportar.");
        meta.setLore(lore);
        skull.setItemMeta(meta);
        meta.setOwner(user.getName());
        skull.setItemMeta(meta);
        return skull;
    }

}
