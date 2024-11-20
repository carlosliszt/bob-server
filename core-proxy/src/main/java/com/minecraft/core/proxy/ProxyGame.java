/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy;

import com.google.common.io.ByteStreams;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.LogData;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.mysql.MySQL;
import com.minecraft.core.database.mysql.MySQLProperties;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.database.redis.RedisPubSub;
import com.minecraft.core.proxy.command.*;
import com.minecraft.core.proxy.discord.Discord;
import com.minecraft.core.proxy.listener.AccountLoader;
import com.minecraft.core.proxy.listener.PluginMessageListener;
import com.minecraft.core.proxy.listener.PunishmentListener;
import com.minecraft.core.proxy.listener.ServerListener;
import com.minecraft.core.proxy.redis.ProxyRedisPubSub;
import com.minecraft.core.proxy.scheduler.CountWatchScheduler;
import com.minecraft.core.proxy.scheduler.LogScheduler;
import com.minecraft.core.proxy.server.ProxyServerStorage;
import com.minecraft.core.proxy.util.command.ProxyFrame;
import com.minecraft.core.proxy.util.language.ProxyTranslationExecutor;
import com.minecraft.core.proxy.util.reward.storage.GiftCodeStorage;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.server.packet.ServerListPacket;
import com.minecraft.core.translation.PropertiesStorageDataTranslation;
import com.minecraft.core.translation.TranslationExecutor;
import com.minecraft.core.util.updater.PluginUpdater;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Getter
public class ProxyGame extends Plugin {

    private static ProxyGame instance;
    private ProxyFrame proxyFrame;
    private TranslationExecutor translationExecutor;
    private RedisPubSub redisPubSub;
    private Configuration configuration;
    private Discord discord;
    private PluginUpdater pluginUpdater;
    private ServerListPacket serverListPacket;
    private CountWatchScheduler countWatchScheduler;
    private GiftCodeStorage giftCodeStorage;
    private long startTime;

    private final Queue<LogData> logQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void onLoad() {
        instance = this;
        Constants.setMySQL(new MySQL(MySQLProperties.load(new File(getSQLDirectory(), "mysql.json"))).connect());
        Constants.setRedis(new Redis());

        enableConfig();
    }

