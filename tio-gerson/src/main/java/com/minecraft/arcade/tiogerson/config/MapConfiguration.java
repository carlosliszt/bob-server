package com.minecraft.arcade.tiogerson.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class MapConfiguration {

    private String name;
    private Location spawnPoint, tioGersonLocation, enzoLocation;
    private int  height;

    @Override
    public String toString() {
        return "MapConfiguration{" +
                "name='" + name + '\'' +
                ", spawnPoint=" + spawnPoint +
                ", tioGersonLocation=" + tioGersonLocation +
                ", enzoLocation=" + enzoLocation +
                ", height=" + height +
                '}';
    }
}
