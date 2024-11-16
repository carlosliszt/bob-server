/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.entity.Player;

public class VanishCommand implements BukkitInterface {

    Vanish vanish = Vanish.getInstance();

    @Command(name = "vanish", aliases = {"v"}, platform = Platform.PLAYER, rank = Rank.STREAMER_PLUS)
    public void handleCommand(Context<Player> context) {
        boolean vanished = vanish.isVanished(context.getUniqueId());
        if (vanished)
            vanish.setVanished(context.getSender(), null, false);
        else
            vanish.setVanished(context.getSender(), context.getAccount().getRank(), false);
        log(context.getAccount(), context.getAccount().getDisplayName() + (!vanished ? " entrou no modo vanish" : " saiu do modo vanish"));
    }

}