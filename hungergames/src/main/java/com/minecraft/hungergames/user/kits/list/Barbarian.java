/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Barbarian extends Kit {

    private final HashMap<UUID, Integer> kills = new HashMap<>();

    public Barbarian(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WOOD_SWORD, true));
        setItems(new ItemFactory(Material.WOOD_SWORD).setName("§aEspada").setDescription("§7Kit Barbarian").getStack());
        setPrice(35000);
        setKitCategory(KitCategory.COMBAT);
    }

    @Override
    public void grant(Player player) {
        if (!kills.containsKey(player.getUniqueId()))
            super.grant(player);
        else {
            ItemStack itemStack = getItems()[0].clone();
            upgrade(player, itemStack);
            giveItem(player, itemStack, player.getLocation());
        }
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        User user = event.getKiller();

        if (kills.containsKey(user.getUniqueId()))
            kills.put(user.getUniqueId(), kills.get(user.getUniqueId()) + 1);
        else
            kills.put(user.getUniqueId(), 1);

        upgrade(user.getPlayer(), user.getPlayer().getItemInHand());
    }

    public void upgrade(Player player, ItemStack itemStack) {
        if (!isItem(itemStack))
            return;
        switch (this.kills.get(player.getUniqueId())) {
            case 1: {
                itemStack.setType(Material.STONE_SWORD);
                break;
            }
            case 3: {
                itemStack.setType(Material.IRON_SWORD);
                break;
            }
            case 7: {
                itemStack.setType(Material.DIAMOND_SWORD);
                break;
            }
            case 10: {
                itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                break;
            }
            case 11: {
                itemStack.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                break;
            }
        }
    }
}
