package com.minecraft.limbo;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.redis.BukkitRedisPubSub;
import com.minecraft.core.bukkit.server.BukkitServerStorage;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.database.redis.RedisPubSub;
import com.minecraft.core.server.ServerType;
import com.minecraft.limbo.listener.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;

public class Limbo extends BukkitGame {

    @Override
    public void onLoad() {
        unsafe(this);
        Constants.setRedis(new Redis());
        //setVisible(true);
    }

    @Override
    public void onEnable() {
        Constants.setServerStorage(new BukkitServerStorage());

        Constants.setServerType(ServerType.LIMBO_AFK);
        getServerStorage().listen(ServerType.MAIN_LOBBY);

        startServerDump();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "Redirection");

        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getScheduler().runTaskAsynchronously(this, new RedisPubSub(new BukkitRedisPubSub(), Redis.SERVER_COMMUNICATION_CHANNEL));

        WorldBorder worldBorder = Bukkit.getWorlds().get(0).getWorldBorder();
        worldBorder.setCenter(new Location(Bukkit.getWorlds().get(0), 0, 28, 0));
        worldBorder.setSize(40);
    }

    @Override
    public void onDisable() {
        try {
            Constants.getRedis().getJedisPool().destroy();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
