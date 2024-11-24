/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateLanguageEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.translation.Language;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageCommand implements BukkitInterface, Listener {

    @Command(name = "language", aliases = {"lang", "idioma"}, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {
        String[] args = context.getArgs();

        Player player = context.getSender();
        Account account = context.getAccount();

        if (args.length == 0) {
            new LangMenu(player, account, false).open();
            return;
        }

        Language language = Language.fromString(args[0]);

        if (language == null) {
            context.info("command.language.not_found");
            return;
        }

        if (account.getLanguage().equals(language)) {
            context.info("command.language.already_selected");
            return;
        }

        if (CooldownProvider.getGenericInstance().hasCooldown(player, "command.language")) {
            Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(player.getUniqueId(), "command.language");
            if (cooldown != null && !cooldown.expired()) {
                context.info("wait_generic", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining()));
                return;
            }
        }

        account.setLanguage(language);
        context.info("command.language.execution_successful", language.getName());

        PlayerUpdateLanguageEvent playerUpdateLanguageEvent = new PlayerUpdateLanguageEvent(account, language);
        Bukkit.getPluginManager().callEvent(playerUpdateLanguageEvent);
        CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "command.language", 3, false);

        account.getData(Columns.LANGUAGE).setData(language.getUniqueCode());

        async(() -> {
            account.getDataStorage().saveColumn(Columns.LANGUAGE);
            Constants.getRedis().publish(Redis.LANGUAGE_UPDATE_CHANNEL, account.getUniqueId() + ":" + language.getUniqueCode());
        });
    }

    @Completer(name = "language")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return Arrays.stream(Language.values()).filter(l -> startsWith(l.getName(), context.getArg(0))).map(Language::getLower).collect(Collectors.toList());
        return Collections.emptyList();
    }

    public static class LangMenu implements BukkitInterface {

        private final Player player;
        private final Account account;
        private final boolean fromProfileMenu;

        public LangMenu(Player player, Account account, boolean fromProfileMenu) {
            this.player = player;
            this.account = account;
            this.fromProfileMenu = fromProfileMenu;
        }

        public void open() {
            Language language = account.getLanguage();
            Inventory inventory = Bukkit.createInventory(null, 27, language == Language.PORTUGUESE ? "Selecionar idioma" : "Choose language");

            List<String> pt_description = new ArrayList<>();
            pt_description.add("");
            pt_description.add("§7Altere seu idioma para Português.");
            pt_description.add("");
            pt_description.add("§eClique para alterar seu idioma!");

            List<String> en_description = new ArrayList<>();
            en_description.add("");
            en_description.add("§7Change your language to English.");
            en_description.add("");
            en_description.add("§eClick to change your language!");

            InteractableItem portuguese = new InteractableItem(new ItemFactory().setType((language == Language.PORTUGUESE ? Material.ENCHANTED_BOOK : Material.BOOK)).setName("§aPortuguês").setDescription(pt_description).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    if (account.getLanguage() == Language.ENGLISH && !CooldownProvider.getGenericInstance().hasCooldown(player, "command.language")) {
                        player.chat("/language português");
                        open();
                    }
                    return true;
                }
            });

            InteractableItem english = new InteractableItem(new ItemFactory().setType((language == Language.ENGLISH ? Material.ENCHANTED_BOOK : Material.BOOK)).setName("§aEnglish").setDescription(en_description).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    if (account.getLanguage() == Language.PORTUGUESE && !CooldownProvider.getGenericInstance().hasCooldown(player, "command.language")) {
                        player.chat("/language english");
                        open();
                    }
                    return true;
                }
            });

            if (fromProfileMenu) {
                InteractableItem profile = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("Para Perfil").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new ProfileCommand.ProfileMenu(player, account).open();
                        return true;
                    }
                });
                inventory.setItem(22, profile.getItemStack());
            }

            inventory.setItem(10, portuguese.getItemStack());
            inventory.setItem(11, english.getItemStack());

            player.openInventory(inventory);
        }

    }

}