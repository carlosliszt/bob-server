/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerFinderCommand implements ProxyInterface {

    private final Pattern addressPattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    @Command(name = "playerfinder", usage = "playerfinder <target>", rank = Rank.EVENT_MOD, async = true)
    public void handleCommand(Context<CommandSender> context, String target) {

        StringBuilder queryBuilder = new StringBuilder("SELECT accounts.username, accounts.banned FROM `accounts` accounts");

        if (addressPattern.matcher(target).matches()) {
            queryBuilder.append(" WHERE address='").append(target).append("'");
        } else {

            UUID uuid = parse(target);
            String address = address(uuid);

            if (address == null || address.equals("...")) {
                context.info("target.not_found");
                return;
            }

            queryBuilder.append(" WHERE address='").append(address).append("'");
            queryBuilder.append(" AND `unique_id` != '").append(uuid).append("'");
        }

        List<PlayerSet> playerSets = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(queryBuilder.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                playerSets.add(new PlayerSet(resultSet.getString("username"), resultSet.getBoolean("banned")));
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException ignored) {
        }

        if (playerSets.isEmpty()) {
            context.info("command.playerfinder.no_results", target);
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        int current = 0, max = playerSets.size();

        stringBuilder.append("§b").append(max).append(" ").append(max > 1 ? "jogadores encontrados: " : "jogador encontrado: ");

        for (PlayerSet playerSet : playerSets) {
            current++;
            stringBuilder.append((playerSet.isBanned() ? ChatColor.RED : ChatColor.GREEN)).append(playerSet.getName());
            if (current != max)
                stringBuilder.append("§7, ");
        }
        context.sendMessage(stringBuilder.toString());
    }

    public UUID parse(String target) {
        ProxiedPlayer proxiedPlayer = getPlayer(target);

        UUID uuid;

        if (proxiedPlayer == null) {
            uuid = Constants.getMojangAPI().getUniqueId(target);

            if (uuid == null)
                uuid = Constants.getCrackedUniqueId(target);
        } else
            uuid = proxiedPlayer.getUniqueId();
        return uuid;
    }

    @Completer(name = "playerfinder")
    public List<String> tabComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

    public String address(UUID uuid) {
        String address = "...";
        Account account = Account.fetch(uuid);
        if (account != null)
            address = account.getData(Columns.ADDRESS).getAsString();
        else {
            try {
                String ADDRESS_QUERY = "SELECT accounts.address FROM `accounts` WHERE accounts.unique_id='%s'";
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(String.format(ADDRESS_QUERY, uuid));
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    address = resultSet.getString("address");
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException ignored) {
            }
        }
        return address;
    }

    @AllArgsConstructor
    @Getter
    private static class PlayerSet {

        private final String name;
        private final boolean banned;

    }

}
