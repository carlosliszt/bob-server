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
import com.minecraft.core.bukkit.util.variable.converter.VariableConverter;
import com.minecraft.core.bukkit.util.variable.loader.VariableLoader;
import com.minecraft.core.bukkit.util.variable.object.ExecutableVariable;
import com.minecraft.core.bukkit.util.variable.object.SimpleVariable;
import com.minecraft.core.bukkit.util.variable.object.parameter.NoParameters;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariableCommand implements BukkitInterface {

    private static final VariableLoader variableLoader = BukkitGame.getEngine().getVariableLoader();

    public VariableCommand() {
        BukkitGame.getEngine().getBukkitFrame().registerAdapter(SimpleVariable.class, variableLoader::getVariable);
    }

    @Command(name = "var", aliases = {"variable"}, platform = Platform.BOTH, rank = Rank.STREAMER_PLUS, usage = "{label} <variable> [value]")
    public void handleCommand(Context<CommandSender> context, SimpleVariable variable) throws IllegalAccessException, InvocationTargetException {

        if (context.getArg(0).equalsIgnoreCase("list")) {
            context.sendMessage("§7§oLista de variáveis:");
            for (SimpleVariable simpleVariable : variableLoader.getVariables()) {

                if (!context.getAccount().hasPermission(simpleVariable.getDefaultRank()))
                    continue;

                context.sendMessage("§b" + simpleVariable.getName() + " §e= §6" + simpleVariable.getValue().toString().toLowerCase());
            }
            return;
        }

        if (variable == null) {
            context.info("object.not_found", "Variable");
            return;
        }

        if (!variable.isActive()) {
            context.info("object.not_found", "Variable");
            return;
        }

        if (context.argsCount() == 1) {
            if (!variable.getParameter().equals(NoParameters.class)) {
                context.info("command.account.argument.flag.value", variable.getName(), variable.getValue());
            } else {

                if (!context.isPlayer() && !context.getAccount().hasPermission(variable.getDefaultRank())) {
                    context.info(MessageType.NO_PERMISSION.getMessageKey());
                    return;
                }

                ((ExecutableVariable) variable).getExecutor().invoke(variable.getVariableStorage());
                context.info("command.variable.successful");
            }
        } else if (context.argsCount() >= 2) {

            if (!context.getAccount().hasPermission(variable.getDefaultRank())) {
                context.info(MessageType.NO_PERMISSION.getMessageKey());
                return;
            }

            String valueString = String.join(" ", Arrays.copyOfRange(context.getArgs(), 1, context.argsCount()));

            Object varValue = VariableConverter.convertToObject(Bukkit.getPlayer(context.getUniqueId()), variable.getParameter(), valueString);

            if (varValue == null) {
                context.info("command.variable.failed_to_convert");
                return;
            }

            if (!variable.validate(varValue)) {
                context.info("command.variable.invalidated_value");
                return;
            }

            if (varValue instanceof Integer) {
                if ((int) varValue < 0) {
                    context.info("command.number_negative");
                    return;
                }
            }

            if (variable instanceof ExecutableVariable) {
                if (variable.getParameter().equals(NoParameters.class))
                    ((ExecutableVariable) variable).getExecutor().invoke(variable.getVariableStorage());
                else
                    ((ExecutableVariable) variable).getExecutor().invoke(variable.getVariableStorage(), varValue);
                context.info("command.variable.successful");

            } else {

                boolean changed = !varValue.equals(variable.getValue());

                variable.setValue(varValue);
                final String valueToName = VariableConverter.convertCurrentValueToName(varValue);

                log(variable.getName(), valueToName);

                if (changed && variable.isAnnouce() && variable.getParameter() == boolean.class)
                    broadcast("var." + variable.getName() + (valueToName.equals("true") ? ".on" : ".off"));
            }
        }
    }

    @Completer(name = "var")
    public List<String> complete(Context<Player> context) {
        String[] args = context.getArgs();
        if (args.length == 1)
            return variableLoader.getVariables().stream().filter(var -> var.isActive() && context.getAccount().hasPermission(var.getDefaultRank()) && startsWith(var.getName(), args[0])).map(SimpleVariable::getName).sorted(Comparator.comparingInt(String::length)).collect(Collectors.toList());
        else if (args.length == 2) {

            SimpleVariable simpleVariable = variableLoader.getVariable(args[0]);

            if (simpleVariable == null || simpleVariable instanceof ExecutableVariable || !context.getAccount().hasPermission(simpleVariable.getDefaultRank()))
                return Collections.emptyList();

            Class parameter = simpleVariable.getParameter();

            if (parameter.equals(Boolean.class) || parameter.equals(boolean.class)) {
                return Stream.of("true", "false", "on", "off").filter(c -> startsWith(c, args[1])).collect(Collectors.toList());
            } else if (parameter.isEnum()) {
                Object[] objects = parameter.getEnumConstants();
                return Stream.of(objects).map(obj -> Enum.valueOf(parameter, obj.toString()).name()).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    public void log(String key, String value) {
        List<Account> receivers = new ArrayList<>(Constants.getAccountStorage().getAccounts());
        receivers.removeIf(accounts -> accounts.getRank().getId() < Rank.STREAMER_PLUS.getId() || accounts.getProperty("stafflog", false).getAsBoolean());
        receivers.forEach(receiver -> {
            Player staff = Bukkit.getPlayer(receiver.getUniqueId());
            if (staff == null)
                return;
            staff.sendMessage(receiver.getLanguage().translate("command.variable.value_changed_to", key, value));
        });
        receivers.clear();
    }
}
