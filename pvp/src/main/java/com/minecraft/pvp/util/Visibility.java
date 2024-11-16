/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.util;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.game.types.Damage;
import com.minecraft.pvp.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Visibility {

    private boolean needUpdate;

    public void update() {
        needUpdate = true;
    }

    public void tick() {
        if (!needUpdate)
            return;

        for (Player observer : Bukkit.getOnlinePlayers()) {

            Account observerAccount = Account.fetch(observer.getUniqueId());
            boolean observerIsAdmin = Vanish.getInstance().isVanished(observerAccount.getUniqueId());

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!observer.equals(target)) {
                    Account targetAccount = Account.fetch(target.getUniqueId());
                    boolean sameGame = observer.getWorld().getUID().equals(target.getWorld().getUID());
                    if (Vanish.getInstance().isVanished(targetAccount.getUniqueId())) {
                        setVisibility(observer, target, observerIsAdmin && sameGame && observerAccount.getRank().getCategory().getImportance() >= Vanish.getInstance().getRank(targetAccount.getUniqueId()).getCategory().getImportance());
                    } else {
                        User ob = User.fetch(observer.getUniqueId());
                        boolean damaging = ob.getGame().getUniqueId().equals(PvP.getPvP().getGameStorage().getGame(Damage.class).getUniqueId()) && !ob.isKept();
                        if (damaging)
                            setVisibility(observer, target, false);
                        else
                            setVisibility(observer, target, sameGame);
                    }
                }
            }
        }

        needUpdate = false;
    }

    public void refresh(User user) {
        Player observer = user.getPlayer();
        Game game = user.getGame();

        if (game == null) {
            hideAll(observer);
            return;
        }

        Account observerAccount = Account.fetch(observer.getUniqueId());
        boolean observerIsAdmin = Vanish.getInstance().isVanished(observerAccount.getUniqueId());

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!observer.getWorld().getUID().equals(target.getWorld().getUID())) {
                setVisibility(observer, target, false);
            } else {
                if (Vanish.getInstance().isVanished(target.getUniqueId())) {
                    setVisibility(observer, target, observerIsAdmin && observerAccount.getRank().getCategory().getImportance() >= Vanish.getInstance().getRank(target.getUniqueId()).getCategory().getImportance());
                } else {
                    User ob = User.fetch(observer.getUniqueId());
                    boolean damaging = ob.getGame().getUniqueId().equals(PvP.getPvP().getGameStorage().getGame(Damage.class).getUniqueId()) && !ob.isKept();
                    setVisibility(observer, target, !damaging);
                }
            }
        }
    }

    public void show(Player observer, Player target) {
        if (!observer.canSee(target)) {
            observer.showPlayer(target);
            debug("showing " + target.getName() + " for " + observer.getName());
        }
    }

    public void hide(Player observer, Player target) {
        if (observer.canSee(target)) {
            observer.hidePlayer(target);
            debug("hiding " + target.getName() + " for " + observer.getName());
        }
    }

    public void setVisibility(Player observer, Player target, boolean state) {
        if (state) {
            show(observer, target);
        } else {
            hide(observer, target);
        }
    }

    public void hideAll(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(player);
            player.hidePlayer(other);
        }
    }

    boolean DEBUG = false;

    protected void debug(String str) {
        if (DEBUG) {
            System.out.println(str);
        }
    }

}