/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.account.fields.Property;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Blink extends Kit {

    public Blink(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.NETHER_STAR));
        setItems(new ItemFactory(Material.NETHER_STAR).setName("§aTeleportar").setDescription("§7Kit Blink").getStack());
        setCooldown(20);
        setKitCategory(KitCategory.MOVEMENT);
        setPrice(25000);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @Variable(name = "hg.kit.blink.max_distance", permission = Rank.ADMINISTRATOR)
    public double distance = 5.0;

    @Variable(name = "hg.kit.blink.max_uses", permission = Rank.ADMINISTRATOR)
    @Setter
    private int maxUses = 4;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        final Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (isCooldown(player) || isCombat(player)) {
            dispatchCooldown(player);
            return;
        }

        final User user = getUser(player.getUniqueId());

        final Property property = user.getAccount().getProperty("hg.kit.blink.uses", 0);

        int uses = property.getAsInt();

        uses++;

        final Block b = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(distance)).getBlock();
        final boolean inGladiator = ((Gladiator) getKit("Gladiator")).isGladiator(player);

        if (b.getY() < 1 || b.getY() > 128 && !inGladiator)
            return;

        if (inGladiator) {
            player.sendMessage("§cVocê não poder usar o kit Blink no Gladiator.");
            return;
        }

        final int limit = getGame().getVariables().getWorldSize() - 10;

        if (absolute(b.getX()) > limit || absolute(b.getZ()) > limit)
            return;

        property.setValue(uses);

        if (b.getRelative(BlockFace.DOWN).getType() == Material.AIR)
            b.getRelative(0, -1, 0).setType(Material.LEAVES);

        final Location location = b.getLocation().add(0.5, 0.3, 0.5);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());

        player.setFallDistance(0);
        player.teleport(location);

        if (uses == maxUses) {
            addCooldown(player.getUniqueId());
            user.getAccount().removeProperty("hg.kit.blink.uses");
        }
    }

    @Override
    public double getCombatTime() {
        return 1.5;
    }

}
