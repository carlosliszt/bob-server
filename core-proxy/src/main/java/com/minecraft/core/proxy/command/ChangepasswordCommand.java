/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ChangepasswordCommand implements ProxyInterface {

    @Command(name = "changepassword", usage = "changepassword <password> <new_password>", aliases = {"alterarsenha", "mudarsenha"}, platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context, String password, String new_password) {
        Account account = context.getAccount();

        if (password.equals(new_password)) {
            context.info("command.changepassword.same_password");
            return;
        }

        if (account.getData(Columns.PASSWORD).getAsString().equals("...")) {
            context.info("command.changepassword.no_registration");
            return;
        }

        if (!password.equals(account.getData(Columns.PASSWORD).getAsString())) {
            context.info("command.changepassword.invalid_password");
            return;
        }

        if (!PATTERN.matcher(new_password).matches()) {
            context.info("command.changepassword.fail");
            return;
        }

        context.info("command.changepassword.successful");
        account.getData(Columns.PASSWORD).setData(new_password);
        account.getData(Columns.PASSWORD_LAST_UPDATE).setData(System.currentTimeMillis());

        async(() -> account.getDataStorage().saveColumnsFromSameTable(Columns.PASSWORD, Columns.PASSWORD_LAST_UPDATE));
    }
}