/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.command.CommandHolder;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.executor.CommandExecutor;
import com.minecraft.core.command.executor.CompleterExecutor;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.util.ArrayUtil;
import com.minecraft.core.command.util.StringUtil;
import com.minecraft.core.proxy.util.command.ProxyFrame;
import com.minecraft.core.proxy.util.command.executor.ProxyCommandExecutor;
import com.minecraft.core.proxy.util.command.platform.ProxyPlatformValidator;
import com.minecraft.core.translation.Language;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.logging.Level;

@Getter
public class ProxyCommand extends Command implements CommandHolder<CommandSender, ProxyChildCommand>, TabExecutor {

    private final ProxyFrame frame;

    private CommandInfo commandInfo;

    private final int position;

    private CommandExecutor<CommandSender> commandExecutor;
    private CompleterExecutor<CommandSender> completerExecutor;

    private final List<ProxyChildCommand> childCommandList = new LinkedList<>();

    private String permission;
    private String[] aliases;

    @Setter
    private String usage;

    private final String description = "Not provided";

    public ProxyCommand(ProxyFrame frame, String name, int position) {
        super(name);
        this.frame = frame;
        this.position = position;
    }

    public final void initCommand(CommandInfo commandInfo, CommandExecutor<CommandSender> commandExecutor) {
        if (this.commandInfo != null) {
            System.out.println("Command " + getName() + " already initialized, ignoring...");
            return;
        }

        this.commandInfo = commandInfo;
        this.commandExecutor = commandExecutor;

        this.aliases = commandInfo.getAliases();

        final String usage = commandInfo.getUsage();
        if (!StringUtil.isEmpty(usage)) {
            setUsage(usage);
        } else if (commandExecutor instanceof ProxyCommandExecutor) {
            setUsage(((ProxyCommandExecutor) commandExecutor).getEvaluator().buildUsage(getFancyName()));
        }

        if (commandExecutor instanceof ProxyCommandExecutor) {
            ((ProxyCommandExecutor) commandExecutor).setCommand(this);
        }

    }

    public final void initCompleter(CompleterExecutor<CommandSender> completerExecutor) {
        if (this.completerExecutor != null) {
            System.out.println("Completer " + getName() + " already initialized, ignoring...");
            return;
        }

        this.completerExecutor = completerExecutor;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (sender instanceof ProxiedPlayer) {

            ProxyServer.getInstance().getLogger().log(Level.INFO, sender.getName() + " executed: /" + commandInfo.getName() + " " + String.join(" ", args));

            ProxiedPlayer player = (ProxiedPlayer) sender;
            Account account = Account.fetch(player.getUniqueId());

            if (account == null) {
                ((ProxiedPlayer) sender).disconnect(TextComponent.fromLegacyText(Language.PORTUGUESE.translate("unexpected_error")));
                return;
            }

            if (account.getRank().getId() < commandInfo.getRank().getId() && !account.hasPermission("command." + commandInfo.getName())) {
                sender.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate(MessageType.NO_PERMISSION.getMessageKey())));
                return;
            }
        }

        if (!ProxyPlatformValidator.INSTANCE.validate(commandInfo.getPlatform(), sender)) {
            Account account = Account.fetch((sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : Constants.CONSOLE_UUID));
            sender.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate(MessageType.INCORRECT_TARGET.getMessageKey(), commandInfo.getPlatform().name().toLowerCase())));
            return;
        }

        if (args.length > 0) {
            ProxyChildCommand command = getChildCommand(args[0]);
            if (command != null) {
                command.execute(sender, ArrayUtil.copyOfRange(args, 1, args.length));
                return;
            }
        }

        if (commandExecutor == null) {
            return;
        }

        if (commandInfo.isAsync() && frame.getExecutor() != null) {
            frame.getExecutor().execute(() -> commandExecutor.execute(new ProxyContext((sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : Constants.CONSOLE_UUID), sender, commandInfo.getName(), ProxyPlatformValidator.INSTANCE.fromSender(sender), args, frame, this)));
            return;
        }

        commandExecutor.execute(new ProxyContext((sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : Constants.CONSOLE_UUID), sender, commandInfo.getName(), ProxyPlatformValidator.INSTANCE.fromSender(sender), args, frame, this));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Account account = Account.fetch(player.getUniqueId());

            if (account == null) {
                ((ProxiedPlayer) sender).disconnect(TextComponent.fromLegacyText(Language.PORTUGUESE.translate("unexpected_error")));
                return Collections.emptyList();
            }

            if (account.getRank().getId() < commandInfo.getRank().getId())
                return Collections.emptyList();
        }

        if (completerExecutor != null) {
            return completerExecutor.execute(new ProxyContext((sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : Constants.CONSOLE_UUID), sender, "undefined",
                    ProxyPlatformValidator.INSTANCE.fromSender(sender),
                    args,
                    frame,
                    this
            ));
        }

        if (childCommandList.size() != 0 && args.length != 0) {
            List<String> matchedChildCommands = new ArrayList<>();

            for (ProxyChildCommand command : childCommandList) {
                if (StringUtil.startsWithIgnoreCase(command.getName(), args[args.length - 1])
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

    @Override
    public ProxyChildCommand getChildCommand(String name) {
        for (ProxyChildCommand childCommand : childCommandList) {
            if (childCommand.equals(name)) return childCommand;
        }

        return null;
    }

    public ProxyCommand createRecursive(String name) {
        int position = getPosition() + StringUtil.countMatches(name, ".");
        if (position == getPosition()) {
            return this;
        }

        String recursive = name.substring(name.indexOf('.') + 1);

        int index = recursive.indexOf('.');
        String childCommandName = index != -1 ? recursive.substring(0, index) : recursive;

        ProxyChildCommand childCommand = getChildCommand(childCommandName);
        if (childCommand == null) {
            childCommand = new ProxyChildCommand(frame, childCommandName, this);
            getChildCommandList().add(childCommand);
        }

        return childCommand.createRecursive(recursive);
    }

    @Override
    public List<String> getAliasesList() {
        return Arrays.asList(getAliases());
    }

    @Override
    public String getFancyName() {
        return getName();
    }

    protected boolean testPermissionSilent(CommandSender target) {
        String permission = getPermission();
        if ((permission == null) || (permission.length() == 0)) {
            return true;
        }

        for (String p : permission.split(";")) {
            if (target.hasPermission(p)) {
                return true;
            }
        }

        return false;
    }
}
