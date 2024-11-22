package com.minecraft.staff.build;

import com.minecraft.staff.build.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

public class Build extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Bukkit.getServer().getWorld("build") == null) {
            System.out.println("Creating build world...");
            WorldCreator wc = new WorldCreator("build");
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            wc.createWorld();
        } else {
            System.out.println("Build world already exists.");
        }

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

}
