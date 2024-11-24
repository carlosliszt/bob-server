/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.PreferencesInventory;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileCommand implements BukkitInterface {

    @Command(name = "profile", aliases = {"perfil"}, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {
        async(() -> {
            context.getAccount().getDataStorage().loadIfUnloaded(Columns.FIRST_LOGIN, Columns.LAST_LOGIN);
            sync(() -> new ProfileMenu(context.getSender(), context.getAccount()).open());
        });
    }

    public static class ProfileMenu {

        private final Player player;
        private final Account account;

        public ProfileMenu(Player player, Account account) {
            this.player = player;
            this.account = account;
        }

        final Inventory inventory = Bukkit.createInventory(null, 45, "Perfil");
        final String LANG_URL = "2e2cc42015e6678f8fd49ccc01fbf787f1ba2c32bcf559a015332fc5db50";

        public void open() {
            Tag tag = this.account.getTagList().getHighestTag();
            DateFormat dateFormat = this.account.getLanguage().getDateFormat();

            List<String> head_description = new ArrayList<>();

            head_description.add("§7Rank: " + tag.getColor() + tag.getName());
            head_description.add("§7Primeiro login: " + dateFormat.format(this.account.getData(Columns.FIRST_LOGIN).getAsLong()));
            head_description.add("§7Último login: " + dateFormat.format(this.account.getData(Columns.LAST_LOGIN).getAsLong()));

            InteractableItem head = new InteractableItem(new ItemFactory().setSkull(this.player.getName()).setName("§a" + this.account.getUsername()).setDescription(head_description).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    return true;
                }
            });

            InteractableItem lang = new InteractableItem(new ItemFactory().setSkullURL(LANG_URL).setName("§aSelecionar idioma").setDescription("§7Altere seu idioma.").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    new LanguageCommand.LangMenu(player, account, true).open();
                    return true;
                }
            });

            InteractableItem prefs = new InteractableItem(new ItemFactory(Material.REDSTONE_COMPARATOR).setName("§aPreferências").setDescription("§7Altere suas preferências.").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    new PreferencesInventory(player, account, true).openInventory();
                    return true;
                }
            });

            this.inventory.setItem(13, head.getItemStack());
            this.inventory.setItem(31, lang.getItemStack());
            this.inventory.setItem(32, prefs.getItemStack());

            this.player.openInventory(this.inventory);
        }

    }
}