/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.auth.scheduler;

import com.minecraft.auth.Auth;
import com.minecraft.core.bukkit.scheduler.GameRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertScheduler extends GameRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getTicksLived() >= 500) {
                player.kickPlayer("§cVocê demorou muito para se autenticar.");
                continue;
            }

            if (player.hasMetadata("captcha_challenge"))
                continue;

            player.sendMessage("§aUtilize §f/login §aou §f/register §apara se autenticar!");
        }
    }

    public void start() {
        this.runTaskTimer(Auth.getAuth(), 50L, 50L);
    }
}