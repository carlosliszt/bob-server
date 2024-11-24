/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.command.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.bukkit.util.command.executor.BukkitCommandExecutor;
import com.minecraft.core.bukkit.util.command.platform.BukkitPlatformValidator;
import com.minecraft.core.command.command.CommandHolder;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.executor.CompleterExecutor;
import com.minecraft.core.command.message.MessageType;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The BukkitCommand is the main implementation of the
 * AbstractCommand, it contains the main information about
 * that command {@link CommandInfo} position and Child commands {@link BukkitChildCommand}
 */
@Getter
public class BukkitCommand extends Command implements CommandHolder<CommandSender, BukkitChildCommand> {

    private final BukkitFrame frame;

    private CommandInfo commandInfo;

    private final int position;

    private CommandExecutor<CommandSender> commandExecutor;
    private CompleterExecutor<CommandSender> completerExecutor;

    private final List<BukkitChildCommand> childCommandList;

    /**
     * Creates a new BukkitCommand with the name provided.
     *
     * @param frame    BukkitFrame
     * @param name     String
     * @param position Integer
     */
    public BukkitCommand(BukkitFrame frame, String name, int position) {
        super(name);

        this.frame = frame;
        this.position = position;

        this.childCommandList = new ArrayList<>();
    }

    /**
     * Initializes the command when the server is started.
     * <p>If you try to register the same commands multiple times, it throws
     * a CommandException</p>
     *
     * @param commandInfo     CommandInfo
     * @param commandExecutor CommandExecutor
     */
    public final void initCommand(CommandInfo commandInfo, CommandExecutor<CommandSender> commandExecutor) {
        if (this.commandInfo != null) {
            System.out.println("Command " + getName() + " already initialized, ignoring...");
            return;
        }

        this.commandInfo = commandInfo;
        this.commandExecutor = commandExecutor;

        setAliases(Arrays.asList(commandInfo.getAliases()));

        final String usage = commandInfo.getUsage();
        if (StringUtils.isNotEmpty(usage)) {
            setUsage(usage);
        } else if (commandExecutor instanceof BukkitCommandExecutor) {
            setUsage(((BukkitCommandExecutor) commandExecutor).getEvaluator().buildUsage(getFancyName()));
        }

        if ((StringUtils.isNotEmpty(commandInfo.getDescription()))) {
            setDescription(commandInfo.getDescription());
        }

        if (commandExecutor instanceof BukkitCommandExecutor) {
            ((BukkitCommandExecutor) commandExecutor).setCommand(this);
        }
    }

    public final void initCompleter(CompleterExecutor<CommandSender> completerExecutor) {
        if (this.completerExecutor != null) {
            System.out.println("Completer " + getName() + " already initialized, ignoring...");
            return;
        }

        this.completerExecutor = completerExecutor;
    }

    /**
     * Get the Child command from this by the name, if it's not register it
     * will return null.
     *
     * @param name String
     * @return BukkitChildCommand
     */
    @Override
    public BukkitChildCommand getChildCommand(String name) {
        for (BukkitChildCommand childCommand : childCommandList) {
            if (childCommand.equals(name)) return childCommand;
        }

        return null;
    }

    @Override
    public String getFancyName() {
        return getName();
    }

    /**
     * Executes the command with the provided label and arguments.
     * <p>If returns false, it wasn't able to execute</p>
     *
     * @param sender       CommandSender
     * @param commandLabel String
     * @param args         String[]
     * @return boolean
     */
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!frame.getPlugin().isEnabled()) {
            return false;
        }

        if (commandInfo != null && !BukkitPlatformValidator.INSTANCE.validate(commandInfo.getPlatform(), sender)) {
            Account account = Account.fetch((sender instanceof ConsoleCommandSender ? Constants.CONSOLE_UUID : ((Player) sender).getUniqueId()));
            sender.sendMessage(account.getLanguage().translate(MessageType.INCORRECT_TARGET.getMessageKey(), commandInfo.getPlatform().name().toLowerCase()));
            return false;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Account account = Account.fetch(player.getUniqueId());

            if (account.getRank().getId() < commandInfo.getRank().getId() && !account.hasPermission("command." + commandInfo.getName())) {
                sender.sendMessage(account.getLanguage().translate(MessageType.NO_PERMISSION.getMessageKey()));
                return false;
            }
        }

        if (args.length > 0) {
            BukkitChildCommand command = getChildCommand(args[0]);
            if (command != null) {
                final String label = commandLabel + " " + args[0];
                return command.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        if (commandExecutor == null) {
            return false;
        }

        final BukkitContext context = new BukkitContext((sender instanceof Player ? ((Player) sender).getUniqueId() : Constants.CONSOLE_UUID), commandLabel, sender, com.minecraft.core.bukkit.util.command.platform.BukkitPlatformValidator.INSTANCE.fromSender(sender), args, frame, this);

        if (commandInfo.isAsync() && frame.getExecutor() != null) {
            frame.getExecutor().execute(() -> commandExecutor.execute(context));
            return false;
        }

        return commandExecutor.execute(context);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Account account = Account.fetch(player.getUniqueId());

            if (account.getRank().getId() < commandInfo.getRank().getId())
                return Collections.emptyList();
        }

        if (completerExecutor != null) {
            return completerExecutor.execute(new BukkitContext((sender instanceof Player ? ((Player) sender).getUniqueId() : Constants.CONSOLE_UUID),
                    alias,
                    sender,
                    com.minecraft.core.bukkit.util.command.platform.BukkitPlatformValidator.INSTANCE.fromSender(sender),
                    args,
                    frame,
                    this
            ));
        }

        if (childCommandList.size() != 0 && args.length != 0) {
            List<String> matchedChildCommands = new ArrayList<>();

            for (BukkitChildCommand command : childCommandList) {
                if (StringUtils.startsWithIgnoreCase(command.getName(), args[args.length - 1])
                        && command.testPermissionSilent(sender)) {
                    matchedChildCommands.add(command.getName());
                }
            }

            if (matchedChildCommands.size() != 0) {
                matchedChildCommands.sort(String.CASE_INSENSITIVE_ORDER);
                return matchedChildCommands;
            }
        }

        return Collections.emptyList();
    }

    public BukkitCommand createRecursive(String name) {
        int position = getPosition() + StringUtils.countMatches(name, ".");
        if (position == getPosition()) {
            return this;
        }

        String subName = name.substring(Math.max(name.indexOf('.') + 1, 0));

        int index = subName.indexOf('.');
        String nextSubCommand = subName;
        if (index != -1) {
            nextSubCommand = subName.substring(0, index);
        }

        BukkitChildCommand childCommand = getChildCommand(nextSubCommand);

        if (childCommand == null) {
            childCommand = new BukkitChildCommand(frame, nextSubCommand, this);
            getChildCommandList().add(childCommand);
        }

        return childCommand.createRecursive(subName);
    }

    @Override
    public List<String> getAliasesList() {
        return getAliases();
    }
}
