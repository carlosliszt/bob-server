/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command;

import com.google.gson.GsonBuilder;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.command.command.BukkitCommand;
import com.minecraft.core.bukkit.util.command.executor.BukkitAsynchronouslyExecutor;
import com.minecraft.core.bukkit.util.command.executor.BukkitCommandExecutor;
import com.minecraft.core.bukkit.util.command.executor.BukkitCompleterExecutor;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.command.CommandFrame;
import com.minecraft.core.command.CommandList;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.argument.AdapterMap;
import com.minecraft.core.command.argument.eval.MethodEvaluator;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.exception.CommandException;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.executor.CompleterExecutor;
import com.minecraft.core.database.redis.Redis;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * The BukkitFrame is the core of the framework,
 * it registers the commands, adapters {@link AdapterMap}
 */
@Getter
public final class BukkitFrame implements CommandFrame<Plugin, CommandSender, BukkitCommand> {

    private final Plugin plugin;
    private final AdapterMap adapterMap;

    private final Map<String, BukkitCommand> commandMap;
    private final Set<CommandInfo> proxyCommands;
    private final Set<BukkitCommand> bukkitCommandList;
    private final MethodEvaluator methodEvaluator;

    @Getter(AccessLevel.PRIVATE)
    private final CommandMap bukkitCommandMap;

    @Setter
    private Executor executor;

    /**
     * Creates a new BukkitFrame with the AdapterMap provided.
     *
     * @param plugin     Plugin
     * @param adapterMap AdapterMap
     */
    public BukkitFrame(Plugin plugin, AdapterMap adapterMap) {
        this.plugin = plugin;

        this.adapterMap = adapterMap;

        this.bukkitCommandList = new HashSet<>();

        this.proxyCommands = resolveProxyCommands();

        this.commandMap = new HashMap<>();
        this.methodEvaluator = new MethodEvaluator(adapterMap);

        this.executor = new BukkitAsynchronouslyExecutor(plugin);

        try {
            final Server server = Bukkit.getServer();
            final Method mapMethod = server.getClass().getMethod("getCommandMap");
            this.bukkitCommandMap = (CommandMap) mapMethod.invoke(server);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new CommandException(exception);
        }
    }


    /**
     * Creates a new BukkitFrame with the default AdapterMap. <p>If the registerDefault is true,
     * it registers the default adapters for Bukkit.
     *
     * @param plugin          Plugin
     * @param registerDefault Boolean
     */
    public BukkitFrame(Plugin plugin, boolean registerDefault) {
        this(plugin, new AdapterMap(registerDefault));

        if (registerDefault) {
            registerAdapter(Player.class, this::getPlayer);
        }
    }

    private Player getPlayer(String text) {
        Player target;

        if (Constants.isUniqueId(text))
            target = Bukkit.getServer().getPlayer(UUID.fromString(text));
        else
            target = Bukkit.getServer().getPlayer(text);
        return target;
    }

    /**
     * Creates a new BukkitFrame with the default AdapterMap
     * and default Bukkit adapters.
     *
     * @param plugin Plugin
     */
    public BukkitFrame(Plugin plugin) {
        this(plugin, true);
    }

    /**
     * Get a command by their name in the CommandMap
     *
     * @param name String
     * @return BukkitCommand
     */
    @Override
    public BukkitCommand getCommand(String name) {
        int index = name.indexOf('.');
        String nextSubCommand = name;
        if (index != -1) {
            nextSubCommand = name.substring(0, index);
        }

        BukkitCommand subCommand = commandMap.get(nextSubCommand);
        if (subCommand == null) {
            subCommand = new BukkitCommand(this, nextSubCommand, 0);
            commandMap.put(nextSubCommand, subCommand);
        }

        return subCommand.createRecursive(name);
    }

