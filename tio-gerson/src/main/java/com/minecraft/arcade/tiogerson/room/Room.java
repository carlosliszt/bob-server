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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class Room implements BukkitInterface {

    private final Mode mode;
    private final String code;
    private final World world;
    private final Team tioGerson, enzo;
    private final Set<User> spectators;
    private final Set<Block> rollback;
    private int maxPlayers;
    private int win;
    private UUID lock;

    private RoomStage stage;
    private boolean countStats;
    private MapConfiguration mapConfiguration;
    private int time;

    public Room(int id, Mode mode, World world) {
        this.mode = mode;
        this.code = "tG1" + id;
        this.tioGerson = new Team(ChatColor.RED, this, 1);
        this.enzo = new Team(ChatColor.BLUE, this, 7);
        this.spectators = new HashSet<>();
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

    public List<User> getAlivePlayers() { // TODO: Change this
        return Stream.concat(tioGerson.getMembers().stream().filter(user -> user.getUniqueId() != null), enzo.getMembers().stream().filter(user -> user.getUniqueId() != null)).collect(Collectors.toList());
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
            } else {
                if (getTioGerson().isFull())
                    getEnzo().getMembers().add(user);
                else
                    getTioGerson().getMembers().add(user);
            }
        } else {
            getEnzo().getMembers().remove(user);
            getTioGerson().getMembers().remove(user);
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
        mode.start(this);
        tioGerson.getMembers().forEach(c -> {
            c.getPlayer().teleport(getMapConfiguration().getTioGersonLocation());
            //c.getAccount().addInt(1, getMode().getGames());
            Visibility.refresh(c.getPlayer());
            new PlayerUpdateTablistEvent(c.getAccount(), c.getAccount().getProperty("account_tag").getAs(Tag.class), c.getAccount().getProperty("account_prefix_type").getAs(PrefixType.class)).fire();
        });
        enzo.getMembers().forEach(c -> {
            c.getPlayer().teleport(getMapConfiguration().getEnzoLocation());
            //c.getAccount().addInt(1, getMode().getGames());
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
                user.getPlayer().sendTitle(new Title("§2§lVITÓRIA!", "§eVocê sobreviveu ao TIO GERSON!", 1, 15, 10));
            } else {
                user.getPlayer().sendTitle(new Title("§2§lVITÓRIA!", "§eVocê passou encontrou todos os seus SOBRINHOS!", 1, 15, 10));
            }

            Mode mode = getMode();

            if (countStats) {
               // account.addInt(1, mode.getWins());
               // account.addInt(1, mode.getWinstreak());
              /*  if (account.getData(mode.getWinstreak()).getAsInt() > account.getData(mode.getWinstreakRecord()).getAsInt())
                    account.getData(mode.getWinstreakRecord()).setData(account.getData(mode.getWinstreak()).getAsInt());
                async(() -> account.getDataStorage().saveTable(mode.getWins().getTable())); */
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