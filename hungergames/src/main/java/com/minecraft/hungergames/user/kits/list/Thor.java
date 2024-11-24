/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;
import java.util.UUID;

public class Thor extends Kit {

    protected final int THOR_Y= 80;

    public Thor(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WOOD_AXE));
        setItems(new ItemFactory(Material.WOOD_AXE).setName("§aInvocar raio").setDescription("§7Kit Thor").getStack());
        setCooldown(7);
        setKitCategory(KitCategory.COMBAT);
        setPrice(35000);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if (!event.getAction().name().contains("RIGHT_CLICK"))
            return;

        if (event.hasItem() && isUser(event.getPlayer())) {

            if (!isItem(event.getItem()))
                return;

            Player player = event.getPlayer();

            if (checkInvincibility(event.getPlayer()))
                return;

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            Block block = (event.getAction() == Action.RIGHT_CLICK_BLOCK ? event.getClickedBlock() : player.getTargetBlock((Set<Material>) null, 100));

            Location playerLocation = player.getLocation().clone();
            Location blockLocation = block.getLocation().clone();

            playerLocation.setY(0);
            blockLocation.setY(0);

            if (playerLocation.distanceSquared(blockLocation) > 36) {
                return;
            }

            Block highestBlockAt = block.getWorld().getHighestBlockAt(block.getLocation());

            if (highestBlockAt.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK) {
                highestBlockAt.getRelative(BlockFace.DOWN).setType(Material.AIR);

                LightningStrike lightningStrike = highestBlockAt.getWorld().strikeLightning(highestBlockAt.getLocation());
                lightningStrike.setMetadata("hg.kit.thor.owner", new GameMetadata(player.getUniqueId()));

                float f = 2.5F;
                if (highestBlockAt.getBiome() != Biome.HELL) {
                    for (int x = -3; x <= 3; x++) {
                        for (int z = -3; z <= 3; z++) {
                            for (int y = -3; y <= 3; y++) {
                                Block b3 = highestBlockAt.getLocation().add(x, y, z).getBlock();
                                if (b3.getType() == Material.NETHERRACK) {
                                    b3.setType(Material.AIR);
                                    f = f + 1F;
                                }
                            }
                        }
                    }
                    highestBlockAt.getWorld().createExplosion(highestBlockAt.getLocation(), f);
                }
            } else {
                LightningStrike lightningStrike = highestBlockAt.getWorld().strikeLightning(highestBlockAt.getLocation());
                lightningStrike.setMetadata("hg.kit.thor.owner", new GameMetadata(player.getUniqueId()));

                if (highestBlockAt.getLocation().getBlockY() >= THOR_Y) {
                    highestBlockAt.setType(Material.NETHERRACK);
                }
            }

            addCooldown(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (isPlayer(event.getEntity()) && event.getDamager() instanceof LightningStrike) {

            LightningStrike lightningStrike = (LightningStrike) event.getDamager();

            if (lightningStrike.hasMetadata("hg.kit.thor.owner")) {

                User owner = getUser((UUID) lightningStrike.getMetadata("hg.kit.thor.owner").get(0).value());

                if (owner.getUniqueId().equals(event.getEntity().getUniqueId())) {
                    event.getEntity().removeMetadata("hg.kit.thor.owner", getPlugin());
                    event.setCancelled(true);
                } else if (event.getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name().contains("WATER")) {
                    event.setDamage(event.getDamage() + 3);
                }

            }
        }
    }
}
