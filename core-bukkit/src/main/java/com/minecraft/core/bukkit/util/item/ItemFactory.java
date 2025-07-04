package com.minecraft.core.bukkit.util.item;

import com.minecraft.core.bukkit.util.reflection.FieldHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemFactory {

    private ItemStack itemStack;

    public ItemFactory(Material material) {
        itemStack = new ItemStack(material);
    }

    public ItemFactory(ItemStack stack) {
        itemStack = stack;
    }

    public ItemFactory() {
    }

    public ItemFactory setItemStack(ItemStack itemStack1) {
        itemStack = itemStack1;
        return this;
    }

    public ItemFactory setMaterial(Material type) {
        itemStack = new ItemStack(type);
        return this;
    }

    public ItemFactory setFast(Material type, String name, int data) {
        setMaterial(type);
        setName(name);
        setDurability(data);
        return this;
    }

    public ItemFactory setFast(Material type, String name) {
        setMaterial(type);
        setName(name);
        return this;
    }

    public ItemFactory setType(Material type) {
        setMaterial(type);
        return this;
    }

    public ItemFactory setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemFactory setDurability(int durability) {
        itemStack.setDurability((short) durability);
        return this;
    }

    public ItemFactory addItemFlag(ItemFlag... flag) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flag);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setDescription(List<String> desc) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(desc);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setDescription(String... desc) {
        setDescription(Arrays.asList(desc));
        return this;
    }

    public ItemFactory setDescription(String text) {
        List<String> lore = getFormattedLore(25, text);
        setDescription(lore.toArray(new String[]{}));
        return this;
    }

    public ItemFactory setDescription(int limit, String text) {
        List<String> lore = getFormattedLore(limit, text);
        setDescription(lore.toArray(new String[]{}));
        return this;
    }

    public ItemFactory addEnchantment(Enchantment[] enchant, int[] level) {
        for (int i = 0; i < enchant.length; ++i) {
            itemStack.addUnsafeEnchantment(enchant[i], level[i]);
        }
        return this;
    }

    public ItemFactory addEnchantment(Enchantment enchant, int level) {
        itemStack.addUnsafeEnchantment(enchant, level);
        return this;
    }

    public ItemFactory addStoredEnchantment(Enchantment enchant, int level) {
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
        meta.addStoredEnchant(enchant, level, true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory removePotionEffects() {
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        for (PotionEffect potionEffect : potionMeta.getCustomEffects())
            potionMeta.removeCustomEffect(potionEffect.getType());
        return this;
    }

    public ItemFactory setUnbreakable() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.spigot().setUnbreakable(true);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setBreakable() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.spigot().setUnbreakable(false);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setBreakable(boolean breakable) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.spigot().setUnbreakable(!breakable);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory build(Player player, int... slot) {
        build(player.getInventory(), slot);
        player.updateInventory();
        return this;
    }

    public ItemFactory noAttributes() {
        return this;
    }

    public ItemFactory build(Player player) {
        player.getInventory().addItem(itemStack);
        player.updateInventory();
        return this;
    }

    public ItemFactory build(Inventory inventory, int... slot) {
        for (int slots : slot) {
            inventory.setItem(slots, itemStack);
        }

        return this;
    }

    public ItemFactory build(Inventory inventory) {
        inventory.addItem(itemStack);
        return this;
    }

    public ItemFactory glow() {
        this.itemStack = addTag("ench");
        return this;
    }

    public ItemStack getStack() {
        return itemStack;
    }

    public ItemMeta setName(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        return meta;
    }

    public ItemFactory replaceDescription(String placeholder, String replacement) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                List<String> updatedLore = new ArrayList<>();
                for (String line : lore) {
                    updatedLore.add(line.replace(placeholder, replacement));
                }
                meta.setLore(updatedLore);
                itemStack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemFactory replaceDescription(String placeholder, int replacement) {
        return replaceDescription(placeholder, String.valueOf(replacement));
    }

    public ItemFactory setSkull(String owner) {
        itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(owner);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setSkullURL(String url) {
        itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", new String(Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/" + url).getBytes()))));
        FieldHelper.setValue(meta, "profile", profile);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemFactory setSkullProperty(Property property) {
        itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", property);

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStack setColor(Material material, Color color) {
        ItemStack stack = new ItemStack(material);
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) stack.getItemMeta();
        armorMeta.setColor(color);
        stack.setItemMeta(armorMeta);
        return stack;
    }

    public ItemFactory setColor(Color color) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) getStack().getItemMeta();
        armorMeta.setColor(color);
        getStack().setItemMeta(armorMeta);
        return this;
    }

    public ItemStack setColor(Material material, Color color, String name) {
        ItemStack stack = new ItemStack(material);
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) stack.getItemMeta();
        armorMeta.setColor(color);
        armorMeta.setDisplayName(name);
        stack.setItemMeta(armorMeta);
        return stack;
    }

    public ItemFactory setColor(Color color, String name) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        armorMeta.setColor(color);
        armorMeta.setDisplayName(name);
        itemStack.setItemMeta(armorMeta);
        return this;
    }

    public ItemFactory chanceItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    public boolean checkItem(ItemStack item, String display) {
        return (item != null && item.getType() != Material.AIR && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equalsIgnoreCase(display));
    }

    public boolean checkContains(ItemStack item, String display) {
        return (item != null && item.getType() != Material.AIR && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains(display));
    }

    private static List<String> getFormattedLore(int limit, String text) {

        List<String> lore = new ArrayList<>();
        String[] split = text.split(" ");
        text = "";

        for (int i = 0; i < split.length; ++i) {
            if (ChatColor.stripColor(text).length() > limit || ChatColor.stripColor(text).endsWith(".")
                    || ChatColor.stripColor(text).endsWith("!")) {
                lore.add("§7" + text);
                if (text.endsWith(".") || text.endsWith("!")) {
                    lore.add("");
                }
                text = "";
            }
            String toAdd = split[i];
            if (toAdd.contains("\n")) {
                toAdd = toAdd.substring(0, toAdd.indexOf("\n"));
                split[i] = split[i].substring(toAdd.length() + 1);
                lore.add("§7" + text + ((text.length() == 0) ? "" : " ") + toAdd);
                text = "";
                --i;
            } else {
                text += ((text.length() == 0) ? "" : " ") + toAdd;
            }
        }
        lore.add("§7" + text);

        return lore;
    }

    public ItemStack addTag(String... tag) {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(this.itemStack);
        NBTTagCompound nbtTagCompound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();
        for (String str : tag)
            nbtTagCompound.set(str, new NBTTagByte((byte) 0));
        nmsCopy.setTag(nbtTagCompound);
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }
}
