/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.skywars;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.server.skywars.GameType;
import com.minecraft.skywars.user.UserStorage;
import lombok.Getter;

@Getter
public class Skywars extends BukkitGame {

    private static Skywars instance;

    private UserStorage userStorage;
    private GameType gameType; //GET FROM CONFIG

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        userStorage = new UserStorage(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static Skywars getInstance() {
        return instance;
    }

    public GameType getGameType() {
        return GameType.SOLO;
    }

}