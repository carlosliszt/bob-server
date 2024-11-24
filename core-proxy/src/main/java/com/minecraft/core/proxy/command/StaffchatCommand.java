/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.chat.ChatType;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffchatCommand implements ProxyInterface {

    @Command(name = "staffchat", aliases = {"sc"}, platform = Platform.PLAYER, rank = Rank.HELPER)
    public void handleCommand(Context<ProxiedPlayer> context) {
        String[] args = context.getArgs();

        Account account = context.getAccount();
        ChatType chatType = account.getProperty("chat_type", ChatType.NORMAL).getAs(ChatType.class);

        if (args.length == 0) {
            if (chatType != ChatType.STAFF) {
                account.setProperty("chat_type", ChatType.STAFF);
                account.setProperty("old_chat_type", chatType);
                context.info("command.staffchat.join");
            } else {

                ChatType type = account.getProperty("old_chat_type", ChatType.NORMAL).getAs(ChatType.class);

                account.setProperty("chat_type", type == ChatType.STAFF ? ChatType.NORMAL : type);
                context.info("command.staffchat.left");
            }
        } else if (args[0].equalsIgnoreCase("toggle")) {

            account.setPreference(Preference.STAFFCHAT, !account.getPreference(Preference.STAFFCHAT));

            account.getData(Columns.PREFERENCES).setData(account.getPreferences());
            async(() -> account.getDataStorage().saveColumn(Columns.PREFERENCES));

            boolean enabled = account.getPreference(Preference.STAFFCHAT);

            context.info((enabled ? "command.staffchat.enable" : "command.staffchat.disable"));
        } else {
            context.info("no_function", context.getArg(0).toLowerCase());
        }
    }
}
