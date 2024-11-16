/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.commands;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.types.Arena;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.listeners.InventoryListeners;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements BukkitInterface {

    @Command(name = "kit", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {
        Player player = context.getSender();
        User user = User.fetch(player.getUniqueId());

        if (!(user.getGame() instanceof Arena)) {
            context.info("command.unavailable");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            InventoryListeners.openSelector(user, Type.PRIMARY);
            return;
        }

        if (!user.isKept()) {
            context.info("pvp.command.kit.not_allowed_to_select");
            return;
        }

        Kit kit = PvP.getPvP().getKitStorage().getKit(args[0]);

        if (kit == null) {
            context.info("object.not_found", "Kit");
            return;
        }

        if (!kit.isActive()) {
            context.info("kit.not_active");
            return;
        }

        if (!kit.isNone() && kit.getName().equals(user.getKit1().getName())) {
            context.info("kit.already_selected");
            return;
        }

        if (user.getKit2().getName().equals(kit.getName()) && !kit.isNone()) {
            context.info("kit.already_selected");
            return;
        }

        if (PvP.getPvP().getKitStorage().isBlocked(kit.getClass(), user.getKit2().getClass())) {
            context.info("kit.not_compatible_with", kit.getName(), user.getKit2().getName());
            return;
        }

        context.info("kit.select", kit.getName());
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 2F, 2F);

        user.setKit1(kit);
        user.handleSidebar();
    }

    @Completer(name = "kit")
    public List<String> tabComplete(Context<Player> context) {
        User user = User.fetch(context.getUniqueId());
        return PvP.getPvP().getKitStorage().getKits().stream().filter(kit -> startsWith(kit.getName(), context.getArg(context.argsCount() - 1)) && kit.isActive() && user.hasKit(kit, Type.PRIMARY) && user.getGame() instanceof Arena).map(kit -> kit.getName().toLowerCase()).collect(Collectors.toList());
    }

}
