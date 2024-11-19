package com.minecraft.arcade.tiogerson.room;

import com.minecraft.arcade.tiogerson.config.MapConfiguration;
import com.minecraft.arcade.tiogerson.mode.Mode;
import com.minecraft.arcade.tiogerson.room.team.Team;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.util.enums.Items;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import com.minecraft.arcade.tiogerson.util.visibility.Visibility;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class Room implements BukkitInterface {

    private final Mode mode;
    private final String code;
    private final World world;
    private final Team tioGerson, enzo;
    private static final int CODE_LENGTH = 5;
    private final Set<User> spectators;
    private final Set<Block> rollback;
    private int maxPlayers;
    private int win;
    private UUID lock;

    private RoomStage stage;
    private boolean countStats;
    private MapConfiguration mapConfiguration;
    private int time;

    private static final String CHARACTERS = "abcdefghijklmnopqrtsuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Set<User> alivePlayers;
    private static final Random RANDOM = new Random();

    public static String generateRoomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public Room(int id, Mode mode, World world) {
        this.mode = mode;
        this.code = generateRoomCode() + id;
        this.tioGerson = new Team(ChatColor.RED, this, 1);
        this.enzo = new Team(ChatColor.BLUE, this, 7);
        this.spectators = new HashSet<>();
        this.alivePlayers = new HashSet<>();
        this.rollback = new HashSet<>();
        this.stage = RoomStage.WAITING;
        this.maxPlayers = 8;
        this.time = -1;
        this.win = -1;
        this.countStats = true;
        this.world = world;
    }

    public boolean isSpectator(User user) {
        return spectators.contains(user);
    }

    public void rollback() {
        Bukkit.getConsoleSender().sendMessage("§aThe room §f" + getMode().getClass().getSimpleName() + "(" + getCode() + ")§a is restarting...");
        setStage(RoomStage.ROLLBACKING);
        this.rollback.forEach(block -> block.setType(Material.AIR));
        this.rollback.clear();
        getSpectators().clear();
        getEnzo().getMembers().clear();
        getTioGerson().getMembers().clear();
        setTime(-1);
        setStage(RoomStage.WAITING);
        getWorld().getEntitiesByClasses(Item.class).forEach(Entity::remove);
    }

    public boolean isFull() {
        return enzo.isFull() && tioGerson.isFull();
    }

    public void join(User user, PlayMode playMode, boolean teleport) {

        if (user.getRoom() != null)
            user.getRoom().getMode().quit(user);

        Player player = user.getPlayer();
        Account account = user.getAccount();

        if (playMode == PlayMode.PLAYER) {

            getSpectators().remove(user);

            if (isFull()) {
                player.sendMessage(account.getLanguage().translate("arcade.room.full", getCode()));
                user.lobby();
            } else if (getStage() != RoomStage.WAITING) {
                player.sendMessage(account.getLanguage().translate("arcade.room.already_started", getCode()));
                user.lobby();
            }

            getAlivePlayers().add(user);

        } else {
            getAlivePlayers().remove(user);
            getSpectators().add(user);
        }

        if (teleport) {
            player.teleport(getMapConfiguration().getSpawnPoint());
        }

        user.setRoom(this);
        getMode().join(user, playMode);
    }

    public void start() {
        this.stage = RoomStage.PLAYING;
        this.time = 1;

        List<User> shuffledPlayers = new ArrayList<>(getAlivePlayers());
        Collections.shuffle(shuffledPlayers);

        boolean firstUser = true;
        for (User user : shuffledPlayers) {
            if (firstUser) {
                getTioGerson().getMembers().add(user);
                firstUser = false;
            } else if (getTioGerson().isFull()) {
                getEnzo().getMembers().add(user);
            } else if (getEnzo().isFull()) {
                getTioGerson().getMembers().add(user);
            } else {
                if (new Random().nextBoolean()) {
                    getTioGerson().getMembers().add(user);
                } else {
                    getEnzo().getMembers().add(user);
                }
            }
        }

        mode.start(this);

        tioGerson.getMembers().forEach(c -> {
            c.getPlayer().teleport(getMapConfiguration().getTioGersonLocation());
            c.getPlayer().setGameMode(GameMode.SURVIVAL);
            c.getAccount().addInt(1, getMode().getGames());
            Visibility.refresh(c.getPlayer());
            new PlayerUpdateTablistEvent(c.getAccount(), c.getAccount().getProperty("account_tag").getAs(Tag.class), c.getAccount().getProperty("account_prefix_type").getAs(PrefixType.class)).fire();
        });
        enzo.getMembers().forEach(c -> {
            c.getPlayer().teleport(getMapConfiguration().getEnzoLocation());
            c.getPlayer().setGameMode(GameMode.SURVIVAL);
            c.getAccount().addInt(1, getMode().getGames());
            Visibility.refresh(c.getPlayer());
            new PlayerUpdateTablistEvent(c.getAccount(), c.getAccount().getProperty("account_tag").getAs(Tag.class), c.getAccount().getProperty("account_prefix_type").getAs(PrefixType.class)).fire();
        });
        world.getPlayers().forEach(player -> player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 1F));
    }

    public void win(Team team) {
        getWorld().getEntitiesByClasses(Item.class).forEach(Entity::remove);
        setStage(RoomStage.ENDING);
        setWin(getTime());
        getAlivePlayers().forEach(user -> {
            Player player = user.getPlayer();
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setHeldItemSlot(2);
            Items.find(user.getAccount().getLanguage()).build(player);

            Account account = user.getAccount();

            if(team == getEnzo()) {
                user.getPlayer().sendTitle(new Title("§2§lVITÓRIA!", "§eVocê sobreviveu ao TIO GERSON!", 1, 40, 10));
            } else {
                user.getPlayer().sendTitle(new Title("§2§lVITÓRIA!", "§eVocê encontrou todos os seus SOBRINHOS!", 1, 40, 10));
            }

            Mode mode = getMode();

            if (countStats) {
                account.addInt(1, mode.getWins());
                account.addInt(1, mode.getWinstreak());
                if (account.getData(mode.getWinstreak()).getAsInt() > account.getData(mode.getWinstreakRecord()).getAsInt())
                    account.getData(mode.getWinstreakRecord()).setData(account.getData(mode.getWinstreak()).getAsInt());
                async(() -> account.getDataStorage().saveTable(mode.getWins().getTable()));
            }
        });

        getSpectators().forEach(user -> {
            if (user.getPreviousTeam() != null && user.getPreviousTeam() != team && team != user.getRoom().getTioGerson()) {
                user.getPlayer().sendTitle(new Title("§c§lDERROTA!", "§eVocê não sobreviveu até o final!", 1, 40, 10));
            }
        });

        setCountStats(true);
    }

    @Override
    public String toString() {
        int limit = getMaxPlayers() / 2;
        return "(" + getCode() + ") " + getMode().getClass().getSimpleName() + " " + limit + "v" + limit;
    }

    public boolean isOutside(Location location) {
        MapConfiguration mapConfiguration = getMapConfiguration();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return y >= mapConfiguration.getHeight();
    }

    public boolean isLock() {
        return lock != null;
    }

}