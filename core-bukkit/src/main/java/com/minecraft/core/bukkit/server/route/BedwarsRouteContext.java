package com.minecraft.core.bukkit.server.route;

import com.minecraft.core.bukkit.server.bedwars.BedwarsType;
import com.minecraft.core.bukkit.server.duels.DuelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BedwarsRouteContext {

    private BedwarsType game;
    private UUID target;
    private PlayMode playMode;

    private boolean hasTarget() {
        return target != null;
    }

    public boolean hasDefinedGame() {
        return game != null;
    }

}