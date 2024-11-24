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
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.util.geodata.AddressData;
import com.minecraft.core.util.geodata.DataResolver;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class RegisterCommand implements ProxyInterface {

    @Command(name = "register", usage = "register <password> <confirmPassword>", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context, String password, String confirmPassword) {
        Account account = context.getAccount();

        if (account.getProperty("authenticated").getAsBoolean()) {
            context.info("command.register.already_authenticated");
            return;
        }

        if (!account.hasProperty("captcha_successful")) {
            context.sendMessage("§cVocê deve completar o desafio para fazer isso.");
            return;
        }

        if (!account.getData(Columns.PASSWORD).getAsString().equals("...")) {
            context.info("command.register.already_registered");
            return;
        }

        if (!PATTERN.matcher(password).matches()) {
            context.info("command.register.invalid_password");
            return;
        }

        if (!password.equalsIgnoreCase(confirmPassword)) {
            context.info("command.register.password_does_not_match");
            return;
        }

        AddressData addressData = DataResolver.getInstance().getData(context.getSender().getAddress().getHostString());

        if (addressData.getRegister() == 5) {
            context.sendMessage("§cVocê atingiu o número limite de registros em um determinado tempo. Por favor, tente novamente mais tarde.");
            return;
        }

        addressData.setRegister(addressData.getRegister() + 1);

        context.info("command.register.successful");
        account.setProperty("authenticated", true);
        context.getSender().connect(BungeeCord.getInstance().getServerInfo(ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY).getName()));

        account.getData(Columns.SESSION_EXPIRES_AT).setData(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
        account.getData(Columns.SESSION_ADDRESS).setData(account.getData(Columns.ADDRESS).getAsString());
        account.getData(Columns.REGISTERED_AT).setData(System.currentTimeMillis());
        account.getData(Columns.PASSWORD_LAST_UPDATE).setData(System.currentTimeMillis());
        account.getData(Columns.PASSWORD).setData(password);

        async(() -> account.getDataStorage().saveColumnsFromSameTable(Columns.SESSION_EXPIRES_AT, Columns.SESSION_ADDRESS, Columns.REGISTERED_AT, Columns.PASSWORD_LAST_UPDATE, Columns.PASSWORD));
    }
}