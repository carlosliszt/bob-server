/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.server;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.server.RedisPubSubEvent;
import com.minecraft.core.bukkit.event.server.ServerPayloadReceiveEvent;
import com.minecraft.core.bukkit.event.server.ServerPayloadSendEvent;
import com.minecraft.core.bukkit.listener.AccountLoader;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.server.packet.ServerListPacket;
import com.minecraft.core.server.packet.ServerPayload;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BukkitServerStorage extends ServerStorage implements Listener, BukkitInterface {

    private final List<ServerType> listen;
    private ServerListPacket serverList;
    private final int port;
    private int playerCount;

    public BukkitServerStorage(ServerType... serverType) {
        super();
        this.listen = new ArrayList<>();
        this.port = Bukkit.getPort();
        this.playerCount = -1;
        listen(serverType);
    }

    @Override
    public int myPort() {
        return this.port;
    }

    @Override
    public void close() {
    }

    @Override
    public void send() {

        AccountLoader accountLoader = BukkitGame.getEngine().getAccountLoader();

        int maxPlayers = accountLoader == null ? Bukkit.getMaxPlayers() : accountLoader.getMaxPlayers();

        if (maxPlayers != Bukkit.getMaxPlayers())
            Bukkit.imanity().setMaxPlayers(maxPlayers);

        ServerPayload serverPayload = new ServerPayload(Constants.getServerCategory(), Constants.getServerType(), myPort(), Bukkit.getOnlinePlayers().size(), maxPlayers);

        ServerPayloadSendEvent event = new ServerPayloadSendEvent(serverPayload);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {

            Server myServer = getServer(serverPayload);

            if (myServer.getServerType() == ServerType.UNKNOWN)
                myServer.setServerType(serverPayload.getServerType());

            if (myServer.getServerCategory() == ServerCategory.UNKNOWN)
                myServer.setServerCategory(serverPayload.getServerCategory());

            getServer(serverPayload).setLastBreath(serverPayload);
            Constants.getRedis().publish(Redis.SERVER_COMMUNICATION_CHANNEL, Constants.GSON.toJson(serverPayload));
        }
    }

    @Override
    public void open() {
        try (Jedis redis = Constants.getRedis().getResource(Redis.SERVER_CACHE)) {

            String raw = redis.get("proxy.serverlist");

            if (raw == null)
                return;

            this.serverList = Constants.GSON.fromJson(raw, ServerListPacket.class);

            for (ServerListPacket.ServerInfo serverInfo : serverList.getServers()) {
                registerServer(new Server(serverInfo.getName(), serverInfo.getPort(), null, ServerType.UNKNOWN, ServerCategory.UNKNOWN));
            }

            Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
        }
    }

    @Override
    public boolean isListen(ServerType serverType) {
        return listen.contains(serverType);
    }

    @Override
    public void listen(ServerType... serverTypes) {
        Collections.addAll(listen, serverTypes);
    }

    public void subscribeProxyCount() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitGame.getEngine(), () -> BukkitGame.getEngine().getRedisPubSub().getJedisPubSub().subscribe(Redis.PROXY_COUNT_CHANNEL), 20L);
    }

    @Override
    public String getNameOf(int port) {
        if (serverList == null)
            return port + "";
        ServerListPacket.ServerInfo serverInfo = serverList.getServers().stream().filter(o -> o.getPort() == port).findFirst().orElse(null);
        return serverInfo == null ? "" : serverInfo.getName();
    }

    @EventHandler
    public void onBukkitPayload(RedisPubSubEvent event) {
        if (event.getChannel().equals(Redis.SERVER_COMMUNICATION_CHANNEL)) {

            String message = event.getMessage();

            ServerPayload serverPayload = Constants.GSON.fromJson(message, ServerPayload.class);

            BukkitServerStorage bukkitServerStorage = (BukkitServerStorage) BukkitGame.getEngine().getServerStorage();

            if (!bukkitServerStorage.isListen(serverPayload.getServerType()) || serverPayload.getPort() == myPort()) {
                return;
            }

            Server server = bukkitServerStorage.getServer(serverPayload);

            if (server == null)
                getServers().add(server = new Server(getNameOf(serverPayload.getPort()), serverPayload.getPort(), serverPayload, serverPayload.getServerType(), serverPayload.getServerCategory()));

            server.setLastBreath(serverPayload);

            server.setServerCategory(serverPayload.getServerCategory());
            server.setServerType(serverPayload.getServerType());

            if (!bukkitServerStorage.getServers().contains(server)) {
                bukkitServerStorage.getServers().add(server);
            }

            Bukkit.getPluginManager().callEvent(new ServerPayloadReceiveEvent(server, serverPayload));
        } else if (event.getChannel().equals(Redis.PROXY_COUNT_CHANNEL)) {
            this.playerCount = Integer.parseInt(event.getMessage());
        }
    }

    @Override
    public int count() {
        return this.playerCount;
    }
}
