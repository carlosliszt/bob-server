/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Monk extends Kit {

    public Monk() {
        setIcon(new ItemStack(Material.BLAZE_ROD));
        setCategory(KitCategory.COMBAT);
        setItems(new ItemFactory(Material.BLAZE_ROD).setName("§aShuffle").setDescription("§7Kit Monk").getStack());
        setDefaultRank(Rank.MEMBER);
        setPrice(25000);
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {

            Player player = event.getPlayer();
            Player clicked = (Player) event.getRightClicked();

            if (isUser(player) && isItem(player)) {
                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                User targetUser = User.fetch(clicked.getUniqueId());

                if (!clicked.isOnline() || targetUser == null || Vanish.getInstance().isVanished(clicked.getUniqueId()) || !clicked.getWorld().getName().equals(player.getWorld().getName()) || targetUser.isKept())
                    return;

                addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 16);

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
