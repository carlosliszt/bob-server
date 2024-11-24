/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.timer;

import com.minecraft.core.bukkit.scheduler.GameRunnable;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.game.GameTimeEvent;
import lombok.Getter;

public class GameScheduler extends GameRunnable {

    @Getter
    private final HungerGames hungerGames;

    public GameScheduler(HungerGames hungerGames) {
        this.hungerGames = hungerGames;
    }

    @Override
    public void run() {
        new GameTimeEvent().fire();
    }

    public void schedule() {
        this.runTaskTimerAsynchronously(getHungerGames(), 20, 20);
    }
}
