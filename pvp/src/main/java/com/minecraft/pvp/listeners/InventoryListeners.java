/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.listeners;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.translation.Language;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.selectors.damage.Challenge;
import com.minecraft.pvp.game.selectors.damage.Editor;
import com.minecraft.pvp.game.types.Damage;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.DamageSettings;
import com.minecraft.pvp.util.Type;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryListeners implements Listener {

    private final static List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    public static void openSelector(User user, Type type) {
        Language language = user.getAccount().getLanguage();
        List<ItemStack> icons = new ArrayList<>();

        for (Kit kit : PvP.getPvP().getKitStorage().getKits()) {
            if (!user.hasKit(kit, type))
                continue;
            icons.add(new ItemFactory(kit.getIcon().getType()).setDurability(kit.getIcon().getDurability()).setName("§a" + kit.getName()).setDescription("§7" + kit.getDescription(language)).addItemFlag(ItemFlag.values()).getStack());
        }

        Kit kit = (type == Type.PRIMARY ? user.getKit1() : user.getKit2());
        ItemStack itemStack = new ItemFactory(kit.getIcon().getType()).setDurability(kit.getIcon().getDurability()).setName("§eKit selecionado §7- §a" + kit.getName()).addItemFlag(ItemFlag.values()).getStack();

        Selector.builder().withAllowedSlots(allowedSlots).withCustomItem(49, itemStack).withName(language.translate("container.select_kit.title", type == Type.PRIMARY ? 1 : 2)).withSize(54).withItems(icons).build().open(user.getPlayer());
    }

    @EventHandler
    public void onKitClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null)
            return;
        if (itemStack.getType() == Material.AIR)
            return;
        Player player = (Player) event.getWhoClicked();
        Language language = Account.fetch(player.getUniqueId()).getLanguage();
        if (event.getInventory().getName().contains("kit 1")) {
            event.setCancelled(true);
            for (Kit kit : PvP.getPvP().getKitStorage().getKits()) {
                if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§a" + kit.getName())) {
                    player.closeInventory();
                    player.performCommand("kit " + kit.getName());
                    return;
                }
            }
        } else if (event.getInventory().getName().contains("kit 2")) {
            event.setCancelled(true);
            for (Kit kit : PvP.getPvP().getKitStorage().getKits()) {
                if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§a" + kit.getName())) {
                    player.closeInventory();
                    player.performCommand("kit2 " + kit.getName());
                    return;
                }
            }
        } else if (event.getInventory().getName().equals("Editor")) {
            event.setCancelled(true);
            User user = User.fetch(player.getUniqueId());
            if (itemStack.getType() == Material.INK_SACK) {
                new Editor(user).buildDamagesType();
            } else if (itemStack.getType() == Material.WATCH) {
                new Editor(user).buildDamagesFrequency();
            } else if (itemStack.getType() == Material.WOOD_SPADE) {
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

                DamageSettings settings = user.getDamageSettings();

                settings.setDrops(nmsStack == null || !nmsStack.hasTag() || !nmsStack.getTag().hasKey("ench"));
                settings.setInChallenge(false);

                user.handleSidebar();

                new Editor(user).build();
            } else if (itemStack.getType() == Material.SKULL_ITEM) {

                DamageSettings settings = user.getDamageSettings();

                settings.setWither(itemStack.getDurability() == 0);
                settings.setInChallenge(false);

                user.handleSidebar();

                new Editor(user).build();
            }
        } else if (event.getInventory().getName().equals(language.translate("container.damages_type_title"))) {
            event.setCancelled(true);

            User user = User.fetch(player.getUniqueId());

            if (itemStack.getType() == Material.ARROW) {
                new Editor(user).build();
                return;
            }

            if (itemStack.getType() != Material.STAINED_GLASS_PANE)
                return;

            Damage.DamageType damageType = Damage.DamageType.fromDurability(itemStack.getDurability());

            DamageSettings settings = user.getDamageSettings();

            settings.setType(damageType);
            settings.setInChallenge(false);

            user.handleSidebar();

            new Editor(user).buildDamagesType();
        } else if (event.getInventory().getName().equals(language.translate("container.damages_frequency_title"))) {
            event.setCancelled(true);

            User user = User.fetch(player.getUniqueId());

            if (itemStack.getType() == Material.ARROW) {
                new Editor(user).build();
                return;
            }

            if (itemStack.getType() != Material.IRON_SWORD)
                return;

            Damage.DamageFrequency frequency = Damage.DamageFrequency.fromItemStack(itemStack);

            DamageSettings settings = user.getDamageSettings();

            settings.setFrequency(frequency);
            settings.setInChallenge(false);

            user.handleSidebar();

            new Editor(user).buildDamagesFrequency();
        } else if (event.getInventory().getName().equals(language.translate("container.challenges_title"))) {
            event.setCancelled(true);

            if (itemStack.getType() != Material.STAINED_CLAY)
                return;

            User user = User.fetch(player.getUniqueId());

            DamageSettings settings = user.getDamageSettings();

            settings.setChallenge(Damage.DamageType.fromDurability(itemStack.getDurability()));
            settings.setDrops(false);
            settings.setWither(false);
            settings.setFrequency(Damage.DamageFrequency.SECOND);

            settings.setInChallenge(true);

            user.handleSidebar();

            new Challenge(user).build();
        }
    }

}
