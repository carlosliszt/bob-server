/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.listeners;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.user.User;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class UserLoader implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        UUID uniqueId = event.getUniqueId();
        User user = new User(Account.fetch(uniqueId));

        Account account = user.getAccount();

        try (Jedis jedis = Constants.getRedis().getResource()) {
            String route_key = "route:" + account.getUniqueId();

            String route = jedis.get(route_key);

            if (route == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, account.getLanguage().translate("arcade.route.not_found", "no route"));
                return;
            }

            if (Constants.isUniqueId(route)) {
                User target = User.fetch(UUID.fromString(route));
                user.setGame(target.getGame());
            } else {
                user.setGame(PvP.getPvP().getGameStorage().getGameByRoute(route));
            }
            jedis.del(route_key);
        }

        account.getDataStorage().loadColumns(user.getGame().getColumnsToLoad());

        Kit none = PvP.getPvP().getKitStorage().getKits().get(0);

        user.setKit1(none);
        user.setKit2(none);

        RankingFactory factory = PvP.getPvP().getRankingFactory();

        if (factory != null) {
            account.setRanking(Ranking.fromId(account.getData(factory.getTarget().getRanking()).getAsInt()));

            if (factory.getBestPlayers().contains(account.getUniqueId()) && account.getRanking() == Ranking.MASTER_IV)
                account.setRanking(Ranking.CHALLENGER);
        }

        PvP.getPvP().getUserStorage().store(uniqueId, user);
    }

    @EventHandler
    public void onDefineSpawn(PlayerInitialSpawnEvent event) {
        User user = User.fetch(event.getPlayer().getUniqueId());
        event.setSpawnLocation(user.getGame().getSpawn());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        User user = User.fetch(player.getUniqueId());
        user.setScoreboard(new GameScoreboard(player));
        user.loadKits();

        user.getGame().join(user, false);
        user.getGame().onLogin(user);

        user.vanish();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        User user = User.fetch(player.getUniqueId());
        user.getGame().quit(user);

        PvP.getPvP().getUserStorage().forget(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        World from_world = event.getFrom().getWorld(), to_world = event.getTo().getWorld();

        if (from_world.getName().equals("world") || from_world.getUID().equals(to_world.getUID()))
            return;

        Game from = PvP.getPvP().getGameStorage().getGameByWorld(from_world), to = PvP.getPvP().getGameStorage().getGameByWorld(to_world);

        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        from.quit(user);
        to.join(user, true);
        user.vanish();
    }

}