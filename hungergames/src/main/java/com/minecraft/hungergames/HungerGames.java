/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.hungergames;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.command.InfoCommand;
import com.minecraft.core.bukkit.command.WhitelistCommand;
import com.minecraft.core.bukkit.command.WorldEditCommand;
import com.minecraft.core.bukkit.listener.EnchantmentListener;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.bukkit.util.file.FileUtil;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.hungergames.command.*;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.handler.listener.DamageListener;
import com.minecraft.hungergames.game.list.Default;
import com.minecraft.hungergames.game.structure.Coliseum;
import com.minecraft.hungergames.game.structure.Stairs;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.celebrations.Celebration;
import com.minecraft.hungergames.user.celebrations.CelebrationStorage;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.KitStorage;
import com.minecraft.hungergames.user.storage.UserStorage;
import com.minecraft.hungergames.util.arena.FileArena;
import com.minecraft.hungergames.util.arena.shape.Square;
import com.minecraft.hungergames.util.bo3.BO3;
import com.minecraft.hungergames.util.constructor.listener.ListenerLoader;
import com.minecraft.hungergames.util.game.GameStorage;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import com.minecraft.hungergames.util.selector.object.DiaryKit;
import com.minecraft.hungergames.util.selector.object.SpectatorList;
import com.minecraft.hungergames.util.timer.GameScheduler;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class HungerGames extends BukkitGame {

    private static HungerGames instance;
    private UserStorage userStorage;
    private KitStorage kitStorage;
    private CelebrationStorage celebrationStorage;
    private Game game;
    private ListenerLoader listenerLoader;
    private SpectatorList spectatorList;
    private Coliseum coliseum;
    private KitEmployer kitEmployer;

    @Override
    public void onLoad() {
        super.onLoad();

        instance = this;

        if (getPluginUpdater().isUpdated())
            return;

        saveDefaultConfig();

        if (!getMap()) {
            System.out.println("Could not get a map, shutting down!");
            Bukkit.shutdown();
        }
    }

    @Override
    public void onEnable() {

        getLogger().info("Loading core-bukkit....");
        super.onEnable();

        if (getPluginUpdater().isUpdated())
            return;

        getLogger().info("core-bukkit loaded successfully!");

        getLogger().info("Loading hungergames...");
        this.listenerLoader = new ListenerLoader();

        getLogger().info("Loading spawn coliseum and limbo...");
        FileArena.load(new File("/home/ubuntu/misc/hg/structures/spawn"));

        World world = Bukkit.getWorlds().get(0);

        getLogger().info("Spawning coliseum...");
        spawn(world);
        getLogger().info("Coliseum spawned! Generating limbo...");
        limbo(world);
        getLogger().info("Limbo generated!");

        getLogger().info("Loading commands...");
        BukkitFrame bukkitFrame = getBukkitFrame();

        bukkitFrame.registerCommands(new SpectatorsCommand(), new TrollCommand(), new ForcewinCommand(), new ForcehealthCommand(), new StartCommand(), new KitsCommand(), new ArenaCommand(), new TemplatekitCommand(), new FeastCommand(), new SpawnCommand(), new CleardropsCommand(), new ClearmobsCommand(), new ForcekitCommand(), new IrCommand(), new LmCommand(), new SpawnPointCommand(), new LockCommand(), new KillCommand(), new ForfeitCommand(), new ComemorationCommand(), new ForcefeastCommand(), new ForceMinifeastCommand(), new TimeCommand());

        getLogger().info("Starting game...");
        this.game = loadGame();

        this.userStorage = new UserStorage(this).enable();
        this.kitStorage = new KitStorage(this).enable();
        this.celebrationStorage = new CelebrationStorage(this).enable();
        this.spectatorList = new SpectatorList();
        this.kitEmployer = new KitEmployer();

        FileArena.load(new File("/home/ubuntu/misc/hg/structures/feast"));

        getLogger().info("hungergames loaded successfully!");

        getLogger().info("Loading chunks...");
        loadChunks(world);
        world.getEntitiesByClasses(Item.class, FallingBlock.class, Rabbit.class).forEach(Entity::remove);
        getLogger().info("chunks loaded successfully!");

        getLogger().info("Loading game...");
        this.game.load();
        getLogger().info("game loaded successfully!");

        new GameScheduler(this).schedule();

        getServer().getPluginManager().registerEvents(new EnchantmentListener(), this);

        FileArena.load(new File("/home/ubuntu/misc/hg/structures/minifeast"));

        getAccountLoader().addColumns(Columns.HG_KILLS, Columns.HG_DEATHS, Columns.HG_KITS, Columns.HG_WINS, Columns.HG_COINS, Columns.HG_MAX_GAME_KILLS, Columns.HG_KITS, Columns.HG_DAILY_KITS);
        makeRecipes();
        startServerDump();

        getServerStorage().listen(ServerType.HG_LOBBY, Constants.getServerType());

        Constants.setLobbyType(ServerType.HG_LOBBY);

        world.setAutoSave(false);
        world.setGameRuleValue("randomTickSpeed", "0");
        world.setGameRuleValue("showDeathMessages", "false");
        world.setGameRuleValue("logAdminCommands", "false");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(6000);

        if (getRankingFactory() != null)
            getBukkitFrame().registerCommands(new RankingCommand());

        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

        while (iterator.hasNext()) {

            Recipe recipe = iterator.next();

            if (recipe.getResult() != null && recipe.getResult().getType() == Material.BLAZE_POWDER)
                iterator.remove();
        }

        bukkitFrame.registerCommands(new InfoCommand<>(User.class, str -> User.fetch(UUID.fromString(str))));
        bukkitFrame.getCommands(WorldEditCommand.class).forEach(c -> c.getCommandInfo().setRank(Rank.PARTNER_PLUS));
        bukkitFrame.getCommands(WhitelistCommand.class).forEach(c -> c.getCommandInfo().setRank(Rank.PARTNER_PLUS));
        bukkitFrame.registerAdapter(Kit.class, getKitStorage()::getKit);
        bukkitFrame.registerAdapter(Celebration.class, getCelebrationStorage()::getCelebration);
        bukkitFrame.registerAdapter(User.class, User::getUser);
        getServer().getPluginManager().registerEvents(new DiaryKit(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);

        kitEmployer.run(false, game);
    }

    private void loadChunks(World world) {
        for (int x = -464; x <= 464 && MinecraftServer.getServer().isRunning(); x += 16) {
            for (int z = -464; z <= 464 && MinecraftServer.getServer().isRunning(); z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                chunk.load();
            }
        }
        world.getEntitiesByClasses(Skeleton.class, Zombie.class).forEach(Entity::remove);
    }

    private void spawn(World world) {

        FileArena fileArena = FileArena.getArena("spawn");
        BO3 bo3 = fileArena.getBO3();

        int highest = world.getHighestBlockYAt(0, 0);
        Random random = Constants.RANDOM;
        int posY = (random.nextBoolean() ? highest + Math.max(random.nextInt(6), 3) : highest - Math.max(random.nextInt(4), 2));

        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), 0, posY, 0);

        getLogger().info("Found coliseum location!");

        Coliseum coliseum = this.coliseum = new Coliseum(spawnLocation, bo3);

        if (!getConfig().getBoolean("game.spawn"))
            return;

        List<Location> ignoreBlocks = bo3.getBlocks().stream().filter(block -> block.getPattern().getMaterial() != Material.EMERALD_BLOCK).map(c -> spawnLocation.clone().add(c.getX(), c.getY(), c.getZ())).collect(Collectors.toList());

        getLogger().info("Placing coliseum...");

        coliseum.spawn((location, pattern) -> {

            if (!location.getChunk().isLoaded())
                location.getChunk().load();

            Material material = pattern.getMaterial();
            Block block = location.getBlock();

            if (material == Material.IRON_FENCE) {
                WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0);
                return false;
            } else if (material == Material.WEB) {
                block.setMetadata("unbreakable", new GameMetadata(true));
                return true;
            } else if (material == Material.NETHER_BRICK_STAIRS) {
                new Stairs(pattern.getData(), location, ignoreBlocks).spawnStructure();
            } else if (material == Material.QUARTZ_BLOCK) {

                WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.COBBLESTONE, (byte) 0);

                while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.DOWN);
                    WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.COBBLESTONE, (byte) 0);
                }

            } else if (material == Material.ENDER_STONE) {

                WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.COBBLESTONE, (byte) 0);

                while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.DOWN);
                    WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.COBBLESTONE, (byte) 0);
                }

            } else if (material == Material.QUARTZ_ORE) {

                WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.SMOOTH_BRICK, (byte) 2);

                while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.DOWN);
                    WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.SMOOTH_BRICK, (byte) 2);
                }
            } else if (material == Material.EMERALD_BLOCK) {
                WorldEditAPI.getInstance().setLightLevel(location, 15);
            } else if (material == Material.LOG || material == Material.LOG_2) {
                block.setMetadata("kit.lumberjack.ignore", new GameMetadata((byte) 0));
                return true;
            } else {
                return true;
            }
            return false;
        });

        getLogger().info("Coliseum placed! Clearing unused memory...");

        bo3.getBlocks().clear();
        ignoreBlocks.clear();
        FileArena.getLoadedArenas().remove(fileArena);
    }

    private void limbo(World world) {

        Location location = new Location(world, 100, 1, 100);

        getLogger().info("Loading limbo chunks...");

        location.getChunk().load();

        int size = 3;
        int height = 5;

        getLogger().info("Clearing limbo area...");

        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                for (int y = 0; y < height; y++) {
                    Location loc = location.clone().add(x, y, z);

                    if (!loc.getChunk().isLoaded())
                        loc.getChunk().load();

                    if (loc.getBlock().getType() == Material.AIR)
                        continue;

                    WorldEditAPI.getInstance().setBlock(loc, Material.AIR, (byte) 0);
                }
            }
        }

        getLogger().info("Area cleared! Generating limbo square...");

        Square square = new Square(size, height, Collections.singletonList(Pattern.of(Material.BEDROCK)), true, false);
        square.spawn(location, (loc, pattern) -> {
            WorldEditAPI.getInstance().setBlock(loc, Material.BEDROCK, (byte) 0);
            return false;
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        delete(new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata"));
    }

    public static HungerGames getInstance() {
        return instance;
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public KitStorage getKitStorage() {
        return kitStorage;
    }

    public CelebrationStorage getCelebrationStorage() {
        return celebrationStorage;
    }

    public ListenerLoader getListenerLoader() {
        return listenerLoader;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    private Game loadGame() {
        try {
            String str = getConfig().getString("game.mode");
            Class<?> classGame = GameStorage.getGame(str);

            if (classGame == null) {
                getLogger().info("Mode '" + str + "' not found, loading default...");
                classGame = Default.class;
            }

            return (Game) classGame.getConstructor(HungerGames.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.shutdown();
            return null;
        }
    }

    protected void makeRecipes() {

        ShapelessRecipe cocoa = new ShapelessRecipe(new ItemStack(Material.MUSHROOM_SOUP));
        cocoa.addIngredient(Material.BOWL);
        cocoa.addIngredient(Material.INK_SACK, 3);

        ShapelessRecipe cactus = new ShapelessRecipe(new ItemStack(Material.MUSHROOM_SOUP));
        cactus.addIngredient(Material.BOWL);
        cactus.addIngredient(Material.CACTUS);

        Bukkit.addRecipe(cocoa);
        Bukkit.addRecipe(cactus);
    }

    protected boolean getMap() {
        try {

            File node = new File(System.getProperty("user.home") + File.separator + "misc" + File.separator + "hg" + File.separator + "maps");
            File[] files = node.listFiles();

            if (files == null || files.length == 0)
                return false;

            getServer().unloadWorld("world", false);
            delete(new File("world"));

            File selected = files[Constants.RANDOM.nextInt(files.length)];

            System.out.println();
            System.out.println("Map: " + selected.getName());
            System.out.println();

            FileUtil.copy(selected, new File("world"), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void delete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                delete(new File(file, child));
            }
        }
        if (file.exists())
            file.delete();
    }

    public void setRankingFactory(RankingFactory rankingFactory) {
        super.setRankingFactory(rankingFactory);
        if (rankingFactory != null)
            Bukkit.getScheduler().runTaskAsynchronously(this, rankingFactory::query);
    }

    public Coliseum getSpawn() {
        return coliseum;
    }

    public SpectatorList getSpectatorList() {
        return spectatorList;
    }

    public KitEmployer getKitEmployer() {
        return kitEmployer;
    }
}