    @Override
    public void onEnable() {

        this.pluginUpdater = new PluginUpdater(super.getFile());
        this.giftCodeStorage = new GiftCodeStorage();

        if (pluginUpdater.verify(ProxyServer.getInstance()::stop))
            return;

        try {

            try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement("DELETE FROM `logs` WHERE DATE(logs.created_at) < CURDATE() - INTERVAL 10 DAY")) {
                ps.executeUpdate();
            }

            DataStorage.createTables();
            // Remove the comment when this is necessary, we will avoid unnecessary SQL queries

            translationExecutor = new ProxyTranslationExecutor(new PropertiesStorageDataTranslation());

            discord = new Discord().start("MTMwNTE5NDYwMzU0ODI0NjA1Nw.G-BIU4.PBP_RcRXT1SbzMvD2iiye9Ep8ZDZtqPaEs5cnY");

            proxyFrame = new ProxyFrame(this);

            countWatchScheduler = new CountWatchScheduler();

            getProxyFrame().registerCommands(new ClanCommand(), new BungeeVersionCommand(), new AuthLogCommand(), new RewardCommand(), new SetplayerlimitCommand(), new StreamCommand(), new DataServiceCommand(), new GobuildCommand(), new TournamentCommand(), new LatencyCommand(), new ProxyTicksPerSecondCommand(), new PlayCommand(), new BroadcastCommand(), new PasteCommand(), new ClanxClanCommand(), new EventCommand(), new ScrimCommand(), new CacheuuidCommand(), new SetmotdCommand(), new StafflistCommand(), new CreatorlistCommand(), new PlayerlistCommand(), new ChangepasswordCommand(), new LoginCommand(), new RegisterCommand(), new PlayerFinderCommand(), new StaffchatCommand(), new PunishCommand(), new AccountCommand(), new KickCommand(), new ReportCommand(), new ServerCommand(), new MotherboardCommand(), new GoCommand());

            getProxy().getPluginManager().registerListener(this, new AccountLoader());
            getProxy().getPluginManager().registerListener(this, new ServerListener());
            getProxy().getPluginManager().registerListener(this, new PunishmentListener());
            getProxy().getPluginManager().registerListener(this, new PluginMessageListener());

            getProxy().registerChannel("BungeeCord");
            getProxy().registerChannel("Redirection");
            getProxy().registerChannel("Auth");
            getProxy().registerChannel("AntiCheat");

            getProxy().getScheduler().schedule(this, this::runGarbageCollector, 5, 5, TimeUnit.MINUTES);

            getProxy().getScheduler().runAsync(this, redisPubSub = new RedisPubSub(new ProxyRedisPubSub(), "proxy.commands", Redis.SERVER_REDIRECT_CHANNEL, Redis.CLAN_TAG_UPDATE, Redis.NICK_ADD_CHANNEL, Redis.OPEN_EVENT_CHANNEL, Redis.SKIN_CHANGE_CHANNEL, Redis.SERVER_COMMUNICATION_CHANNEL, Redis.PROFILE_UPDATE_CHANNEL, Redis.NICK_DISGUISE_CHANNEL, Redis.LANGUAGE_UPDATE_CHANNEL, Redis.RANK_UPDATE_CHANNEL, Redis.FLAG_UPDATE_CHANNEL, Redis.PREFERENCES_UPDATE_CHANNEL));

            getProxy().getScheduler().schedule(this, () -> {
                new LogScheduler().run();
                countWatchScheduler.run();
            }, 1, 1, TimeUnit.SECONDS);

            enableBroadcast();

            publishServerlist();

            Constants.setServerStorage(new ProxyServerStorage());
            Constants.getServerStorage().open();

            this.startTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            getProxy().stop();
        }

    }

    @Override
    public void onDisable() {

        if (pluginUpdater.isUpdated())
            return;

        try {
            Constants.getMySQL().getConnection().close();
            Constants.getRedis().getJedisPool().destroy();
            getDiscord().shutdown();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        pluginUpdater.verify(ProxyServer.getInstance()::stop);
    }

    protected void enableConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile();
                    try (InputStream is = getResourceAsStream("config.yml");
                         OutputStream os = new FileOutputStream(configFile)) {
                        ByteStreams.copy(is, os);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to create configuration file", e);
                }
            }
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ProxyGame getInstance() {
        return instance;
    }

    public void addLog(UUID uniqueId, String nickname, String server, String content, LogData.Type type) {
        getLogQueue().add(new LogData(uniqueId, nickname, server, content, type, LocalDateTime.now()));
    }

    public File getSQLDirectory() {
        File file = new File(System.getProperty("user.home") + File.separator + "misc" + File.separator + "db");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public void publishServerlist() {
        final ServerListPacket serverListPacket = this.serverListPacket = new ServerListPacket();
        List<ServerInfo> serverInfos = new ArrayList<>(ProxyServer.getInstance().getServersCopy().values());
        serverInfos.sort(Comparator.comparingInt(sv -> sv.getAddress().getPort()));
        for (ServerInfo server : serverInfos) {
            ServerListPacket.ServerInfo serverInfo = new ServerListPacket.ServerInfo(server.getName(), server.getAddress().getPort());
            serverListPacket.getServers().add(serverInfo);
        }
        try (Jedis redis = Constants.getRedis().getResource(Redis.SERVER_CACHE)) {
            redis.set("proxy.serverlist", Constants.GSON.toJson(serverListPacket));
        }
        serverInfos.clear();
    }

    public void runGarbageCollector() {

        Iterator<Account> accountIterator = Constants.getAccountStorage().getAccounts().iterator();

        while (accountIterator.hasNext()) {

            Account account = accountIterator.next();

            if (account.getUniqueId().equals(Constants.CONSOLE_UUID))
                continue;

            if (ProxyServer.getInstance().getPlayer(account.getUniqueId()) == null)
                accountIterator.remove();
        }
    }

    private final List<String> broadcasts = new ArrayList<>();
    private int index = 0;

    public void enableBroadcast() {
        broadcasts.add("§eAltere seu idioma principal usando §b/language");
        broadcasts.add("§eAcompanhe novidades em nosso Twitter §6@BobGames");
        broadcasts.add("§eAcompanhe suas estatísticas usando §b/stats");
        broadcasts.add("§eVenha fazer parte de nossa comunidade! Acesse nosso discord: §b" + Constants.SERVER_DISCORD);
        broadcasts.add("§eAltere sua experiência de jogo usando §b/prefs");
        getProxy().getScheduler().schedule(this, () -> {
            if (index >= broadcasts.size())
                index = 0;
            getProxy().broadcast(TextComponent.fromLegacyText("§b§l" + Constants.SERVER_NAME.toUpperCase() + " §7» " + broadcasts.get(index)));
            index++;
        }, 1, 1, TimeUnit.MINUTES);
    }

    public ServerStorage getServerStorage() {
        return Constants.getServerStorage();
    }

}