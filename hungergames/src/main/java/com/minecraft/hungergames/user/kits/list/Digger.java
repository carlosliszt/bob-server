/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class Digger extends Kit {

    public Digger(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.DRAGON_EGG));
        setItems(new ItemFactory(Material.DRAGON_EGG).setName("§aEscavar").setDescription("§7Kit Digger").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setCooldown(20);
        setPrice(25000);
        setCombatCooldown(true);
    }

    @Variable(name = "hg.kit.digger.radius", permission = Rank.ADMINISTRATOR)
    private int radius = 5;

    @Variable(name = "hg.kit.digger.depth", permission = Rank.ADMINISTRATOR)
    private int depth = 6;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        ItemStack itemStack = player.getItemInHand();

        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        if (isUser(player) && isItem(itemStack)) {

            event.setCancelled(true);
            player.updateInventory();

            if (checkInvincibility(player))
                return;

            if (isCombat(player) || isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            addCooldown(player.getUniqueId());

            Location block = event.getBlock().getLocation();
            Location firstPosition = block.clone().add(radius, 0, radius);
            Location secondPosition = block.clone().add(-radius, -depth, -radius);

            block.getWorld().playEffect(block, Effect.STEP_SOUND, block.getBlock().getType().getId());
            WorldEditAPI.getInstance().getBlocksBetween(firstPosition, secondPosition).forEach(c -> {
                if (c.getBlock().getType() != Material.BEDROCK && c.getBlock().getType() != Material.CHEST && !c.getBlock().hasMetadata("unbreakable"))
                    c.getBlock().setType(Material.AIR);
            });

            player.setMetadata("kit.digger.no_fall", new GameMetadata(true));

            for (Entity entity : player.getNearbyEntities(radius, 3, radius))
                entity.setMetadata("kit.digger.no_fall", new GameMetadata(true));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity().hasMetadata("kit.digger.no_fall")) {
            event.getEntity().removeMetadata("kit.digger.no_fall", getPlugin());
            event.setCancelled(true);
        }
    }

    @Variable(name = "hg.kit.digger.combat_cooldown_time", permission = Rank.ADMINISTRATOR)
    public double duration = 1.8;

    @Override
    public double getCombatTime() {
        return duration;
    }

}