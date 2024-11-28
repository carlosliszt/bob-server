/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.bukkit;

import com.minecraft.core.Constants;

import com.minecraft.core.bukkit.command.*;
import com.minecraft.core.bukkit.listener.AccountLoader;
import com.minecraft.core.bukkit.listener.AntiDamageIndicator;
import com.minecraft.core.bukkit.listener.ServerListener;
import com.minecraft.core.bukkit.redis.BukkitRedisPubSub;
import com.minecraft.core.bukkit.scheduler.BukkitServerTicker;
import com.minecraft.core.bukkit.server.BukkitServerInformationThread;
import com.minecraft.core.bukkit.server.BukkitServerStorage;
import com.minecraft.core.bukkit.util.WordCensor;
import com.minecraft.core.bukkit.util.bossbar.BossbarProvider;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.bukkit.util.hologram.HologramProvider;
import com.minecraft.core.bukkit.util.item.InteractableItemListener;
import com.minecraft.core.bukkit.util.language.BukkitTranslationExecutor;
import com.minecraft.core.bukkit.util.npc.NPCProvider;
import com.minecraft.core.bukkit.util.selector.InventoryService;
import com.minecraft.core.bukkit.util.variable.loader.VariableLoader;
import com.minecraft.core.bukkit.util.whitelist.Whitelist;
import com.minecraft.core.bukkit.util.worldedit.WorldEditProvider;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.mysql.MySQL;
import com.minecraft.core.database.mysql.MySQLProperties;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.database.redis.RedisPubSub;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.PropertiesStorageDataTranslation;
import com.minecraft.core.translation.TranslationExecutor;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.updater.PluginUpdater;
import dev.imanity.knockback.api.KnockbackService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.github.paperspigot.PaperSpigotConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Getter
public class BukkitGame extends JavaPlugin {

    private static BukkitGame engine;

    private BukkitFrame bukkitFrame;
    private NPCProvider NPCProvider;
    private HologramProvider hologramProvider;
    private WorldEditProvider worldEditProvider;
    private TranslationExecutor translationExecutor;
    private WordCensor wordCensor;
    private RedisPubSub redisPubSub;
    private Whitelist whitelist;
    private VariableLoader variableLoader;
    private PluginUpdater pluginUpdater;
    private AccountLoader accountLoader;
    private RankingFactory rankingFactory;
    private BossbarProvider bossbarProvider;
    private KnockbackService knockbackService;
    private InventoryService inventoryService;

    private String instanceId;

