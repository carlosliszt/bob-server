/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.scheduler;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.LogData;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.player.PlayerPingHistory;
import net.md_5.bungee.api.ProxyServer;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Queue;

/**
 * CREATE TABLE IF NOT EXISTS `logs` (
 * `index` INT UNSIGNED NOT NULL AUTO_INCREMENT,
 * `unique_id` VARCHAR(36) NOT NULL,
 * `nickname` VARCHAR(16) NOT NULL,
 * `server` VARCHAR(20) NOT NULL,
 * `content` TINYTEXT NOT NULL,
 * `type` VARCHAR(20) NOT NULL,
 * `created_at` DATETIME NOT NULL DEFAULT NOW(),
 * PRIMARY KEY(`index`)
 * );
 */
public class LogScheduler implements Runnable {

    @Override
    public void run() {

        flushPings();

        Queue<LogData> queue = ProxyGame.getInstance().getLogQueue();

        if (queue.isEmpty())
            return;

        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement("INSERT INTO `logs` (`unique_id`, `nickname`, `server`, `content`, `type`, `created_at`) VALUES (?,?,?,?,?,?)")) {
            while (!queue.isEmpty()) {
                LogData logData = queue.poll();
                ps.setString(1, logData.getUniqueId().toString());
                ps.setString(2, logData.getNickname());
                ps.setString(3, logData.getServer());
                ps.setString(4, logData.getContent());
                ps.setString(5, logData.getType().toString());
                ps.setTimestamp(6, Timestamp.valueOf(logData.getCreatedAt()));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void flushPings() {

        ProxyServer.getInstance().getPlayers().forEach(player -> {

            Account account = Account.fetch(player.getUniqueId());

            if (account == null)
                return;

            if (!account.hasProperty("pings"))
                return;

            PlayerPingHistory pingHistory = account.getProperty("pings").getAs(PlayerPingHistory.class);
            pingHistory.addPing(player.getPing());
        });
    }
}