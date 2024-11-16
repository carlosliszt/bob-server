/*
 * Copyright (C) BobMC, All Rights Reserved
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
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class LoginCommand implements ProxyInterface {

    @Command(name = "login", usage = "login <password>", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context, String password) {
        Account account = context.getAccount();

        if (account.getProperty("authenticated").getAsBoolean()) {
            context.info("command.login.already_authenticated");
            return;
        }

        if (!account.hasProperty("captcha_successful")) {
            context.sendMessage("§cVocê deve completar o desafio para fazer isso.");
            return;
        }


        if (account.getData(Columns.PASSWORD).getAsString().equals("...")) {
            context.info("command.login.empty_password");
            return;
        }

        if (!password.equals(account.getData(Columns.PASSWORD).getAsString())) {
            context.info("command.login.incorrect_password");
            return;
        }

        context.info("command.login.successful");
        account.setProperty("authenticated", true);
        context.getSender().connect(BungeeCord.getInstance().getServerInfo(ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY).getName()));

        account.getData(Columns.SESSION_EXPIRES_AT).setData(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
        account.getData(Columns.SESSION_ADDRESS).setData(account.getData(Columns.ADDRESS).getAsString());

        async(() -> account.getDataStorage().saveColumnsFromSameTable(Columns.SESSION_EXPIRES_AT, Columns.SESSION_ADDRESS));
    }
}