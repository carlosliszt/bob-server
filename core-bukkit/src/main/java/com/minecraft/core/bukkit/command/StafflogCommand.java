/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import org.bukkit.entity.Player;

public class StafflogCommand implements BukkitInterface {

    @Command(name = "stafflog", platform = Platform.PLAYER, rank = Rank.PRIMARY_MOD)
    public void handleCommand(Context<Player> context) {
        Account account = context.getAccount();
        account.setPreference(Preference.STAFFLOG, !account.getPreference(Preference.STAFFLOG));
        account.getData(Columns.PREFERENCES).setData(account.getPreferences());
        boolean enabled = account.getPreference(Preference.STAFFLOG);
        context.info("command.stafflog.toggle", enabled);
        async(() -> account.getDataStorage().saveColumn(Columns.PREFERENCES));
    }
}