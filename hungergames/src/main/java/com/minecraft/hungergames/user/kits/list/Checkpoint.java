/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Checkpoint extends Kit {

    public Checkpoint(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.NETHER_FENCE));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.NETHER_FENCE).setName("§aCheck").setDescription("§7Kit Checkpoint").getStack(), new ItemFactory(Material.FLOWER_POT_ITEM).setName("§aPoint").setDescription("§7Kit Checkpoint").getStack());
    }

    public Map<UUID, Location> playerPoints = new ConcurrentHashMap<>();


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (isItem(event.getPlayer().getItemInHand(), Material.NETHER_FENCE)) {

            Player player = event.getPlayer();

            if (!isUser(player))
                return;

            player.setItemInHand(player.getItemInHand());
            player.updateInventory();

            if (playerPoints.containsKey(player.getUniqueId())) {

                Location location = playerPoints.get(player.getUniqueId());

                if (location.equals(event.getBlock().getLocation()))
                    return;

                Block currentBlock = location.getBlock();

                currentBlock.setType(Material.AIR);
                currentBlock.getWorld().playEffect(currentBlock.getLocation(), Effect.STEP_SOUND, Material.NETHER_FENCE.getId());
            }
            event.getBlock().setType(Material.NETHER_FENCE);
            playerPoints.put(player.getUniqueId(), event.getBlock().getLocation());
            player.sendMessage("§aLocalização marcada! Use seu vaso para voltar até aqui.");
        }
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isItem(event.getItem(), Material.FLOWER_POT_ITEM))
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (!playerPoints.containsKey(player.getUniqueId())) {
            player.sendMessage("§cNenhuma marcação encontrada.");
            return;
        }

        Location location = playerPoints.get(player.getUniqueId());

        double distance = location.distance(player.getLocation());

        if (distance > 90000) { /* 300 blocks */
            player.sendMessage("§cVocê está muito longe do seu checkpoint.");
            return;
        } else if (distance > 10000)  /* 100 blocks */
            location.getWorld().strikeLightningEffect(location);

        playerPoints.remove(player.getUniqueId());
        player.teleport(location);
        location.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getBlock().getType() == Material.NETHER_FENCE) {
            if (playerPoints.containsValue(event.getBlock().getLocation())) {

                event.getBlock().setType(Material.AIR);

                UUID uuid = playerPoints.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(event.getBlock().getLocation()))
                        .map(Map.Entry::getKey).findFirst().orElse(null);

                if (uuid != null && !uuid.equals(event.getPlayer().getUniqueId())) {

                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null)
                        return;

                    player.sendMessage("§cDestruiram sua marcação!");
                }
                playerPoints.remove(uuid);
            }
        }
    }

    @EventHandler
    public void onLivingUserDie(LivingUserDieEvent event) {
        if (!playerPoints.containsKey(event.getUser().getUniqueId()))
            return;
        playerPoints.remove(event.getUser().getUniqueId()).getBlock().setType(Material.AIR);
    }
}
