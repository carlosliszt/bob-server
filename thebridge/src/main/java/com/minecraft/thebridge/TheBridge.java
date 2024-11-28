package com.minecraft.thebridge;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.thebridge.GameType;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.compression.WinRAR;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.ranking.RankingHandler;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.thebridge.command.RankingCommand;
import com.minecraft.thebridge.game.Game;
import com.minecraft.thebridge.game.GameStorage;
import com.minecraft.thebridge.game.cage.CageStorage;
import com.minecraft.thebridge.game.listeners.GameListeners;
import com.minecraft.thebridge.game.listeners.UserListeners;
import com.minecraft.thebridge.listeners.DamageListener;
import com.minecraft.thebridge.listeners.RoomListeners;
import com.minecraft.thebridge.listeners.WorldListener;
import com.minecraft.thebridge.user.UserLoader;
import com.minecraft.thebridge.user.UserStorage;
import com.minecraft.thebridge.util.constants.BridgeConstants;
import com.minecraft.thebridge.util.map.GameConfiguration;
import com.minecraft.thebridge.util.map.VoidGenerator;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class TheBridge extends BukkitGame implements BukkitInterface {

    private static TheBridge instance;

    private GameStorage gameStorage;
    private UserStorage userStorage;
    private BridgeConstants constants;
    private CageStorage cageStorage;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (getPluginUpdater().isUpdated())
            return;

        gameStorage = new GameStorage(this);

        constants = new BridgeConstants();
        userStorage = new UserStorage();
        cageStorage = new CageStorage();

        loadGames();
        getCageStorage().loadCages();

        setRankingFactory(new RankingFactory(RankingTarget.THE_BRIDGE));
        getRankingFactory().query();

        getRankingFactory().registerRankingHandler(new RankingHandler() {
            @Override
            public void onRankingUpgrade(Account account, Ranking old, Ranking upgrade) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    player.sendMessage("§eVocê foi promovido para " + upgrade.getColor() + upgrade.getSymbol() + " " + upgrade.getName() + ".");
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 3F);

                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }
                async(() -> account.getDataStorage().saveTable(Tables.THE_BRIDGE));
            }

            @Override
            public void onRankingDowngrade(Account account, Ranking downgrade) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    player.sendMessage("§eVocê foi rebaixado para " + downgrade.getColor() + downgrade.getSymbol() + " " + downgrade.getName() + ".");

                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }

                async(() -> account.getDataStorage().saveTable(Tables.THE_BRIDGE));
            }

            @Override
            public void onChallengerAssign(Account account) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }
            }

            @Override
            public void onChallengerDesign(Account account) {
                Player player = Bukkit.getPlayer(account.getUniqueId());

                if (player != null) {
                    Tag tag = account.getProperty("account_tag").getAs(Tag.class);
                    PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

                    Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, prefixType));
                }
            }
        });

        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new GameListeners(), this);
        getServer().getPluginManager().registerEvents(new UserListeners(), this);
        getServer().getPluginManager().registerEvents(new UserLoader(), this);

        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new RoomListeners(this), this);

        getBukkitFrame().registerCommands(new RankingCommand());

        Constants.setServerType(ServerType.THE_BRIDGE);
        Constants.setLobbyType(ServerType.THE_BRIDGE_LOBBY);
        getServerStorage().listen(ServerType.MAIN_LOBBY, ServerType.THE_BRIDGE_LOBBY);

        getAccountLoader().addColumns(Tables.THE_BRIDGE.getColumns());

        startServerDump();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        gameStorage.end();
    }

    public void loadGames() {
        File directory = new File(System.getProperty("user.home") + File.separator + "misc" + File.separator + "arcade" + File.separator + "thebridge" + File.separator + "maps");
        File[] binaryMaps = directory.listFiles();

        if (binaryMaps == null || binaryMaps.length == 0) {
            System.out.println("No maps found.");
            return;
        }

        List<File> maps = new ArrayList<>();
        Collections.addAll(maps, binaryMaps);

        int mapLoop = 0;

        final int SOLO_GAMES = GameType.SOLO.getMaxGames();

        for (int i = 0; i < SOLO_GAMES + GameType.DOUBLE.getMaxGames(); i++) {
            int id = i + 1;
            int replaceLength = (id < 10) ? 1 : 2;
            String mapName = "bridge-" + id;

            File mapDirectory = new File(Bukkit.getWorldContainer(), mapName);
            File map = maps.get(mapLoop);
            WinRAR.unzip(map, mapDirectory);

            WorldCreator creator = new WorldCreator(mapName);
            creator.generateStructures(false);
            creator.generator(VoidGenerator.getInstance());

            World world = Bukkit.createWorld(creator);

            Game game = new Game( getInstanceId().substring(0, getInstanceId().length() - replaceLength) + id, world, i < SOLO_GAMES ? GameType.SOLO : GameType.DOUBLE);

            if (mapLoop + 1 >= maps.size())
                mapLoop = 0;
            else
                mapLoop++;

            GameConfiguration configuration = game.getConfiguration();

            try {
                File fileConfiguration = new File(mapDirectory, "map.json");
                JsonObject json = Constants.JSON_PARSER.parse(new FileReader(fileConfiguration)).getAsJsonObject();

                configuration.setName(json.get("name").getAsString());

                String rawSpawn = json.get("spawn").getAsString();
                List<Integer> integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                configuration.setSpawnPoint(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("red_spawn").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                configuration.setRedLocation(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("blue_spawn").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                configuration.setBlueLocation(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("blue_hologram").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                final Location blueHologram = new Location(world, integerList.get(0) + 0.5, integerList.get(1), integerList.get(2) + 0.5, integerList.get(3), integerList.get(4));
                configuration.setBlueHologram(blueHologram);

                rawSpawn = json.get("red_hologram").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                final Location redHologram = new Location(world, integerList.get(0) + 0.5, integerList.get(1), integerList.get(2) + 0.5, integerList.get(3), integerList.get(4));
                configuration.setRedHologram(redHologram);

                rawSpawn = json.get("red_cage").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                configuration.setRedCage(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("blue_cage").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    getGameStorage().delete(folder);
                    continue;
                }

                configuration.setBlueCage(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("red_buildLimit_1").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                final Location red_buildLimit_1 = new Location(world, integerList.get(0), integerList.get(1), integerList.get(2), integerList.get(3), integerList.get(4));

                rawSpawn = json.get("red_buildLimit_2").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                final Location red_buildLimit_2 = new Location(world, integerList.get(0), integerList.get(1), integerList.get(2), integerList.get(3), integerList.get(4));

                configuration.getInvincibleBlocks().addAll(this.blocksFromTwoPoints(red_buildLimit_1, red_buildLimit_2));

                rawSpawn = json.get("blue_buildLimit_1").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                final Location blue_buildLimit_1 = new Location(world, integerList.get(0), integerList.get(1), integerList.get(2), integerList.get(3), integerList.get(4));

                rawSpawn = json.get("blue_buildLimit_2").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                final Location blue_buildLimit_2 = new Location(world, integerList.get(0), integerList.get(1), integerList.get(2), integerList.get(3), integerList.get(4));

                configuration.getInvincibleBlocks().addAll(this.blocksFromTwoPoints(blue_buildLimit_1, blue_buildLimit_2));

                WorldEditAPI.getInstance().makeSphere(blueHologram, 8, 8, 8, true).forEach(location -> {
                    final Block block = location.getBlock();

                    if (block.getType() == Material.ENDER_PORTAL) {
                        configuration.getBlueBlockPortals().add(block);
                        block.setType(Material.AIR);
                    }
                });

                WorldEditAPI.getInstance().makeSphere(redHologram, 8, 8, 8, true).forEach(location -> {
                    final Block block = location.getBlock();

                    if (block.getType() == Material.ENDER_PORTAL) {
                        configuration.getRedBlockPortals().add(block);
                        block.setType(Material.AIR);
                    }
                });

                configuration.setMax_y(configuration.getSpawnPoint().getBlockY() + 7);
                configuration.setMin_y(configuration.getSpawnPoint().getBlockY() - 20);

                world.setPVP(true);
                world.setGameRuleValue("doMobSpawning", "false");
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("naturalRegeneration", "false");
                world.setGameRuleValue("sendCommandFeedback", "false");
                world.setGameRuleValue("logAdminCommands", "false");

                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MIN_VALUE);
                world.setThunderDuration(Integer.MIN_VALUE);

                world.setSpawnLocation(0, 71, 0);
                world.setAutoSave(false);
                world.setTime(6000);

                world.getEntities().forEach(Entity::remove);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            gameStorage.register(game);
        }

        gameStorage.start();
    }

    public static TheBridge getInstance() {
        return instance;
    }

    protected List<Block> blocksFromTwoPoints(final Location loc1, final Location loc2) {
        final List<Block> blocks = new ArrayList<>();

        final int topBlockX = (Math.max(loc1.getBlockX(), loc2.getBlockX()));
        final int bottomBlockX = (Math.min(loc1.getBlockX(), loc2.getBlockX()));

        final int topBlockY = (Math.max(loc1.getBlockY(), loc2.getBlockY()));
        final int bottomBlockY = (Math.min(loc1.getBlockY(), loc2.getBlockY()));

        final int topBlockZ = (Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        final int bottomBlockZ = (Math.min(loc1.getBlockZ(), loc2.getBlockZ()));

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);

                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

}