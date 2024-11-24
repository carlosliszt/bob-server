/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.constructor.listener;

import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class ListenerLoader {

    public void load(Object... objects) {
        for (Object object : objects) {
            if (Listener.class.isAssignableFrom(object.getClass())) {
                if (object.getClass().isAnnotationPresent(RecurringListener.class)) {
                    final RecurringListener recurringListener = object.getClass().getAnnotation(RecurringListener.class);
                    recurringListener.register().getPendingRegister().add((Listener) object);
                    recurringListener.unregister().getPendingUnregister().add((Listener) object);
                } else {
                    Bukkit.getPluginManager().registerEvents((Listener) object, HungerGames.getInstance());
                }
            }
        }
    }

    public void handle(GameStage stage) {
        stage.register();
        stage.unregister();
    }
}
