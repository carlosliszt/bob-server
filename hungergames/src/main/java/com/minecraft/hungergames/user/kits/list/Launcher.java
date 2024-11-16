/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Launcher extends Kit {

    public Launcher(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SPONGE));
        setPrice(35000);
        setKitCategory(KitCategory.STRATEGY);
    }

    @Override
    public void grant(Player player) {
        super.grant(player);
        giveItem(player, new ItemStack(Material.SPONGE, 20), player.getLocation());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Block block = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (block.getType() == Material.SPONGE /*&& block.hasMetadata("hg.kit.launcher.boost_block")*/) {
            Vector vector = event.getPlayer().getLocation().getDirection();

            if (block.getRelative(BlockFace.UP).getType() == Material.WOOD_PLATE)
                vector.multiply(3.92).setY(1.10);
            else
                vector.setY(4);

            event.getPlayer().setVelocity(vector);
            Account.fetch(event.getPlayer().getUniqueId()).setProperty("hg.kit.launcher.no_fall_damage", true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL && Account.fetch(event.getEntity().getUniqueId()).hasProperty("hg.kit.launcher.no_fall_damage")) {
            Account.fetch(event.getEntity().getUniqueId()).removeProperty("hg.kit.launcher.no_fall_damage");
            event.setCancelled(true);
        }
    }
}
