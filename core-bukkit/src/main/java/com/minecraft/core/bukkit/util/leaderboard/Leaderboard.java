/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.leaderboard;


import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardData;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Leaderboard implements Listener, BukkitInterface {

    private LeaderboardHandler handler;
    private volatile List<LeaderboardData> values = new ArrayList<>();
    private final Columns statistic;
    private final LeaderboardUpdate update;
    private final int limit;
    private final LeaderboardType leaderboardType;
    private final Columns[] load;
    private boolean registered;

    public Leaderboard(Columns statistic, @NonNull LeaderboardUpdate update, LeaderboardType type, int limit, Columns... load) {
        this.statistic = statistic;
        this.update = update;
        this.leaderboardType = type;
        this.limit = limit;
        this.load = load;
    }

    public Leaderboard query() {

        System.out.println("Updating " + this + ".");

        if (!registered && update != LeaderboardUpdate.NEVER) {
            this.registered = true;
            Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());

            BukkitGame.getEngine().getBukkitFrame().registerCommand(CommandInfo.builder().holder(getClass()).async(true).name("reload_leaderboard_" + statistic.getTable().getName().toLowerCase() + "_" + statistic.getField().toLowerCase()).rank(Rank.DEVELOPER_ADMIN).build(), context -> {
                final long l1 = System.currentTimeMillis();
                query();
                context.sendMessage("§a" + this + " reloaded! (" + (System.currentTimeMillis() - l1) + "ms)");
                return true;
            });
        }

        if (Bukkit.isPrimaryThread())
            BukkitGame.getEngine().getLogger().info(this + " is running on the primary thread, its may affect server performance.");

        try {
            String s = internalQuery();

            PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(s);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<LeaderboardData> dataList = new ArrayList<>();

            while (resultSet.next()) {

                UUID uuid = UUID.fromString(resultSet.getString("uuid"));

                LeaderboardData leaderboardData = new LeaderboardData(uuid, statistic);
                leaderboardData.setValue(statistic, DataStorage.loadData(statistic, resultSet, "statistic"));

                for (Columns columns : load) {
                    leaderboardData.setValue(columns, DataStorage.loadData(columns, resultSet, columns.getField()));
                }
                dataList.add(leaderboardData);
            }

            values().clear();
            this.values = dataList;

            if (handler != null)
                sync(handler::onUpdate);

            preparedStatement.close();
            resultSet.close();

        } catch (Exception e) {
            throw new IllegalStateException("Exception while updating " + this, e);
        }
        return this;
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        values().clear();
        BukkitGame.getEngine().getBukkitFrame().unregisterCommand("reload" + statistic.getField().toLowerCase());
    }

    protected String internalQuery() {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ").append("t.").append("unique_id as uuid, ");
        query.append("t.").append(statistic.getField()).append(" as statistic");

        if (load.length != 0) {

            List<Tables> tablesList = removeDuplicates(Arrays.stream(load).map(Columns::getTable).filter(c -> c != statistic.getTable()).collect(Collectors.toList()));

            Iterator<Columns> iterator = Arrays.stream(load).iterator();

            while (iterator.hasNext()) {
                Columns columns = iterator.next();
                query.append(", ").append((columns.getTable() == statistic.getTable() ? "t" : columns.getTable().getName())).append(".").append(columns.getField());
            }

            query.append(" FROM ").append(statistic.getTable().getName()).append(" AS t");

            for (Tables table : tablesList) {
                query.append(" INNER JOIN ").append(table.getName()).append(" ON t.unique_id = ").append(table.getName()).append(".unique_id ");
            }
        }

        query.append(" WHERE").append(" banned IS NULL AND ").append(statistic.getField()).append(" IS NOT NULL AND ").append(statistic.getField()).append(" != '").append(statistic.getDefaultValue().toString()).append("' OR").append(" banned != 'true' AND ").append(statistic.getField()).append(" IS NOT NULL AND ").append(statistic.getField()).append(" != '").append(statistic.getDefaultValue()).append("'");
        query.append(" ORDER BY statistic DESC LIMIT ").append(limit).append(";");

        return query.toString();
    }

    @EventHandler
    public void onServerHeartbeat(ServerHeartbeatEvent event) {
        if (event.isPeriodic(update.getPeriod()))
            async(this::query);
    }

    public List<LeaderboardData> values() {
        return values;
    }

    protected <T> ArrayList<T> removeDuplicates(List<T> list) {
        Set<T> set = new LinkedHashSet<>(list);
        list.clear();
        list.addAll(set);
        return new ArrayList<>(list);
    }

    @Override
    public String toString() {
        return "Leaderboard{" +
                "statistic=" + statistic.getField() +
                '}';
    }

    public Leaderboard registerHandler(LeaderboardHandler handler) {
        this.handler = handler;
        return this;
    }
}
