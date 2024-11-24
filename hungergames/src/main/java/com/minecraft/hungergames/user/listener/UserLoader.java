/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserLoader implements Listener, BukkitInterface {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {

        Account account = Account.fetch(event.getPlayer().getUniqueId());

        if (account == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("unexpected_error");
            return;
        }

        User user = HungerGames.getInstance().getUserStorage().storeIfAbsent(account);
        user.setPlayer(event.getPlayer());
        user.setOnline(true);
        user.setBossbar(BukkitGame.getEngine().getBossbarProvider().getBossbar(event.getPlayer()));

        RankingFactory factory = HungerGames.getInstance().getRankingFactory();

        if (factory != null) {
            account.setRanking(Ranking.fromId(account.getData(factory.getTarget().getRanking()).getAsInt()));

            if (factory.getBestPlayers().contains(account.getUniqueId()) && account.getRanking() == Ranking.MASTER_IV)
                account.setRanking(Ranking.CHALLENGER);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        User user = User.fetch(event.getPlayer().getUniqueId());
        user.setOnline(false);

        if (user.isAlive() && user.getKills() > 0)
            async(() -> user.getAccount().getDataStorage().saveTable(Tables.HUNGERGAMES));
    }
}
