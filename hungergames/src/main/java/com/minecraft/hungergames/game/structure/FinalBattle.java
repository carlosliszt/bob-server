/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.structure;

import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.arena.shape.Square;
import com.minecraft.hungergames.util.constructor.Assistance;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class FinalBattle implements Assistance {

    private final Location location;
    private boolean disableKits;

    public FinalBattle(Location location) {
        this.location = location;
    }

    public void prepare() {

        List<Location> blocks = WorldEditAPI.getInstance().makeCylinder(location, 22, 22, 1, true);

        blocks.stream().map(Location::getBlock).forEach(block -> {

            WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);

            while (block.getY() < getGame().getVariables().getWorldHeight()) {

                if (block.getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.UP);
                    continue;
                }

                Location blockLocation = block.getLocation();

                WorldEditAPI.getInstance().setBlock(blockLocation, Material.AIR, (byte) 0);
                block = block.getRelative(BlockFace.UP);
            }
        });

        Square square = new Square(35, (getGame().getVariables().getWorldHeight() - location.getBlockY()), Collections.singletonList(Pattern.of(Material.BEDROCK)), false, false);
        square.spawn(location, (loc, pattern) -> {
            if (loc.getBlock().getType() != Material.BEDROCK)
                WorldEditAPI.getInstance().setBlock(loc, Material.BEDROCK, (byte) 0, false);
            return false;
        });
    }

    public void teleport() {

        if (disableKits) {
            for (Kit kit : getPlugin().getKitStorage().getKits()) {
                if (kit.isActive())
                    kit.setActive(false, true);
            }
        }

        Location loc = this.location.clone().add(0.5, 0.8, 0.5);

        getGame().getVariables().setWorldHeight(50);
        getGame().getVariables().setWorldSize(38);

        try {
            getPlugin().getVariableLoader().getVariable("hg.border.damage").setValue(4);
        } catch (Exception e) {
            System.out.println("The plugin can't change border damage.");
        }

        PlayerMassiveTeleportExecuteEvent event = new PlayerMassiveTeleportExecuteEvent(getPlugin().getUserStorage().getUsers().stream().map(User::getPlayer).collect(Collectors.toSet()), loc);
        event.fire();

        event.getRecipients().forEach(c -> {

            Player player = c.getPlayer();

            player.setNoDamageTicks(20);
            player.setFallDistance(-1);
            player.teleport(loc);
        });
    }

    public void setDisableKits(boolean disableKits) {
        this.disableKits = disableKits;
    }
}
