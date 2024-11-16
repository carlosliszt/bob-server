/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.PreferencesInventory;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import org.bukkit.entity.Player;

public class PreferencesCommand implements BukkitInterface {

    @Command(name = "preferences", aliases = {"preferencias", "prefs", "conf", "config"}, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {
        Player player = context.getSender();
        Account account = Account.fetch(player.getUniqueId());
        if (account == null)
            return;
        new PreferencesInventory(player, account, false).openInventory();
    }

}