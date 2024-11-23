package com.minecraft.staff.build;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.server.BukkitServerStorage;
import com.minecraft.core.server.ServerType;
import com.minecraft.staff.build.command.CreateWorld;
import com.minecraft.staff.build.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class Build extends BukkitGame {

    @Override
    public void onLoad() {
        super.onLoad();
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Constants.setServerType(ServerType.UNKNOWN);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        Bukkit.unloadWorld("world", false);

        if (Bukkit.getServer().getWorld("build") == null) {
            System.out.println("Creating build world...");
            WorldCreator wc = new WorldCreator("build");
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            wc.createWorld();
        } else {
            System.out.println("Build world already exists.");
        }

        startServerDump();

        getEngine().getBukkitFrame().registerCommands(new CreateWorld());

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        ((BukkitServerStorage) getServerStorage()).subscribeProxyCount();

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
