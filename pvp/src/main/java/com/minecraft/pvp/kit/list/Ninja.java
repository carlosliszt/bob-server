/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.GameMetadata;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ninja extends Kit {

    public Ninja() {
        setIcon(new ItemStack(Material.NETHER_STAR));
        setCategory(KitCategory.MOVEMENT);
        setPrice(30000);
        setDefaultRank(Rank.VIP);
    }

    private final Map<UUID, Player> targettedPlayers = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            Player attacker = (Player) event.getDamager();
            if (isUser(attacker)) {
                attacker.setMetadata("ninja_millies", new GameMetadata(System.currentTimeMillis()));
                targettedPlayers.put(attacker.getUniqueId(), ((Player) event.getEntity()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {

            Player player = event.getPlayer();

            if (isUser(player)) {

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                if (!targettedPlayers.containsKey(player.getUniqueId()))
                    return;

                User user = User.fetch(player.getUniqueId());
                Player target = targettedPlayers.get(event.getPlayer().getUniqueId());
                User targetUser = User.fetch(target.getUniqueId());

                if (!target.isOnline() || targetUser == null || Vanish.getInstance().isVanished(target.getUniqueId()) || targetUser.isKept() || !target.getWorld().getUID().equals(player.getWorld().getUID())) {
                    player.sendMessage(user.getAccount().getLanguage().translate("kit.ninja.target_not_found"));
                    return;
                }

                Kit neo = getPlugin().getKitStorage().getKit("Neo");

                if (targetUser.getKit1().getName().equals(neo.getName()) || targetUser.getKit2().getName().equals(neo.getName())) {
                    player.sendMessage(user.getAccount().getLanguage().translate("kit.neo.not_affected"));
                    return;
                }

                if (player.getLocation().distanceSquared(target.getLocation()) > 3025) {
                    player.sendMessage(user.getAccount().getLanguage().translate("kit.ninja.too_far_away"));
                    return;
                }

                targettedPlayers.remove(player.getUniqueId());
                addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 7);
                player.teleport(target);
            }
        }
    }

    @Override
    public void resetAttributes(User user) {
        targettedPlayers.remove(user.getUniqueId());
    }

}