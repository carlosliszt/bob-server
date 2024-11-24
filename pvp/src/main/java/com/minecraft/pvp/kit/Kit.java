/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@lombok.Data
public abstract class Kit implements Listener, BukkitInterface {

    private final String name;

    public KitCategory category;

    private int price;

    private ItemStack[] items;
    private ItemStack icon;

    private final String COOLDOWN, COMBAT;
    private final String PORTUGUESE_DESCRIPTION, ENGLISH_DESCRIPTION;

    private int cooldown;
    private int limit;

    private boolean active;
    private Rank defaultRank;

    private final PvP plugin = PvP.getPvP();
    private static final CooldownProvider cooldownProvider = CooldownProvider.getGenericInstance();

    public Kit() {
        this.name = getClass().getSimpleName();
        this.category = KitCategory.NONE;

        this.price = -1;
        this.limit = -1;
        this.cooldown = -1;

        this.active = true;
        this.defaultRank = Rank.PRO;

        this.icon = new ItemStack(Material.BARRIER);

        String kit_key = this.name.toLowerCase();

        this.COOLDOWN = "kit.cooldown." + kit_key;
        this.COMBAT = "kit.combat." + kit_key;

        this.PORTUGUESE_DESCRIPTION = Language.PORTUGUESE.translate("kit." + kit_key + ".description");
        this.ENGLISH_DESCRIPTION = Language.ENGLISH.translate("kit." + kit_key + ".description");
    }

    public boolean isItem(Player player) {
        return player.getItemInHand().getType() == getIcon().getType();
    }

    public boolean isUser(Player player) {
        User user = User.fetch(player.getUniqueId());
        return !user.isKept() && user.getKit1().equals(this) || user.getKit2().equals(this);
    }

    public boolean isUser(User user) {
        return !user.isKept() && user.getKit1().equals(this) || user.getKit2().equals(this);
    }

    public String getDescription(Language language) {
        return language == Language.PORTUGUESE ? PORTUGUESE_DESCRIPTION : ENGLISH_DESCRIPTION;
    }

    public void dispatchCooldown(Player player) {
        Cooldown cooldown = cooldownProvider.getCooldown(player.getUniqueId(), COOLDOWN);
        if (cooldown == null)
            cooldown = cooldownProvider.getCooldown(player.getUniqueId(), COMBAT);
        if (!cooldown.expired())
            player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("wait_to_use_kit", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
    }

    public boolean isCooldown(Player player) {
        Cooldown cooldown = cooldownProvider.getCooldown(player.getUniqueId(), COOLDOWN);
        return cooldown != null && !cooldown.expired();
    }

    public boolean isCombat(Player player) {
        Cooldown cooldown = cooldownProvider.getCooldown(player.getUniqueId(), COMBAT);
        return cooldown != null && !cooldown.expired();
    }

    public void addCooldown(UUID uuid, CooldownType cooldownType, long duration) {
        cooldownProvider.addCooldown(uuid, cooldownType.getWord() + getName(), cooldownType == CooldownType.DEFAULT ? COOLDOWN : COMBAT, duration, true);
    }

    public void setItems(ItemStack... itemStacks) {
        this.items = itemStacks;
    }

    public boolean hasCooldown() {
        return cooldown != -1;
    }

    public boolean hasLimit() {
        return limit != -1;
    }

    public boolean isBuyable() {
        return price != -1;
    }

    public abstract void resetAttributes(User user);

    public boolean isNone() {
        return this == PvP.getPvP().getKitStorage().getKits().get(0);
    }

    private boolean combatCooldown = false;
    @Setter(AccessLevel.NONE)
    private long combatTime = 0;

    public void setCombatCooldown(boolean b, long t) {
        this.combatCooldown = b;
        this.combatTime = t;
    }

    @AllArgsConstructor
    @Getter
    public enum CooldownType {

        COMBAT("Combate: ", true), DEFAULT("Kit ", true);

        private final String word;
        private final boolean display;
    }

}