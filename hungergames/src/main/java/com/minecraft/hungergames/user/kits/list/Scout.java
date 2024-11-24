/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Scout extends Kit {

    public Scout(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.POTION, 8226));
        setItems(new ItemFactory(Material.POTION).setDurability(8226).setName("§aSpeed").setDescription("§7Kit Scout").removePotionEffects().addItemFlag(ItemFlag.values()).getStack());
        setCooldown(32);
        setKitCategory(KitCategory.MOVEMENT);
        setPrice(35000);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (isItem(event.getItem())) {
            event.setCancelled(true);
            event.getPlayer().setItemInHand(event.getPlayer().getItemInHand());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.getAction().toString().contains("RIGHT") && isUser(player) && isItem(player.getItemInHand())) {

            event.setCancelled(true);

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            int time = 400;

            if (event.getPlayer().hasPotionEffect(PotionEffectType.SPEED)) {
                PotionEffect potionEffect = event.getPlayer().getActivePotionEffects().stream().filter(c -> c.getType() == PotionEffectType.SPEED).findAny().orElse(null);
                if (potionEffect != null)
                    time += potionEffect.getDuration();
            }

            addCooldown(player.getUniqueId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, time, 1), true);
        }
    }
}
