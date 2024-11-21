package com.minecraft.lobby.feature.parkour;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@Getter
@RequiredArgsConstructor
public class Checkpoint {

    private final int id;
    private final Location location;

    @Override
    public String toString() {
        return "Checkpoint{" +
                "id=" + id +
                ", location=" + location +
                '}';
    }
}