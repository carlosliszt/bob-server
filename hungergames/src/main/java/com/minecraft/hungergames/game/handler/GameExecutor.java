/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.handler;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.game.GameTimeEvent;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RecurringListener(register = GameStage.WAITING, unregister = GameStage.VICTORY)
public abstract class GameExecutor implements Listener, BukkitInterface, Assistance {

    private Game game;

    public GameExecutor(HungerGames hungerGames) {
        run(() -> this.game = hungerGames.getGame(), 2L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameTime(GameTimeEvent event) {
        getPlugin().getUserStorage().getUsers().forEach(game::handleSidebar);

        if (game.getRecoveryMode().isEnabled())
            return;

        execute();
    }

    public abstract void execute();

    public int count() {
        return getPlugin().getUserStorage().getAliveUsers().size();
    }

    public void broadcast(String key, GameStage stage, int time) {
        String pt = DateUtils.formatTime(Language.PORTUGUESE, time), us = DateUtils.formatTime(Language.ENGLISH, time);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(p.getUniqueId());
            if (account == null)
                continue;
            p.sendMessage(account.getLanguage().translate(key, (account.getLanguage()) == Language.PORTUGUESE ? pt : us));
            if (stage == GameStage.WAITING && time <= 5)
                p.playSound(p.getLocation(), Sound.CLICK, 3.5F, 3.5F);
        }
    }

    public void start() {
        game.setTime(game.getVariables().getInvincibility() + 1);
        game.setStage(GameStage.INVINCIBILITY);
//        game.getHungerGames().getKitStorage().register();
        Bukkit.getOnlinePlayers().forEach(player -> {
            game.prepare(player);
            player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("hg.game.start", count()));
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 1F);
        });
        getPlugin().getHologramProvider().getHologramListener().unload();
        getPlugin().getNPCProvider().getNpcListener().unload();
        game.getLeaderboardPresets().forEach(leaderboardPreset -> leaderboardPreset.getLeaderboard().destroy());
        game.getWorld().setGameRuleValue("doDaylightCycle", "true");
    }
}
