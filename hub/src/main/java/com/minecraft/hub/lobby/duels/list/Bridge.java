package com.minecraft.hub.lobby.duels.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.server.ServerType;
import com.minecraft.hub.Hub;
import com.minecraft.hub.lobby.Lobby;
import com.minecraft.hub.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class Bridge extends Lobby {

    protected final Configuration configuration;

    public Bridge(Hub hub) {
        super(hub, false);

        this.configuration = new Configuration(getWorld());

        Constants.setServerType(ServerType.THE_BRIDGE_LOBBY);
        Constants.setLobbyType(ServerType.DUELS_LOBBY);

        setBossbar("§b§lTHE BRIDGE NO BOBMC.COM");
    }

    @Override
    public void handleScoreboard(User user, String displayName) {
        final GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        gameScoreboard.updateTitle(displayName);

        Collection<String> lines = new ArrayList<>();

        lines.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(lines);
    }

    @Override
    public void handleJoin(Player player) {

    }

    @Getter
    @RequiredArgsConstructor
    public static class Configuration {

        private final World world;

        private final Location soloNpc = new Location(getWorld(), 2.5, 69.5, 28.5, 175, 0);
        private final Location doublesNpc = new Location(getWorld(), -1.5, 69.5, 28.5, -175, 0);
        private final Location merchantNpc = new Location(getWorld(), 18.5, 68, 17.5, 100, 0);

        private final Location bestPlayersHologram = new Location(getWorld(), -18.5, 74, 17.5);

        private final Location firstBestPlayer = new Location(getWorld(), -19.5, 71, 17.5, -90, 0);
        private final Location secondBestPlayer = new Location(getWorld(), -18.5, 70, 19.5, -90, 0);
        private final Location thirdBestPlayer = new Location(getWorld(), -18.5, 69, 15.5, -90, 0);

        private final Location pointsLeaderboard = new Location(getWorld(), -15.5, 71.5, 23.5);
        private final Location roundsLeaderboard = new Location(getWorld(), -15.5, 71.5, 11.5);

        private final Location statisticsNpc = new Location(getWorld(), -2.5, 69, 4.5, -140, 0);

    }

}