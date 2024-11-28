package com.minecraft.thebridge.game;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.server.thebridge.GameType;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.thebridge.event.GameEndEvent;
import com.minecraft.thebridge.event.GameStartEvent;
import com.minecraft.thebridge.event.UserJoinGameEvent;
import com.minecraft.thebridge.event.UserLeaveGameEvent;
import com.minecraft.thebridge.game.enums.GameStage;
import com.minecraft.thebridge.game.task.CageTask;
import com.minecraft.thebridge.team.Team;
import com.minecraft.thebridge.user.User;
import com.minecraft.thebridge.util.map.GameConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class Game implements BukkitInterface {

    private final String code;
    private final World world;
    private final GameType type;
    private final int maxPlayers;

    private final Set<Block> rollback;
    private final HashMap<Block, Byte> bridgeBlocks;

    private Team blue, red, gray;
    private int time, rollbackTime, cageTime;

    private GameStage stage;
    private final GameConfiguration configuration;

    private UUID lock;
    private boolean countStats;

    public Game(final String code, final World world, final GameType gameType) {
        this.code = code;
        this.world = world;

        this.type = gameType;
        this.maxPlayers = gameType.getMaxPlayers();

        this.rollback = new HashSet<>();
        this.bridgeBlocks = new HashMap<>();

        this.time = -1;
        this.rollbackTime = -1;
        this.cageTime = -1;

        this.stage = GameStage.WAITING;
        this.configuration = new GameConfiguration();

        this.countStats = true;

        this.blue = new Team(this, "Azul", ChatColor.BLUE);
        this.red = new Team(this, "Vermelho", ChatColor.RED);
        this.gray = new Team(this, "Cinza", ChatColor.GRAY);
    }

    public void rollBack() {
        setStage(GameStage.ROLLBACKING);

        this.rollbackTime = -1;
        this.cageTime = -1;

        this.rollback.forEach(block -> block.setType(Material.AIR));
        this.rollback.clear();

        this.blue.clear();
        this.red.clear();
        this.gray.clear();

        this.configuration.getBlueBlockPortals().forEach(block -> block.setType(Material.AIR));
        this.configuration.getRedBlockPortals().forEach(block -> block.setType(Material.AIR));

        this.bridgeBlocks.forEach((block, data) -> {
            block.setType(Material.STAINED_CLAY);
            block.setData(data);
        });

        this.bridgeBlocks.clear();

        this.lock = null;
        this.countStats = true;

        this.time = -1;
        this.world.getEntitiesByClasses(Item.class, Arrow.class).forEach(Entity::remove);

        setStage(GameStage.WAITING);
    }

    public void tick() {
        GameStage stage = getStage();

        if (stage == GameStage.WAITING && isFull()) {
            setStage(GameStage.STARTING);
            this.time = 4;
        } else if (stage == GameStage.STARTING) {

            if (!isFull()) {
                setStage(GameStage.WAITING);
                this.time = -1;
                return;
            }

            this.time--;

            if (this.time <= 0)
                start();
            else if (time <= 3) {
                sendTitle(new Title("§c" + this.time, "", 1, 15, 10));
                sendMessage("§eA partida iniciará em §c" + this.time + "s");
                playSound(Sound.CLICK, 3.5F, 3.5F);
            }
        } else if (stage == GameStage.PLAYING) {
            this.time--;

            if (this.time <= 0) {
                this.end(null, null, GameEndEvent.Reason.TIME);
                return;
            }

            checkWin();
        } else if (stage == GameStage.ENDING) {
            this.time++;

            if (this.world.getPlayers().isEmpty()) {
                rollBack();
                return;
            }

            if (this.rollbackTime + 6 == this.time) {
                Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.THE_BRIDGE_LOBBY);

                this.world.getPlayers().forEach(player -> {

                    final Account account = Account.fetch(player.getUniqueId());

                    if (server != null) {
                        account.connect(server);
                    } else
                        sync(() -> player.kickPlayer(account.getLanguage().translate("arcade.room.not_found")));
                });

                rollBack();
            }
        }

        this.world.getPlayers().forEach(this::handleSidebar);
    }

    public void handleSidebar(final Player player) {
        final User user = User.fetch(player.getUniqueId());

        Game game = user.getGame();

        if (game == null)
            return;

        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            user.setScoreboard(gameScoreboard = new GameScoreboard(user.getPlayer()));

        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lTHE BRIDGE");
        scores.add("§8" + game.getCode());
        scores.add(" ");

        final GameStage stage = game.getStage();
        boolean bool = stage == GameStage.STARTING || stage == GameStage.WAITING;

        if (bool) {
            scores.add("Jogadores: §a" + game.getAliveUsers().size() + "/" + game.getMaxPlayers());
            scores.add(" ");
            scores.add(time == -1 ? "Aguardando..." : "Iniciando em §a" + time + "s");
        } else {
            scores.add("Acaba em: §a" + format(time));
            scores.add(" ");

            final Team blue = game.getBlue();
            final Team red = game.getRed();

            final StringBuilder blueString = new StringBuilder(), redString = new StringBuilder();

            for (int i = 0; i < 5; i++) {
                if (blue.getPoints() > i)
                    blueString.append(blue.getChatColor()).append("⬤");
                else
                    blueString.append("§7⬤");
                if (red.getPoints() > i)
                    redString.append(red.getChatColor()).append("⬤");
                else
                    redString.append("§7⬤");
            }

            scores.add(red.getChatColor() + "[V] " + redString);
            scores.add(blue.getChatColor() + "[A] " + blueString);
            scores.add(" ");
            scores.add("Kills: §a" + user.getKills());
            scores.add("Pontos: §a" + user.getPoints());
        }

        scores.add(" ");
        scores.add("Winstreak: §7" + user.getAccount().getData(getType().getWinstreak()).getAsInteger());
        if (bool)
            scores.add("Mapa: §a" + game.getConfiguration().getName());
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    public void start() {
        setStage(GameStage.PLAYING);
        this.time = 900;

        final GameStartEvent gameStartEvent = new GameStartEvent(this);
        gameStartEvent.fire();

        this.world.getPlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 1F);

            final Account account = Account.fetch(player.getUniqueId());

            new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class)).fire();
        });

        new CageTask(this).run();
    }

    public void join(final User user, final PlayMode mode, final boolean teleport) {
        final UserJoinGameEvent userJoinGameEvent = new UserJoinGameEvent(user, this, mode, teleport);
        userJoinGameEvent.fire();
    }

    public void quit(final User user) {
        final UserLeaveGameEvent userLeaveGameEvent = new UserLeaveGameEvent(user, this, user.getTeam());
        userLeaveGameEvent.fire();
    }

    public void findTeam(final User user) {
        final Team blue = this.getBlue(), red = this.getRed();

        if (blue.isFull()) {
            user.setTeam(red);
            red.getUsers().add(user);
        } else {
            blue.getUsers().add(user);
            user.setTeam(blue);
        }
    }

    public void end(final Team team, final Team loser, final GameEndEvent.Reason reason) {
        setStage(GameStage.ENDING);
        setRollbackTime(getTime());

        final GameEndEvent gameEndEvent = new GameEndEvent(this, reason, team, loser);
        gameEndEvent.fire();
    }

    public void checkWin() {
        if (this.stage != GameStage.PLAYING)
            return;

        Team winner = null;
        Team loser = null;

        GameEndEvent.Reason reason = null;

        if (this.blue.getPoints() >= 5) {
            winner = getBlue();
            loser = getRed();
            reason = GameEndEvent.Reason.POINTS;
        } else if (this.red.getPoints() >= 5) {
            winner = getRed();
            loser = getBlue();
            reason = GameEndEvent.Reason.POINTS;
        } else if (this.red.getUsers().isEmpty()) {
            winner = getBlue();
            loser = getRed();
            reason = GameEndEvent.Reason.LOGOUT;
        } else if (this.blue.getUsers().isEmpty()) {
            winner = getRed();
            loser = getBlue();
            reason = GameEndEvent.Reason.LOGOUT;
        }

        if (winner == null || loser == null)
            return;

        Team finalWinner = winner;
        Team finalLoser = loser;
        GameEndEvent.Reason finalReason = reason;

        this.end(finalWinner, finalLoser, finalReason);
    }

    public void sendMessage(final String message) {
        this.world.getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void sendTitle(final Title title) {
        this.world.getPlayers().forEach(player -> player.sendTitle(title));
    }

    public void playSound(final Sound sound, final float var, final float var1) {
        this.world.getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, var, var1));
    }

    public boolean isFull() {
        return blue.isFull() && red.isFull();
    }

    public List<User> getAliveUsers() {
        return Stream.concat(red.getUsers().stream().filter(user -> user.getUniqueId() != null), blue.getUsers().stream().filter(user -> user.getUniqueId() != null)).collect(Collectors.toList());
    }

    public boolean isSpectator(User user) {
        return gray.getUsers().contains(user);
    }

    public boolean isSameTeam(User user1, User user2) {
        return user1.getTeam().getChatColor().getChar() == user2.getTeam().getChatColor().getChar();
    }

    public boolean isLock() {
        return lock != null;
    }

}