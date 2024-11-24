/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.event.user;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.game.GameStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@AllArgsConstructor
public class LivingUserDieEvent extends ServerEvent {

    private final User user, killer;
    private final boolean countStats;
    private final DieCause dieCause;
    private final List<ItemStack> drops;
    private Location locationToDrop;

    @Override
    public void fire() {
        if (HungerGames.getInstance().getGame().getStage() == GameStage.VICTORY)
            return;
        super.fire();
    }

    public void setLocationToDrop(Location locationToDrop) {
        this.locationToDrop = locationToDrop;
    }

    public boolean hasKiller() {
        return killer != null;
    }
}
