package com.minecraft.arcade.tiogerson.mode;

import com.google.gson.JsonObject;
import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.config.MapConfiguration;
import com.minecraft.arcade.tiogerson.player.UserDeathEvent;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.room.generator.VoidGenerator;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.util.enums.Items;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import com.minecraft.arcade.tiogerson.util.visibility.Visibility;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.compression.WinRAR;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.github.paperspigot.Title;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public abstract class Mode implements BukkitInterface, Listener {

    private static final ArcadeMain plugin = ArcadeMain.getInstance();

    @Setter
    private String name = getClass().getSimpleName();
    private final int maxRooms;
    private JsonObject jsonReader;
    private World world;
    @Setter
    private Columns wins, loses, winstreak, winstreakRecord, games;

    @Setter
    private int maxPlayers, minTioGerson, maxTioGerson;

    private int tioGersonDelay = 10;

    public void load() {

        String name = getClass().getSimpleName();

        plugin.getLogger().info("Loading " + name);

        List<File> maps = new ArrayList<>();

        File directory = new File(System.getProperty("user.home") + File.separator + "misc" + File.separator + "tiogerson" + File.separator + name.toLowerCase());
        File[] binaryMaps = directory.listFiles();

        if (binaryMaps == null || binaryMaps.length == 0) {
            System.out.println("No maps found for " + name);
            return;
        }

        Collections.addAll(maps, binaryMaps);

        int mapLoop = 0;

        for (int i = 0; i < maxRooms; i++) {

            int id = i + 1;
            String mapName = name.toLowerCase() + "-" + id;

            plugin.getLogger().info("Loading " + mapName + " for " + name);

            File mapDirectory = new File(Bukkit.getWorldContainer(), mapName);
            File map = maps.get(mapLoop);
            WinRAR.unzip(map, mapDirectory);

            WorldCreator creator = new WorldCreator(mapName);
            creator.generateStructures(false);
            creator.generator(VoidGenerator.getInstance());

            World world = this.world = Bukkit.createWorld(creator);

            Room room = new Room(id, this, world);

            if (mapLoop + 1 >= maps.size())
                mapLoop = 0;
            else
                mapLoop++;

            MapConfiguration mapConfiguration = new MapConfiguration();

            try {
                File fileConfiguration = new File(mapDirectory, "map.json");
                JsonObject json = this.jsonReader = Constants.JSON_PARSER.parse(new FileReader(fileConfiguration)).getAsJsonObject();

                mapConfiguration.setName(json.get("name").getAsString());
                mapConfiguration.setHeight(json.get("max_y").getAsInt());

                String rawSpawn = json.get("spawn").getAsString();
                List<Integer> integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 3) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    ArcadeMain.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                mapConfiguration.setSpawnPoint(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.5, integerList.get(2) + 0.5));

                rawSpawn = json.get("tiogerson_location").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    ArcadeMain.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                mapConfiguration.setTioGersonLocation(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("enzo_location").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    ArcadeMain.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                mapConfiguration.setEnzoLocation(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));
                room.setMapConfiguration(mapConfiguration);
                setup(room);
            } catch (Exception e) {
                System.out.println("Failed to load map " + map.getName());
                e.printStackTrace();
                continue;
            }

            plugin.getRoomStorage().register(room);

            plugin.getLogger().info("Loaded " + room.getCode() + " for " + name);
        }
    }

    public void tick(Room room) {

        RoomStage stage = room.getStage();
        boolean isFull = room.isFull();
        boolean almostFull = room.getAlivePlayers().size() == room.getMaxPlayers() / 2;
        int time = room.getTime();

        if (stage == RoomStage.WAITING && almostFull) {
            room.setStage(RoomStage.STARTING);
            room.setTime(40);
        } else if (stage == RoomStage.WAITING && isFull) {
            room.setStage(RoomStage.STARTING);
            room.setTime(4);
        } else if (stage == RoomStage.STARTING) {

            if (!isFull && !almostFull) {
                room.setStage(RoomStage.WAITING);
                room.setTime(-1);
                return;
            }

            room.setTime(time - 1);

            if (time == 0)
                room.start();
            else if (time <= 3) {
                final Title title = new Title("§c" + time, "", 1, 15, 10);

                int finalTime = time;

                room.getWorld().getPlayers().forEach(c -> {
                    c.sendTitle(title);
                    c.sendMessage(Account.fetch(c.getPlayer().getUniqueId()).getLanguage().translate("duels.start_announcement", finalTime));
                    c.playSound(c.getLocation(), Sound.CLICK, 3.5F, 3.5F);
                });
            }
        } else if (stage == RoomStage.PLAYING) {

            if (tioGersonDelay > 0) {
                final Title title = new Title("§c" + tioGersonDelay, "", 1, 15, 10);

                room.getEnzo().getMembers().forEach(c -> {
                    c.getPlayer().sendTitle(title);
                    c.getPlayer().sendMessage("§eO Tio Gerson irá começar a caçar em " + tioGersonDelay + " segundos!");
                    c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.CLICK, 3.5F, 3.5F);
                });

                room.getTioGerson().getMembers().forEach(c -> {
                    c.getPlayer().sendTitle(title);
                    c.getPlayer().sendMessage("§eVocê estará livre para caçar em " + tioGersonDelay + " segundos!");
                    c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.CLICK, 3.5F, 3.5F);

                    c.getPlayer().teleport(room.getMapConfiguration().getTioGersonLocation());
                });
                tioGersonDelay--;
            }

            if (room.getEnzo().getMembers().isEmpty()) {
                room.win(room.getTioGerson());
            }

            if(room.getTioGerson().getMembers().isEmpty()) {
                room.win(room.getEnzo());
            }

            if (room.getTime() == 370) {
                room.getTioGerson().getMembers().forEach(c -> {
                    c.getPlayer().sendTitle(new Title("§4§lDERROTA!", "§eVocê não conseguiu achar todos os ENZOS!", 1, 40, 10));
                    c.getPlayer().sendMessage("§cVocê não conseguiu matar os Enzos a tempo!");
                    c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.ANVIL_LAND, 3.5F, 3.5F);
                });

                room.getEnzo().getMembers().forEach(c -> {
                    c.getPlayer().sendMessage("§aVocê sobreviveu ao Tio Gerson!");
                    c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.LEVEL_UP, 3.5F, 3.5F);
                });

                room.win(room.getEnzo());
            }

            room.setTime(time + 1);
        } else if (stage == RoomStage.ENDING) {

            room.setTime(time = time + 1);

            if (room.getWorld().getPlayers().isEmpty()) {
                room.setWin(-1);
                room.rollback();
            } else if (room.getWin() + 6 == time) {

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.PROTOTYPE);

                room.getWorld().getPlayers().forEach(player -> {

                    final Account account = Account.fetch(player.getUniqueId());

                    if (server != null) {
                        account.connect(server);
                    } else
                        sync(() -> player.kickPlayer(account.getLanguage().translate("arcade.room.not_found")));
                });

                room.setWin(-1);
                room.rollback();
            }
        }

        room.getWorld().getPlayers().forEach(c -> handleSidebar(User.fetch(c.getUniqueId())));
    }

    public void pointCompass(User user) {

        Player comparator = null;
        Player consumer = user.getPlayer();

        for (User other : user.getRoom().getEnzo().getMembers()) {

            if (other.getUniqueId().equals(user.getUniqueId()))
                continue;

            Player player = other.getPlayer();

            if (player.getLocation().distanceSquared(consumer.getLocation()) >= 225) {
                if (comparator == null || comparator.getLocation().distanceSquared(consumer.getLocation()) > player.getLocation().distanceSquared(consumer.getLocation())) {
                    comparator = player;
                }
            }
        }

        if (comparator == null) {
            consumer.sendMessage("§cNenhum jogador encontrado.");
            consumer.setCompassTarget(user.getRoom().getMapConfiguration().getTioGersonLocation());
        } else {
            consumer.setCompassTarget(comparator.getLocation());
            consumer.sendMessage(user.getAccount().getLanguage().translate("hg.game.user.compass_pointing_to", comparator.getName()));
        }
    }

    public void setup(Room room) {

        World world = room.getWorld();

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

        /*int size = room.getMapConfiguration().getSize();

        for (int x = -size; x <= size; x += 16) {
            for (int z = -size; z <= size; z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                chunk.load(false);
            }
        } */

        world.getEntities().forEach(Entity::remove);
    }

    public void join(User user, PlayMode playMode) {

        Player player = user.getPlayer();

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getOpenInventory().getTopInventory().clear();
        player.setLevel(0);
        player.setExp(0);

        if (playMode == PlayMode.PLAYER) {
            player.setGameMode(GameMode.SURVIVAL);
            player.spigot().setCollidesWithEntities(true);
            player.setFlying(false);
            player.setAllowFlight(false);

            if (Vanish.getInstance().isVanished(player.getUniqueId()))
                Vanish.getInstance().setVanished(player, null);

        } else {
            Account account = Account.fetch(player.getUniqueId());

            if (account.hasPermission(Rank.PARTNER_PLUS)) {
                Vanish.getInstance().setVanished(player, account.getRank());
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.spigot().setCollidesWithEntities(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false), true);
            }

            Items.find(account.getLanguage()).build(player);
        }

        Visibility.refresh(player);

        refresh(user);

        if (((CraftPlayer) player).getHandle().playerConnection != null)
            player.getInventory().setHeldItemSlot(2);
    }

    public void quit(User user) {

        Room room = user.getRoom();

        if (room.isSpectator(user)) {
            user.getRoom().getSpectators().remove(user);
        } else {
            new UserDeathEvent(user,null, true).fire();
        }

        Player player = user.getPlayer();

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        player.setFireTicks(0);

        if (room.isLock()) {
            room.setLock(null);
        }

    }

    public void start(Room room) {

    }

    public void handleSidebar(User user) {

        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            user.setScoreboard(gameScoreboard = new GameScoreboard(user.getPlayer()));

        Room room = user.getRoom();

        if (room == null)
            return;

        RoomStage stage = room.getStage();

        List<String> scores = new ArrayList<>();

        int time = room.getTime();
        int teamLimit = room.getMaxPlayers() / 2;

        gameScoreboard.updateTitle("§b§lTIO GERSON");
        scores.add("§8" + room.getCode());
        scores.add(" ");
        scores.add("Mapa: §a" + room.getMapConfiguration().getName());

        if (stage == RoomStage.STARTING || stage == RoomStage.WAITING) {
            scores.add("Jogadores: §a" + room.getAlivePlayers().size() + "/" + room.getMaxPlayers());
            scores.add(" ");
            scores.add(time == -1 ? "Aguardando..." : "Iniciando em §a" + time + "s");
            scores.add(" ");
            scores.add("Modo: §a" + getName());

        } else {
            scores.add("Tempo: §a" + format(time));
            scores.add(" ");
            scores.add("Tios Gerson: §a" + room.getTioGerson().getMembers().size());
            scores.add("Jogadores vivos: §a" + room.getEnzo().getMembers().size());
            scores.add(" ");

        }

        scores.add("Winstreak: §70");

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    public Mode(int maxRooms) {
        this.maxRooms = maxRooms;
        Bukkit.getPluginManager().registerEvents(this, ArcadeMain.getInstance());
    }

    public boolean isCanBuild() {
        return false;
    }

    public boolean isAllowDrops() {
        return false;
    }

    public void refresh(User user) {
        Account account = user.getAccount();
        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);
        new PlayerUpdateTablistEvent(account, tag, prefixType).fire();
    }
}