/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Optional;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class ClearmobsCommand implements Listener, VariableStorage, BukkitInterface {

    public ClearmobsCommand() {
        loadVariables();
    }

    private final ImmutableSet<EntityType> ACCEPTABLES_TYPES = Sets.immutableEnumSet(EntityType.SQUID, EntityType.GUARDIAN, EntityType.PIG_ZOMBIE, EntityType.HORSE, EntityType.SILVERFISH, EntityType.ENDERMITE, EntityType.MAGMA_CUBE, EntityType.BLAZE, EntityType.GHAST, EntityType.CHICKEN, EntityType.COW, EntityType.SKELETON, EntityType.ZOMBIE, EntityType.PIG, EntityType.ENDERMAN, EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BAT, EntityType.OCELOT, EntityType.SHEEP, EntityType.RABBIT, EntityType.WOLF, EntityType.WITCH, EntityType.SLIME, EntityType.VILLAGER);

    @Variable(name = "hg.mob_spawn", permission = Rank.EVENT_MOD)
    private boolean spawnMobs = true;

    @Command(name = "clearmobs", platform = Platform.BOTH, rank = Rank.SECONDARY_MOD)
    public void handleCommand(Context<CommandSender> context, @Optional(def = "true") String[] disable) {

        boolean toggle = true;

        if (isBoolean(disable[0]))
            toggle = BukkitGame.getEngine().getBukkitFrame().getAdapterMap().getBoolean(disable[0]);

        AtomicInteger removed = new AtomicInteger();

        for (World world : Bukkit.getWorlds()) {

            world.getLivingEntities().forEach(entity -> {

                if (ACCEPTABLES_TYPES.contains(entity.getType())) {
                    removed.getAndIncrement();
                    entity.remove();
                }
            });
        }

        context.sendMessage("§aForam removidos %s mobs!", removed.get());
        if (toggle && spawnMobs) {
            context.sendMessage("§cDesativado spawn de mobs!");
            this.spawnMobs = false;
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!spawnMobs && ACCEPTABLES_TYPES.contains(event.getEntityType()))
            event.setCancelled(true);
    }
}
