/*
 * Copyright (C) YoloMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountStorage;
import com.minecraft.core.command.CommandFrame;
import com.minecraft.core.command.CommandList;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.argument.AdapterMap;
import com.minecraft.core.command.argument.eval.MethodEvaluator;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.executor.CompleterExecutor;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.command.ProxyCommand;
import com.minecraft.core.proxy.util.command.executor.ProxyAsynchronouslyExecutor;
import com.minecraft.core.proxy.util.command.executor.ProxyCommandExecutor;
import com.minecraft.core.proxy.util.command.executor.ProxyCompleterExecutor;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;

@Getter
public class ProxyFrame implements CommandFrame<Plugin, CommandSender, ProxyCommand> {

    private final Plugin plugin;
    private final AdapterMap adapterMap;

    private final Map<String, ProxyCommand> commandMap;
    private final MethodEvaluator methodEvaluator;

    private Executor executor;

    private final List<CommandInfo> commandInfoList;

    public ProxyFrame(Plugin plugin, AdapterMap adapterMap) {
        this.plugin = plugin;

        this.adapterMap = adapterMap;

        this.commandInfoList = new ArrayList<>();

        this.executor = new ProxyAsynchronouslyExecutor(plugin);

        this.commandMap = new HashMap<>();
        this.methodEvaluator = new MethodEvaluator(adapterMap);
    }

    public ProxyFrame(Plugin plugin, boolean registerDefault) {
        this(plugin, new AdapterMap(registerDefault));

        if (registerDefault) {
            registerAdapter(ProxiedPlayer.class, this::getPlayer);
            registerAdapter(ServerInfo.class, BungeeCord.getInstance()::getServerInfo);
            registerAdapter(PunishType.class, PunishType::fromString);
            registerAdapter(PunishCategory.class, PunishCategory::fromString);
        }
    }

    private ProxiedPlayer getPlayer(String text) {
        Account account;

        if (Constants.isUniqueId(text))
            account = Account.fetch(UUID.fromString(text));
        else
            account = AccountStorage.getAccountByName(text, false);
        if (account != null)
            return ProxyServer.getInstance().getPlayer(account.getUniqueId());
        return null;
    }

    public ProxyFrame(Plugin plugin) {
        this(plugin, true);
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public ProxyCommand getCommand(String name) {
        int index = name.indexOf('.');
        String recursiveCommand = (index != -1 ? name.substring(0, index) : name).toLowerCase();

        ProxyCommand command = commandMap.get(recursiveCommand);
        if (command == null) {
            command = new ProxyCommand(this, recursiveCommand, 0);
            commandMap.put(recursiveCommand, command);
        }

        return index != -1 ? command.createRecursive(name) : command;
    }

    @Override
    public void registerCommands(Object... objects) {
        for (Object object : objects) {
            for (Method method : object.getClass().getDeclaredMethods()) {
                Command command = method.getAnnotation(Command.class);
                if (command != null) {
                    registerCommand(new CommandInfo(command, object.getClass()), new ProxyCommandExecutor(this, method, object));
                    continue;
                }

                Completer completer = method.getAnnotation(Completer.class);
                if (completer != null) {
                    registerCompleter(completer.name(), new ProxyCompleterExecutor(method, object));
                }
            }
            if (Listener.class.isAssignableFrom(object.getClass()))
                BungeeCord.getInstance().getPluginManager().registerListener(getPlugin(), (Listener) object);
        }
        publish();
    }

    @Override
    public void registerCommand(CommandInfo commandInfo, CommandExecutor<CommandSender> commandExecutor) {
        ProxyCommand recursive = getCommand(commandInfo.getName());
        if (recursive == null) {
            return;
        }

        recursive.initCommand(commandInfo, commandExecutor);

        getCommandInfoList().add(commandInfo);

        if (recursive.getPosition() == 0) {
            ProxyServer.getInstance().getPluginManager().registerCommand(
                    plugin,
                    recursive
            );
        }
    }

    @Override
    public void registerCompleter(String name, CompleterExecutor<CommandSender> completerExecutor) {
        ProxyCommand recursive = getCommand(name);
        if (recursive == null) {
            return;
        }

        recursive.initCompleter(completerExecutor);
    }

    @Override
    public boolean unregisterCommand(String name) {
        final ProxyCommand command = commandMap.remove(name);
        if (command == null) return false;

        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
        getCommandInfoList().remove(command.getCommandInfo());
        ProxyServer.getInstance().getScheduler().runAsync(ProxyGame.getInstance(), this::publish);
        return true;
    }

    private void publish() {
        try (Jedis redis = Constants.getRedis().getResource(Redis.SERVER_CACHE)) {
            CommandList commandList = new CommandList(getCommandInfoList());
            redis.set("proxy.commands", Constants.GSON.toJson(commandList));
        }
    }

}
