/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.list.Nenhum;
import com.minecraft.hungergames.user.kits.list.Surprise;
import com.minecraft.hungergames.user.kits.pattern.CooldownType;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.game.GameStage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public abstract class Kit extends DynamicListener implements Assistance, VariableStorage, BukkitInterface {

    @Getter(AccessLevel.NONE)
    protected final transient String COOLDOWN, COMBAT;

    @Getter(AccessLevel.NONE)
    protected final String PORTUGUESE_DESCRIPTION, ENGLISH_DESCRIPTION; // Caching kit description.

    private final HungerGames hungerGames; // Hunger Games instance

    private static final CooldownProvider cooldownProvider = CooldownProvider.getGenericInstance();

    private final String name;
    private String displayName;
    @Setter(AccessLevel.NONE)
    private boolean active;
    private Pattern icon;
    private ItemStack[] items;
    private Rank permission;
    private double cooldown;
    private int price;
    private KitCategory kitCategory;
    private long releasedAt;

    public Kit(HungerGames hungerGames) {
        this.hungerGames = hungerGames;
        this.active = true;
        this.permission = Rank.PRO;
        this.name = getClass().getSimpleName();
        this.displayName = getClass().getSimpleName();
        this.COOLDOWN = "kit.cooldown." + getName().toLowerCase();
        this.COMBAT = "kit.combat." + getName().toLowerCase();
        this.price = 20000;
        this.kitCategory = KitCategory.NONE;
        this.releasedAt = -1;
        this.PORTUGUESE_DESCRIPTION = Language.PORTUGUESE.translate("kit." + getName().toLowerCase() + ".description");
        this.ENGLISH_DESCRIPTION = Language.ENGLISH.translate("kit." + getName().toLowerCase() + ".description");
    }

    public boolean isUser(Player player) {
        User user = User.fetch(player.getUniqueId());
        return isUser(user);
    }

    public boolean isUser(User user) {
        return user.getKitList().stream().anyMatch(c -> c == this);
    }

    public boolean isItem(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("kit." + getName().toLowerCase());
    }

    public boolean isItem(ItemStack itemStack, Material material) {
        if (itemStack == null)
            return false;
        if (itemStack.getType() != material)
            return false;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("kit." + getName().toLowerCase());
    }

    public void grant(Player player) {

        if (!isRegistered())
            register();

        if (items == null)
            return;
        for (ItemStack items : items) {
            ItemStack itemStack = addTag(items.clone(), player.getUniqueId().toString());

            if (player.getInventory().firstEmpty() != -1)
                player.getInventory().addItem(itemStack);
            else {
                player.sendMessage("§cO seu inventário está cheio, o item do seu kit foi dropado no chão!");
                Item droppedItem = player.getWorld().dropItem(player.getLocation().clone().add(0, 0.1, 0), itemStack);
                if (droppedItem.getItemStack().hasItemMeta() && droppedItem.getItemStack().getItemMeta().hasDisplayName()) {
                    droppedItem.setCustomName("§e§l" + getName().toUpperCase());
                    droppedItem.setCustomNameVisible(true);
                }
            }
        }
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

    public void addCooldown(UUID uuid, CooldownType cooldownType, double duration) {
        if (getHungerGames().getKitStorage().isCooldown())
            cooldownProvider.addCooldown(uuid, cooldownType.getWord() + getDisplayName(), cooldownType == CooldownType.DEFAULT ? COOLDOWN : COMBAT, duration, true);
    }

    public void addCooldown(UUID uuid) {
        if (getHungerGames().getKitStorage().isCooldown())
            cooldownProvider.addCooldown(uuid, CooldownType.DEFAULT.getWord() + getDisplayName(), COOLDOWN, getCooldown(), true);
    }

    public void removeItems(Player player) {
        String KEY = "kit." + getName().toLowerCase();
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR)
                continue;
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

            if (nmsStack == null || nmsStack.getTag() == null)
                continue;

            if (nmsStack.getTag().hasKey(KEY))
                player.getInventory().remove(itemStack);
        }
    }

    public void setActive(boolean bool, boolean unregister) {
        if (isActive() == bool)
            return;

        this.active = bool;

        boolean started = hasStarted();

        if (unregister && !bool) {

            unregister(); // Unregistering listeners.

            Kit defaultKit = getPlugin().getKitStorage().getKit("Nenhum");
            getHungerGames().getUserStorage().getUsers().stream().filter(this::isUser).forEach(user -> {

                Kit[] kits = user.getKits();

                for (int i = 0; i < kits.length; i++) {
                    if (kits[i] == this)
                        user.setKit(i, defaultKit);
                }

                if (started)
                    removeItems(user.getPlayer());
            });
        }
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public boolean isNone() {
        return this instanceof Nenhum;
    }

    public void setItems(ItemStack... itemstacks) {
        for (int i = 0; i < itemstacks.length; i++) {
            ItemStack item = addTag(itemstacks[i], "kit." + getName().toLowerCase(), "kit", "undroppable");
            itemstacks[i] = item;
        }
        this.items = itemstacks;
    }

    public boolean checkInvincibility(Player player) {
        if (getPlugin().getGame().getStage() == GameStage.INVINCIBILITY) {
            player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("hg.game.invincibility.cant_use_kit", getDisplayName()));
            return true;
        }
        return false;
    }

    public boolean isMultipleChoices() {
        return this instanceof Nenhum || this instanceof Surprise;
    }

    public boolean isUnique() {
        return this.releasedAt != -1;
    }

    public String getDescrition(Language language) {
        return language == Language.PORTUGUESE ? PORTUGUESE_DESCRIPTION : ENGLISH_DESCRIPTION;
    }

    public void appreciate(LivingUserDieEvent event) {
    }

    private boolean combatCooldown = false;
    @Setter(AccessLevel.NONE)
    private double combatTime = 0;

    public void setCombatCooldown(boolean b, long t) {
        this.combatCooldown = b;
        this.combatTime = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return Objects.equals(name, kit.name);
    }
}
