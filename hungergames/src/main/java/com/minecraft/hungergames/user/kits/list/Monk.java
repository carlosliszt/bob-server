/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Monk extends Kit {

    public Monk(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.BLAZE_ROD));
        setItems(new ItemFactory(Material.BLAZE_ROD).setName("§aShuffle").setDescription("§7Kit Monk").getStack());
        setKitCategory(KitCategory.COMBAT);
        setCooldown(16);
        setPrice(25000);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isPlayer(event.getRightClicked())) {

            Player player = event.getPlayer();
            Player clicked = (Player) event.getRightClicked();

            if (isUser(player) && isItem(player.getItemInHand())) {

                if (checkInvincibility(player))
                    return;

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                if (!getUser(clicked.getUniqueId()).isAlive())
                    return;

                addCooldown(player.getUniqueId());

                int slot = Constants.RANDOM.nextInt(36);

                ItemStack current = clicked.getItemInHand() != null ? clicked.getItemInHand().clone() : null;
                ItemStack random = clicked.getInventory().getItem(slot) != null ? clicked.getInventory().getItem(slot).clone() : null;
                clicked.getInventory().setItem(slot, current);
                if (random == null) {
                    clicked.setItemInHand(null);
                } else {
                    clicked.getInventory().setItemInHand(random);
                }
                player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("kit.monk.inventory_shuffle", clicked.getName()));
            }
        }
    }
}
