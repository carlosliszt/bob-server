/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.CommandFrame;
import com.minecraft.core.command.command.CommandHolder;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ProxyContext implements Context<CommandSender> {
    @NonNull
    private final UUID uuid;
    @NonNull
    private final CommandSender sender;
    @NonNull
    private final String label;
    @NonNull
    private final Platform platform;
    @NonNull
    private final String[] args;

    @NonNull
    private final CommandFrame<?, ?, ?> commandFrame;
    @NonNull
    private final CommandHolder<?, ?> commandHolder;

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public boolean isPlayer() {
        return this.uuid != Constants.CONSOLE_UUID;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    private Account account;

    public Account getAccount() {
        if (account == null)
            return account = Account.fetch(uuid);
        return account;
    }
}
