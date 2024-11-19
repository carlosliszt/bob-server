/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatCommand implements BukkitInterface, Listener, VariableStorage {

    public ChatCommand() {
        loadVariables();
    }

    @Variable(name = "chat")
    public boolean CHAT_ACTIVE = true;

    @Command(name = "chat", platform = Platform.BOTH, rank = Rank.PARTNER_PLUS, usage = "chat <clear/true/false>")
    public void handleCommand(Context<CommandSender> context, String str) {
        if (isBoolean(str)) {
            setActive(BukkitGame.getEngine().getBukkitFrame().getAdapterMap().getBoolean(str));
            log("chat", String.valueOf(isActive()));
        } else if (str.equalsIgnoreCase("clear")) {
            for (int i = 0; i < 150; i++) {
                Bukkit.getOnlinePlayers().forEach(c -> c.sendMessage(" "));
            }
            log(context.getAccount(), context.getSender().getName() + " limpou o bate-papo.");
        } else {
            context.info("no_function", context.getArg(0));
        }
    }

    @Completer(name = "chat")
    public List<String> handleComplete(Context<CommandSender> context) {
        List<String> response = new ArrayList<>(Arrays.asList("true", "false", "clear"));
        response.removeIf(c -> !startsWith(c, context.getArg(0)));
        return response;
    }

    public void setActive(boolean chatActive) {
        CHAT_ACTIVE = chatActive;
    }

    public boolean isActive() {
        return CHAT_ACTIVE;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {

        if (isActive())
            return;

        Account account = Account.fetch(event.getPlayer().getUniqueId());

        if (!account.hasPermission(Rank.PARTNER_PLUS)) {
            event.getPlayer().sendMessage(account.getLanguage().translate("chat.not_enabled"));
            event.setCancelled(true);
        }
    }

    public void log(String key, String value) {
        List<Account> receivers = new ArrayList<>(Constants.getAccountStorage().getAccounts());
        receivers.removeIf(accounts -> accounts.getRank().getId() < Rank.PARTNER_PLUS.getId() || accounts.getProperty("stafflog", false).getAsBoolean());
        receivers.forEach(receiver -> {
            Player staff = Bukkit.getPlayer(receiver.getUniqueId());
            if (staff == null)
                return;
            staff.sendMessage(receiver.getLanguage().translate("command.variable.value_changed_to", key, value.toLowerCase()));
        });
    }

}