/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Archer extends Kit {

    ItemStack arrow = new ItemFactory(Material.ARROW).setAmount(20).getStack();

    public Archer() {
        setIcon(new ItemStack(Material.BOW));
        setCategory(KitCategory.STRATEGY);
        setPrice(10000);
        setDefaultRank(Rank.MEMBER);
        setItems(new ItemFactory(Material.BOW).setName("§aRobin Hood").addEnchantment(Enchantment.ARROW_DAMAGE, 1).setDescription("§7Kit Archer").getStack(), arrow);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {

            event.setDamage(Math.max(4, event.getDamage() / 2));

            Arrow arrow = (Arrow) event.getDamager();

            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }

            Player p = (Player) arrow.getShooter();

            if (isUser(p)) {
                p.getInventory().addItem(new ItemStack(Material.ARROW));

                if (Constants.RANDOM.nextBoolean())
                    p.getInventory().addItem(new ItemStack(Material.ARROW));
            }
        }
    }

    @Override
    public void resetAttributes(User user) {

    }

}
