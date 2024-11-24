package com.minecraft.duels.mode;

import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.compression.WinRAR;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.duels.Duels;
import com.minecraft.duels.event.player.UserDeathEvent;
import com.minecraft.duels.map.config.MapConfiguration;
import com.minecraft.duels.room.Room;
import com.minecraft.duels.room.generator.VoidGenerator;
import com.minecraft.duels.user.User;
import com.minecraft.duels.util.enums.Items;
import com.minecraft.duels.util.enums.RoomStage;
import com.minecraft.duels.util.visibility.Visibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Getter
public abstract class Mode implements BukkitInterface, Listener {

    private static final Duels plugin = Duels.getInstance();

    @Setter
    private String name = getClass().getSimpleName();
    private final Set<DuelType> supportedModes;
    private final int maxRooms;
    private JsonObject jsonReader;
    private World world;
    @Setter
    private Columns wins, loses, winstreak, winstreakRecord, games, rating;

    Leaderboard winLeaderboard;
    Leaderboard winstreakLeaderboard;

    private ArrayList<LeaderboardHologram> leaderboardHolograms = new ArrayList<>();

    public void load() {

        String name = getClass().getSimpleName();

        plugin.getLogger().info("Loading " + name);

        List<File> maps = new ArrayList<>();

        File directory = new File(System.getProperty("user.home") + File.separator + "misc" + File.separator + "duels" + File.separator + name.toLowerCase());
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
                mapConfiguration.setSize(json.get("size").getAsInt());
                mapConfiguration.setHeight(json.get("max_y").getAsInt());

                String rawSpawn = json.get("spawn").getAsString();
                List<Integer> integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 3) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    Duels.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                mapConfiguration.setSpawnPoint(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.5, integerList.get(2) + 0.5));

                rawSpawn = json.get("red_location").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    Duels.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                mapConfiguration.setRedLocation(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));

                rawSpawn = json.get("blue_location").getAsString();
                integerList = Arrays.stream(rawSpawn.split(",")).map(Integer::valueOf).collect(Collectors.toList());

                if (integerList.size() != 5) {
                    System.out.println("Map " + mapConfiguration.getName() + " is broken! Skipping it...");
                    File folder = world.getWorldFolder();
                    Bukkit.unloadWorld(world, false);
                    Duels.getInstance().getRoomStorage().delete(folder);
                    continue;
                }

                mapConfiguration.setBlueLocation(new Location(world, integerList.get(0) + 0.5, integerList.get(1) + 0.1, integerList.get(2) + 0.5, integerList.get(3), integerList.get(4)));
                room.setMapConfiguration(mapConfiguration);
                setup(room);
            } catch (Exception e) {
                System.out.println("Failed to load map " + map.getName());
                e.printStackTrace();
                continue;
            }

            plugin.getRoomStorage().register(room);

            plugin.getLogger().info("Loaded " + room.getCode() + " for " + name);

            winLeaderboard = new Leaderboard(wins, LeaderboardUpdate.MINUTE, LeaderboardType.PLAYER, 20, Columns.USERNAME, Columns.RANKS).query();
            winstreakLeaderboard = new Leaderboard(winstreak, LeaderboardUpdate.MINUTE, LeaderboardType.PLAYER, 20, Columns.USERNAME, Columns.RANKS).query();
        }
    }

    public void tick(Room room) {

        RoomStage stage = room.getStage();
        boolean isFull = room.isFull();
        int time = room.getTime();

        if (stage == RoomStage.WAITING && isFull) {
            room.setStage(RoomStage.STARTING);
            room.setTime(4);
        } else if (stage == RoomStage.STARTING) {

            if (!isFull) {
                room.setStage(RoomStage.WAITING);
                room.setTime(-1);
                return;
            }

            room.setTime(time = time - 1);

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

            if (room.getRed().getMembers().isEmpty()) {
                room.win(room.getBlue());
            } else if (room.getBlue().getMembers().isEmpty()) {
                room.win(room.getRed());
            }

            room.setTime(time + 1);
        } else if (stage == RoomStage.ENDING) {

            room.setTime(time = time + 1);

            if (room.getWorld().getPlayers().isEmpty()) {
                room.setWin(-1);
                room.rollback();
            } else if (room.getWin() + 6 == time) {

                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.DUELS_LOBBY);

                room.getWorld().getPlayers().forEach(player -> {

                    final Account account = Account.fetch(player.getUniqueId());

                    if (server != null) {
                        account.connect(server);
                    } else
                        sync(() -> player.kickPlayer(account.getLanguage().translate("arcade.room.not_found")));
                });
                /*  });*/

                room.setWin(-1);
                room.rollback();
            }
        }

        room.getWorld().getPlayers().forEach(c -> handleSidebar(User.fetch(c.getUniqueId())));
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

        int size = room.getMapConfiguration().getSize();

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

        LeaderboardHologram leaderboardHologram1 = new LeaderboardHologram(winLeaderboard, "§e§lTOP 20 §b§l" + name + " WINS §7(%s/%s)", player, user.getRoom().getMapConfiguration().getSpawnPoint().clone().add(-2, 2.5, 0));
        LeaderboardHologram leaderboardHologram2 = new LeaderboardHologram(winstreakLeaderboard, "§e§lTOP 20 §b§l" + name + " WINSTREAK §7(%s/%s)", player, user.getRoom().getMapConfiguration().getSpawnPoint().clone().add(2, 2.5, 0));

        leaderboardHolograms.add(leaderboardHologram1);
        leaderboardHolograms.add(leaderboardHologram2);

        if (playMode == PlayMode.PLAYER) {
            player.setGameMode(GameMode.SURVIVAL);
            player.spigot().setCollidesWithEntities(true);
            player.setFlying(false);
            player.setAllowFlight(false);

            if (Vanish.getInstance().isVanished(player.getUniqueId()))
                Vanish.getInstance().setVanished(player, null);

            leaderboardHologram1.show();
            leaderboardHologram2.show();

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

            if (!leaderboardHologram1.isHidden()) {
                leaderboardHologram1.hide();
            }
            if (!leaderboardHologram2.isHidden()) {
                leaderboardHologram2.hide();
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
            new UserDeathEvent(user, true).fire();
        }

        Player player = user.getPlayer();

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        player.setFireTicks(0);

        if (room.isLock()) {
            room.setLock(null);
        }

        user.setBoxingHits(0);
    }

    public void start(Room room) {
        room.getAlivePlayers().forEach(user -> {
            user.setBoxingHits(0);
        });

        for (LeaderboardHologram leaderboardHologram : leaderboardHolograms) {
            System.out.println("Hiding hologram: " + leaderboardHologram);
            leaderboardHologram.hide();
        }
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

        gameScoreboard.updateTitle("§b§lDUELS");
        scores.add("§8" + room.getCode());
        scores.add(" ");
        scores.add("Modo: §a" + getName() + " " + teamLimit + "v" + teamLimit);

        if (stage == RoomStage.STARTING || stage == RoomStage.WAITING) {
            scores.add("Jogadores: §a" + room.getAlivePlayers().size() + "/" + room.getMaxPlayers());
            scores.add(" ");
            scores.add(time == -1 ? "Aguardando..." : "Iniciando em §a" + time + "s");
        } else {
            scores.add("Tempo: §a" + format(time));
            scores.add(" ");
            room.getRed().getMembers().forEach(red -> scores.add("§c" + red.getName() + ": §7" + red.getPlayer().spigot().getPing() + "ms"));
            room.getBlue().getMembers().forEach(blue -> scores.add("§9" + blue.getName() + ": §7" + blue.getPlayer().spigot().getPing() + "ms"));
        }

        scores.add(" ");
        scores.add("Rating: §a" + user.getAccount().getData(getRating()).getAsInteger());
        scores.add("Winstreak: §7" + user.getAccount().getData(getWinstreak()).getAsInteger());

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    public Mode(int maxRooms, DuelType... duelTypes) {
        this.maxRooms = maxRooms;
        this.supportedModes = new HashSet<>(Arrays.asList(duelTypes));
        Bukkit.getPluginManager().registerEvents(this, Duels.getInstance());
    }

    public boolean isCanBuild() {
        return true;
    }

    public boolean isAllowDrops() {
        return true;
    }

    public void refresh(User user) {
        Account account = user.getAccount();
        Tag tag = account.getProperty("account_tag").getAs(Tag.class);
        PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);
        new PlayerUpdateTablistEvent(account, tag, prefixType).fire();
    }
}