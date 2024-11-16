/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.CommandFrame;
import com.minecraft.core.command.command.CommandHolder;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BukkitContext implements Context<CommandSender> {
    @NonNull
    private final UUID uuid;
    @NonNull
    private final String label;
    @NonNull
    private final CommandSender sender;
    @NonNull
    private final Platform platform;
    @NonNull
    private final String[] args;
    @NonNull
    private final CommandFrame<?, ?, ?> commandFrame;
    @NonNull
    private final CommandHolder<?, ?> commandHolder;

    /**
     * Sends a message to the CommandSender
     *
     * @param message the message to be sent
     */
    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    /**
     * Sends a array of messages to the CommandSender
     *
     * @param messages the messages to be sent
     */
    @Override
    public void sendMessage(String[] messages) {
        sender.sendMessage(messages);
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