    private static final String CHARACTERS = "abcdefghijklmnopqrtsuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static String generateRoomCode(int a) {
        StringBuilder code = new StringBuilder(a);
        for (int i = 0; i < a; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    @Override
    public void onLoad() {

        engine = this;
        this.pluginUpdater = new PluginUpdater(super.getFile());

        if (pluginUpdater.verify(Bukkit::shutdown))
            return;

        Constants.setMySQL(new MySQL(MySQLProperties.load(new File(getSQLDirectory(), "mysql.json"))).connect());
        Constants.setRedis(new Redis());

        //setVisible(true);
    }

    @Override
    public void onEnable() {

        instanceId = generateRoomCode(6);

        System.out.println("Instance ID: " + instanceId);

        if (pluginUpdater.isUpdated())
            return;

         DataStorage.createTables();
        // Remove the comment when this is necessary, we will avoid unnecessary SQL queries

        Constants.setServerStorage(new BukkitServerStorage(ServerType.MAIN_LOBBY));
        this.bukkitFrame = new BukkitFrame(this);
        this.NPCProvider = new NPCProvider(this);
        this.hologramProvider = new HologramProvider(this);
        this.bossbarProvider = new BossbarProvider(this);
        this.worldEditProvider = new WorldEditProvider();
        this.translationExecutor = new BukkitTranslationExecutor(new PropertiesStorageDataTranslation());
        this.whitelist = Whitelist.load();
        this.variableLoader = new VariableLoader();
        this.knockbackService = Bukkit.imanity().getKnockbackService();

        getServer().imanity().registerPacketHandler(this, new AntiDamageIndicator());

        this.inventoryService = new InventoryService(this);
        inventoryService.init();

        this.wordCensor = new WordCensor("*")
                .addCensure("porra", "poha", "easy", "ruim", "tnc", "vtnc", "gordo", "pobre", "fudido", "fodido", "filho da puta", "puta", "fdp", "desgraçado", "caralho",
                        "arrombado", "macaco", "gorila", "carniça", "flame", "mush", "hylex", "doente", "vsfd", "vadia", "vagabundo", "vagabunda",
                        "vsf", "fodase", "nerdola", "nerd", "retardado", "viado", "bixa", "corno", "chifrudo", "doente", "mongol", "nazismo", "ku klux klan", "kukluxklan");


        getServer().getScheduler().runTaskLater(this, () -> unregisterCommands("op", "deop", "scoreboard",
                "me", "say", "reload", "rl", "ban", "ban-ip", "ban-list", "pardon",
                "pardon-ip", "xp", "minecraft:kick", "minecraft:gamemode"), 2L);

        bukkitFrame.registerCommands(new StatisticsCommand(), new ServerIdCommand(), new PlusColorCommand(), new NickBookCommand(), new TpsCommand(), new ForceskinCommand(), new ReportsCommand(), new ArcadeDevCommand(), new ExportInventoryCommand(), new ParticleCommand(), new PckgstatCommand(), new GetlogsCommand(), new PacketFilterCommand(), new AlertCommand(), new PreferencesCommand(), new CensorCheckCommand(), new TeleportallCommand(), new MedalCommand(), new LobbyCommand(), new VariableCommand(), new ChatCommand(), new StafflogCommand(), new GetlocationCommand(), new VanishCommand(), new WhitelistCommand(), new SpeedCommand(), new LanguageCommand(), new ProfileCommand(), new CrashCommand(), new SudoCommand(), new SmiteCommand(), new WhisperCommand(), new FollowCommand(), new EffectsCommand(), new InventoryCommand(), new LoopCommand(), new NickCommand(), new TagCommand(), new PrefixtypeCommand(), new WorldEditCommand(), new TeleportCommand(), new GamemodeCommand(), new MydragonCommand());

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(this.accountLoader = new AccountLoader(), this);
        pluginManager.registerEvents(new ServerListener(), this);
        pluginManager.registerEvents(new InteractableItemListener(), this);

        new SkinCommand();

        Messenger messenger = getServer().getMessenger();

        messenger.registerOutgoingPluginChannel(this, "BungeeCord");
        messenger.registerOutgoingPluginChannel(this, "AntiCheat");
        messenger.registerOutgoingPluginChannel(this, "Redirection");

        getServer().getScheduler().runTaskAsynchronously(this, redisPubSub = new RedisPubSub(new BukkitRedisPubSub(), "proxy.commands", Redis.FRIEND_UPDATE_CHANNEL, Redis.CLAN_INTEGRATION_CHANNEL, Redis.CLAN_TAG_UPDATE, Redis.NICK_ADD_CHANNEL, Redis.SERVER_COMMUNICATION_CHANNEL, Redis.NICK_ADD_CHANNEL, Redis.PROFILE_UPDATE_CHANNEL, Redis.NICK_DISGUISE_CHANNEL, Redis.LANGUAGE_UPDATE_CHANNEL, Redis.RANK_UPDATE_CHANNEL, Redis.FLAG_UPDATE_CHANNEL, Redis.PREFERENCES_UPDATE_CHANNEL));

        new BukkitServerTicker().start(this);

        PaperSpigotConfig.interactLimitEnabled = false;
    }


    public File getSQLDirectory() {
        File file = new File(System.getProperty("user.home") + File.separator + "misc" + File.separator + "db");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    @Override
    public void onDisable() {

        if (pluginUpdater.isUpdated())
            return;

        try {
            Constants.getMySQL().getConnection().close();
            Constants.getRedis().getJedisPool().destroy();
            getWhitelist().save();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        pluginUpdater.verify(Bukkit::shutdown);
    }

    public static BukkitGame getEngine() {
        return engine;
    }

    public void startServerDump() {
        final ServerStorage serverStorage = getServerStorage();
        serverStorage.open();
        Thread thread = new BukkitServerInformationThread((BukkitServerStorage) serverStorage);
        thread.setDaemon(true);
        thread.start();
    }

    public void setWhitelist(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    public ServerStorage getServerStorage() {
        return Constants.getServerStorage();
    }

    public RankingFactory getRankingFactory() {
        return rankingFactory;
    }

    public void setRankingFactory(RankingFactory rankingFactory) {
        this.rankingFactory = rankingFactory;
    }

    public static void unsafe(BukkitGame bukkitGame) { // Engine was returning null in Auth module cuz its "soft-independent"
        engine = bukkitGame;
    }

    protected void deleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                deleteFile(new File(file, child));
            }
        }
        if (file.exists())
            file.delete();
    }

    @SuppressWarnings("unchecked")
    public void unregisterCommands(String... commands) {
        try {
            Field firstField = getServer().getClass().getDeclaredField("commandMap");
            firstField.setAccessible(true);
            CommandMap commandMap = (CommandMap) firstField.get(getServer());
            Field secondField = commandMap.getClass().getDeclaredField("knownCommands");
            secondField.setAccessible(true);
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) secondField.get(commandMap);
            for (String command : commands) {
                if (knownCommands.containsKey(command)) {
                    knownCommands.remove(command);
                    List<String> aliases = new ArrayList<>();
                    for (String key : knownCommands.keySet()) {
                        if (!key.contains(":"))
                            continue;

                        String substr = key.substring(key.indexOf(":") + 1);
                        if (substr.equalsIgnoreCase(command)) {
                            aliases.add(key);
                        }
                    }
                    for (String alias : aliases) {
                        knownCommands.remove(alias);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}