    /**
     * Registers multiple command objects into the CommandMap.
     *
     * @param objects Object...
     */
    @Override
    public void registerCommands(Object... objects) {
        int commandsCount = 0, completerCount = 0;
        for (Object object : objects) {
            for (Method method : object.getClass().getDeclaredMethods()) {
                final Command command = method.getAnnotation(Command.class);
                if (command != null) {
                    registerCommand(new CommandInfo(command, object.getClass()), new BukkitCommandExecutor(this, method, object));
                    commandsCount++;
                    continue;
                }

                Completer completer = method.getAnnotation(Completer.class);
                if (completer != null) {
                    registerCompleter(completer.name(), new BukkitCompleterExecutor(method, object));
                    completerCount++;
                }
            }

            if (Listener.class.isAssignableFrom(object.getClass())) {
                if (!(object instanceof DynamicListener))
                    Bukkit.getPluginManager().registerEvents((Listener) object, getPlugin());
            }
        }

        System.out.println("Loaded " + commandsCount + " commands and " + completerCount + " completers.");
    }

    /**
     * Registers a command into the CommandMap
     *
     * @param commandInfo     CommandInfo
     * @param commandExecutor CommandExecutor
     */
    @Override
    public void registerCommand(CommandInfo commandInfo, CommandExecutor<CommandSender> commandExecutor) {
        final BukkitCommand recursive = getCommand(commandInfo.getName());
        if (recursive == null) {
            return;
        }

        recursive.initCommand(commandInfo, commandExecutor);

        getBukkitCommandList().add(recursive);

        if (recursive.getPosition() == 0) {
            bukkitCommandMap.register(plugin.getName(), recursive);
        }
    }

    @Override
    public void registerCompleter(String name, CompleterExecutor<CommandSender> completerExecutor) {
        final BukkitCommand recursive = getCommand(name);
        if (recursive == null) {
            return;
        }

        recursive.initCompleter(completerExecutor);
    }

    /**
     * Unregisters a command from the CommandMap by
     * the Command name.
     *
     * <p>Returns a boolean that depends if the
     * operation was successful</p>
     *
     * @param name String | Command name
     * @return boolean
     */
    @Override
    public boolean unregisterCommand(String name) {
        final BukkitCommand command = commandMap.remove(name);
        getBukkitCommandList().remove(command);
        return command != null && command.unregister(bukkitCommandMap);
    }

    private Set<CommandInfo> resolveProxyCommands() {
        Set<CommandInfo> proxy = new HashSet<>();
        try (Jedis jedis = Constants.getRedis().getResource(Redis.SERVER_CACHE)) {
            if (!jedis.exists("proxy.commands")) {
                return proxy;
            }
            String JSON = jedis.get("proxy.commands");
            CommandList commandList = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                    .fromJson(JSON, CommandList.class);
            if (commandList != null && commandList.getCommands() != null) {
                proxy.addAll(commandList.getCommands());
            }
        }
        return proxy;
    }
    public List<BukkitCommand> getCommands(Class<?> clzz) {
        return getBukkitCommandList().stream().filter(c -> c.getCommandInfo().getHolder().getName().equals(clzz.getName())).collect(Collectors.toList());
    }

    public void unregisterCommands(String... commands) {
        try {
            Field f1 = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f1.setAccessible(true);
            CommandMap commandMap = (CommandMap) f1.get(Bukkit.getServer());
            Field f2 = commandMap.getClass().getDeclaredField("knownCommands");
            f2.setAccessible(true);
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) f2.get(commandMap);
            for (String command : commands) {
                if (knownCommands.containsKey(command)) {
                    knownCommands.remove(command);
                    unregisterCommand(command);
                    List<String> aliases = new ArrayList<>();
                    for (String key : knownCommands.keySet()) {
                        if (!key.contains(":"))
                            continue;
                        String substr = key.substring(key.indexOf(":") + 1);
                        if (substr.equalsIgnoreCase(command)) {
                            aliases.add(key);
                        }
                    }
                    for (String alias : aliases) {
                        knownCommands.remove(alias);
                        unregisterCommand(alias);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
