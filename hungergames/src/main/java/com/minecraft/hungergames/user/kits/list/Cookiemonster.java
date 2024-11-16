/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Cookiemonster extends Kit {

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);
    @Variable(name = "hg.kit.cookiemonster.drop_chance", permission = Rank.PRIMARY_MOD)
    private int dropChance = 40;

    public Cookiemonster(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.COOKIE));
        setItems(new ItemFactory(Material.COOKIE).setName("§aBiscoito Divino").setAmount(20).setDescription("§7Kit Cookiemonster").getStack());
        setCooldown(11);
        setKitCategory(KitCategory.STRATEGY);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (ACCEPTABLES_INTERACT.contains(event.getAction()) && event.hasItem() && isItem(event.getItem()) && isUser(event.getPlayer())) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            addCooldown(player.getUniqueId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (20 * 8), 1), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1), true);

            if (player.getFoodLevel() != 20)
                player.setFoodLevel(Math.min(player.getFoodLevel() + 3, 20));

            if (event.getItem().getAmount() > 1) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getType() == Material.LONG_GRASS && isUser(player) && Constants.RANDOM.nextInt(101) <= dropChance) {
            giveCookie(event.getBlock().getLocation(), player);
        }
    }

    private void giveCookie(Location location, Player player) {
        ItemStack itemStack = getItems()[0].clone();
        itemStack.setAmount(1);
        itemStack = addTag(itemStack, player.getUniqueId().toString());

        location.getWorld().dropItemNaturally(location.clone().add(0, 0.5, 0), itemStack);
    }
}
