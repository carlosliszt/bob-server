/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.util.selector;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.kit.Kit;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.util.Assistance;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.translation.Language;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Chooser implements BukkitInterface, Assistance {

    private User owner;
    private Sort sort;
    private int slot;
    private Selector selector;
    private String command;
    private long nextSortTime;

    public final static List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    public Chooser(User user, Sort sort, int slot, String command) {
        this.owner = user;
        this.sort = sort;
        this.slot = slot;
        this.command = command;
    }

    public Chooser build() {
        List<ItemStack> itemstacks = new ArrayList<>();

        User user = this.owner;
        Language language = user.getAccount().getLanguage();

        Kit selected = user.getKit(slot);

        ItemStack selectedIcon = new ItemFactory(selected.getIcon().getMaterial()).setDurability(selected.getIcon().getData()).setName("§aKit Selecionado - §f" + selected.getDisplayName()).addItemFlag(ItemFlag.values()).getStack();

        InteractableItem sortItem = new InteractableItem(new ItemFactory(Material.SLIME_BALL).setName(language == Language.PORTUGUESE ? "§aOrdenar por" : "§aOrder by").setDescription("§f" + sort.getDisplay(language)).getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {

                if (getNextSortTime() > System.currentTimeMillis())
                    return true;

                setNextSortTime(System.currentTimeMillis() + 200);
                setSort(getSort().next());
                build().open();
                player.playSound(player.getLocation(), Sound.CLICK, 3F, 3F);
                return true;
            }
        });

        getSort().getShorter().getKits(this).forEach(kit -> {

            boolean has = owner.hasKit(kit, slot);

            if (!kit.isActive())
                return;

            if (kit.isNone())
                return;

            String description = kit.getDescription(user.getAccount().getLanguage());

            InteractableItem item = new InteractableItem(new ItemFactory(has ? kit.getIcon().getMaterial() : Material.STAINED_GLASS_PANE).setDescription((has ? description + (kit.isUnique() ? "\n\n§7" + (language == Language.ENGLISH ? "Released at" : "Lançado em") + ": " + language.getDayFormat().format(kit.getReleasedAt()) : "") + "\n\n" + (language == Language.ENGLISH ? "§eClick to select." : "§eClique para selecionar.") : (language == Language.PORTUGUESE ? "\n§cVocê não possui este kit.\n§cAdquira-o agora em §e" + Constants.SERVER_WEBSITE + "\n\n" + description : "\n§cYou do not have this kit.\n§cBuy it now at §e" + Constants.SERVER_WEBSITE + "\n\n" + description))).setName((has ? "§a" : "§c") + kit.getDisplayName()).setDurability(has ? kit.getIcon().getData() : 14).addItemFlag(ItemFlag.values()).getStack(), new InteractableItem.Interact() {
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, command + ' ' + kit.getName().toLowerCase());
                    return true;
                }
            });

            ItemStack itemStack = item.getItemStack();

            if (kit.getIcon().isGlow() && has)
                itemStack = addTag(itemStack, "ench");

            itemstacks.add(itemStack);
        });

        this.selector = Selector.builder().withAllowedSlots(allowedSlots).withCustomItem(49, selectedIcon).withCustomItem(50, sortItem.getItemStack()).withName(language.translate("container.select_kit.title", (slot + 1))).withSize(54).withItems(itemstacks).build();
        return this;
    }

    public Chooser open() {
        getSelector().open(getOwner().getPlayer());
        return this;
    }

    @AllArgsConstructor
    public enum Sort {

        ASCENDING("A-Z", "A-Z", (chooser) -> {
            return PvP.getInstance().getKitStorage().getKits();
        }),
        DESCENDING("Z-A", "Z-A", (chooser) -> {
            List<Kit> kits = new ArrayList<>(PvP.getInstance().getKitStorage().getKits());
            kits.sort((a, b) -> b.getName().compareTo(a.getName()));
            return kits;
        }),
        MINE_ALL("Meus-Todos", "Mine-All", (chooser) -> {
            List<Kit> kits = new ArrayList<>(PvP.getInstance().getKitStorage().getKits());
            User user = chooser.getOwner();
            kits.sort((a, b) -> Boolean.compare(user.hasKit(b, chooser.getSlot()), user.hasKit(a, chooser.getSlot())));
            return kits;
        }),
        ALL_MINE("Todos-Meus", "All-Mine", (chooser) -> {
            User user = chooser.getOwner();
            List<Kit> kits = new ArrayList<>(PvP.getInstance().getKitStorage().getKits());
            kits.sort((a, b) -> Boolean.compare(user.hasKit(a, chooser.getSlot()), user.hasKit(b, chooser.getSlot())));
            return kits;
        });

        private final String br, us;
        @Getter
        private final Shorter shorter;

        public String getDisplay(Language language) {
            return language == Language.PORTUGUESE ? br : us;
        }

        public interface Shorter {

            List<Kit> getKits(Chooser chooser);

        }

        public Sort next() {
            return (ordinal() < (values().length - 1) ? values()[ordinal() + 1] : ASCENDING);
        }
    }
}