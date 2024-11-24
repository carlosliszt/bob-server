/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.selector;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.selector.object.Chooser;
import com.minecraft.hungergames.util.selector.object.Comemorations;
import com.minecraft.hungergames.util.selector.object.Store;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public enum Items implements Assistance {

    STARTING() {
        @Override
        public void apply(User user) {

            Player player = user.getPlayer();
            Inventory inventory = player.getInventory();
            Account account = user.getAccount();
            Language language = account.getLanguage();

            inventory.clear();

            int slot = 0;

            for (int i = 0; i < user.getKits().length && slot < 5; i++) {

                final int ovo1 = i;

                InteractableItem ovo = new InteractableItem(new ItemFactory(Material.CHEST).setName(language.translate("items.select_kit.name", i + 1)).getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new Chooser(user, Chooser.Sort.MINE_ALL, ovo1, ovo1 == 0 ? "kit" : "kit" + (ovo1 + 1)).build().open();
                        return true;
                    }
                });

                inventory.setItem(slot, ovo.getItemStack());
                if (i != (user.getKits().length - 1))
                    slot++;
            }

            slot++;
            inventory.setItem(slot, new InteractableItem(new ItemFactory(Material.EMERALD).setName(language.translate("items.kit_store.name")).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    new Store(user).build().open();
                    return true;
                }
            }).getItemStack());

            slot += 3;

            if (inventory.getItem(4) == null || inventory.getItem(4).getType() == Material.AIR)
                slot--;

            inventory.setItem(slot, new ItemFactory(Material.STORAGE_MINECART).addEnchantment(Enchantment.SILK_TOUCH, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).setName(language.translate("items.daily_kit.name")).getStack());
            slot++;
            inventory.setItem(slot, new InteractableItem(new ItemFactory(Material.CAKE).setName(language.translate("items.select_dance.name")).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    new Comemorations(user).build().open();
                    return true;
                }
            }).getItemStack());
        }
    },

    SPECTATOR() {
        @Override
        public void apply(User user) {

            Player player = user.getPlayer();
            Inventory inventory = player.getInventory();
            Account account = user.getAccount();
            Language language = account.getLanguage();

            inventory.clear();

            inventory.setItem(0, new InteractableItem(new ItemFactory(Material.COMPASS).setName(language.translate("items.teleporter.name")).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    HungerGames.getInstance().getSpectatorList().getInventory().open(player);
                    return true;
                }
            }).getItemStack());

            inventory.setItem(8, new InteractableItem(new ItemFactory(Material.PAPER).setName(language.translate("items.play_again.name")).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {

                    ServerType serverType = Constants.getServerType();

                    Server server = serverType.getServerCategory().getServerFinder().getBestServer(serverType);

                    if (server == null)
                        server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.HG_LOBBY);

                    if (server != null) {
                        Account.fetch(player.getUniqueId()).connect(server);
                    }

                    return true;
                }
            }).getItemStack());
        }
    };

    public abstract void apply(User user);
}