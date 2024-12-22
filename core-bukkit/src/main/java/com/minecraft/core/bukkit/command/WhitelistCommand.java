/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.whitelist.Whitelist;
import com.minecraft.core.bukkit.util.whitelist.WhitelistData;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.database.HttpRequest;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WhitelistCommand implements BukkitInterface {

    private final Whitelist whitelist = BukkitGame.getEngine().getWhitelist();

    @Command(name = "whitelist", rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<CommandSender> context) {
        String[] args = context.getArgs();

        if (args.length == 0) {
            Argument.INFO.getExecutor().execute(context, null);
        } else {
            Argument argument = Argument.get(context.getArg(0));

            if (argument == null) {
                context.info("no_function", args[0]);
                return;
            }

            if (args.length < argument.getMinimumArgs()) {
                Argument.INFO.getExecutor().execute(context, null);
                return;
            }
            argument.getExecutor().execute(context, whitelist);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Argument {

        INFO(null, 0, (context, whitelist) -> {
            context.sendMessage("§cUso de /whitelist:");
            context.sendMessage("§c* /whitelist on");
            context.sendMessage("§c* /whitelist off");
            context.sendMessage("§c* /whitelist list");
            context.sendMessage("§c* /whitelist add <username>");
            context.sendMessage("§c* /whitelist remove <username>");
            context.sendMessage("§c* /whitelist clear");
            context.sendMessage("§c* /whitelist import <id>");
        }),

        ON("on", 1, (context, whitelist) -> {

            if (whitelist.isActive()) {
                context.info("command.whitelist.already_on");
                return;
            }

            whitelist.setActive(true);
            context.info("command.whitelist.enabled_succesful");
        }),

        OFF("off", 1, (context, whitelist) -> {
            if (!whitelist.isActive()) {
                context.info("command.whitelist.already_off");
                return;
            }

            whitelist.setActive(false);
            context.info("command.whitelist.disabled_succesful");
        }),

        LIST("list", 1, (context, whitelist) -> {

            if (whitelist.getWhitelistedPlayers().isEmpty()) {
                context.info("command.whitelist.list_empty");
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            List<WhitelistData> players = new ArrayList<>(whitelist.getWhitelistedPlayers());

            for (int i = 0; i < players.size(); i++) {
                WhitelistData whitelistData = players.get(i);

                stringBuilder.append("§e").append(whitelistData.getName());

                if (i != players.size() - 1)
                    stringBuilder.append("§f, ");
            }

            players.clear();

            context.info("command.whitelist.list_players", stringBuilder.toString());
        }),

        ADD("add", 2, (context, whitelist) -> {

            if (whitelist.isWhitelisted(context.getArg(1))) {
                context.info("command.whitelist.target_already_whitelisted");
                return;
            }

            String SQL = "SELECT `unique_id`, `username` FROM accounts WHERE username='" + context.getArg(1) + "' LIMIT 1;";

            Executor.async(() -> {
                try {
                    PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(SQL);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (!resultSet.next()) {
                        context.info("target.not_found");
                        preparedStatement.close();
                        resultSet.close();
                        return;
                    }

                    String name = resultSet.getString("username");

                    if (name.equals("...")) {
                        context.info("target.not_found");
                        preparedStatement.close();
                        resultSet.close();
                        return;
                    }

                    UUID uuid = UUID.fromString(resultSet.getString("unique_id"));

                    preparedStatement.close();
                    resultSet.close();

                    whitelist.getWhitelistedPlayers().add(new WhitelistData(name, uuid, System.currentTimeMillis()));
                    context.info("command.whitelist.added_succesful", name);
                } catch (SQLException e) {
                    context.info("unexpected_error");
                    e.printStackTrace();
                }
            });
        }),

        REMOVE("remove", 2, (context, whitelist) -> {

            String name = context.getArg(1);

            if (!whitelist.isWhitelisted(name)) {
                context.info("command.whitelist.target_not_whitelisted", name);
                return;
            }

            whitelist.getWhitelistedPlayers().remove(whitelist.getData(name));
            context.info("command.whitelist.removed_succesful", name);
        }),

        CLEAR("clear", 1, (context, whitelist) -> {

            if (whitelist.getWhitelistedPlayers().isEmpty()) {
                context.info("command.whitelist.list_empty");
                return;
            }

            context.info("command.whitelist.cleared_succesful", whitelist.getWhitelistedPlayers().size());
            whitelist.getWhitelistedPlayers().clear();
        }),

        RANK("rank", 0, (context, whitelist) -> {
            if (context.argsCount() == 1) {
                context.sendMessage("§bwhitelist_rank §e= §6" + whitelist.getMinimumRank().getDefaultTag().getName());
            } else {
                Rank rank = Rank.fromString(context.getArg(1));

                if (rank == null || rank == Rank.MEMBER) {
                    context.info("object.not_found", "Rank");
                    return;
                }
                context.sendMessage("§aVocê alterou o whitelist_rank para " + rank.getDefaultTag().getName());
                whitelist.setMinimumRank(rank);
            }
        }),

        IMPORT("import", 2, (context, whitelist) -> {

            String URL = "http://localhost/raw/";
            String ID = context.getArg(1);

            context.info("command.whitelist.import_processing");

            Executor.async(() -> {

                AtomicInteger error = new AtomicInteger();

                List<String> readNames = new ArrayList<>();
                HttpRequest request = HttpRequest.get(URL + ID).connectTimeout(5000).readTimeout(5000).userAgent("Administrator/1.0.0").acceptJson();

                if (request.ok()) {
                    request.bufferedReader().lines().forEach(name -> {
                        if (!Constants.isValid(name)) {
                            context.info("command.whitelist.import_failed", name);
                            error.getAndIncrement();
                            return;
                        }
                        readNames.add(name);
                    });

                    request.disconnect();

                    if (readNames.isEmpty())
                        return;

                    StringBuilder SQL = new StringBuilder();
                    SQL.append("SELECT `unique_id`, `username` FROM accounts WHERE username='").append(readNames.get(0)).append("'");
                    if (readNames.size() != 1) {
                        for (int i = 1; i < readNames.size(); i++)
                            SQL.append(" OR username='").append(readNames.get(i)).append("'");
                    }

                    SQL.append(" LIMIT ").append(readNames.size()).append(";");
                    try {

                        PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(SQL.toString());
                        ResultSet resultSet = preparedStatement.executeQuery();

                        int added = 0;

                        while (resultSet.next()) {
                            String name = resultSet.getString("username");

                            if (name.equals("...")) {
                                context.info("command.whitelist.import_failed", name);
                                return;
                            }

                            UUID uuid = UUID.fromString(resultSet.getString("unique_id"));

                            whitelist.getWhitelistedPlayers().add(new WhitelistData(name, uuid, System.currentTimeMillis()));
                            context.info("command.whitelist.added_succesful", name);
                            added++;
                        }
                        context.info("command.whitelist.import_feedback", added, error);
                        preparedStatement.close();
                        resultSet.close();
                    } catch (SQLException e) {
                        context.info("unexpected_error");
                        e.printStackTrace();
                    }
                } else {
                    context.info("command.whitelist.import_read_error", URL + ID);
                }
            });
        });

        private final String key;
        private final int minimumArgs;
        private final Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(argument -> argument.getKey() != null && argument.getKey().equalsIgnoreCase(key)).findFirst().orElse(INFO);
        }

        protected interface Executor {
            void execute(Context<CommandSender> context, Whitelist whitelist);

            static void async(Runnable runnable) {
                Bukkit.getScheduler().runTaskAsynchronously(BukkitGame.getEngine(), runnable);
            }
        }
    }

    @Completer(name = "whitelist")
    public List<String> handleComplete(Context<CommandSender> context) {
        String[] args = context.getArgs();
        if (args.length == 1)
            return Arrays.stream(Argument.values()).filter(arg -> arg.getKey() != null && startsWith(arg.getKey(), args[0])).map(Argument::getKey).collect(Collectors.toList());
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove"))
                return whitelist.getWhitelistedPlayers().stream().filter(whitelist -> startsWith(whitelist.getName(), args[1])).map(WhitelistData::getName).collect(Collectors.toList());
            else if (args[0].equalsIgnoreCase("rank"))
                return Arrays.stream(Rank.values()).filter(rank -> rank != Rank.MEMBER && startsWith(rank.getDisplayName(), args[1]) && context.getAccount().hasPermission(rank)).map(r -> r.getDisplayName().toLowerCase()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}