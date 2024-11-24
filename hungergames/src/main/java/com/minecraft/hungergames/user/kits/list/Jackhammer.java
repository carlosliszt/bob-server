/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.account.fields.Property;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Jackhammer extends Kit {

    public Jackhammer(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.STONE_AXE));
        setItems(new ItemFactory(Material.STONE_AXE).setName("§aCavar").setDescription("§7Kit Jackhammer").getStack());
        setCooldown(28);
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @Variable(name = "hg.kit.jackhammer.max_uses", permission = Rank.ADMINISTRATOR)
    private int max_uses = 5;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(BlockBreakEvent event) {

        Player player = event.getPlayer();

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (checkInvincibility(player))
            return;

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        event.setCancelled(true);

        User user = getUser(player.getUniqueId());

        Property property = user.getAccount().getProperty("hg.kit.jackhammer.uses", 0);

        int uses = property.getAsInt();

        uses++;

        property.setValue(uses);

        if (event.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
            breakBlock(event.getBlock(), BlockFace.UP);
        } else {
            breakBlock(event.getBlock(), BlockFace.DOWN);
        }

        if (uses == max_uses) {
            addCooldown(player.getUniqueId());
            user.getAccount().removeProperty("hg.kit.jackhammer.uses");
        }
    }

    private void breakBlock(final Block b, final BlockFace face) {
        new BukkitRunnable() {

            Block block = b;

            public void run() {
                if (block.getType() != Material.BEDROCK && block.getY() <= 128 && !block.hasMetadata("unbreakable")) {
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType().getId(), 30);
                    WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);
                    block = block.getRelative(face);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(getPlugin(), 2L, 2L);
    }
}
