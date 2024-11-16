package com.minecraft.hub.lobby.pvp;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.server.ServerType;
import com.minecraft.hub.Hub;
import com.minecraft.hub.lobby.Lobby;
import com.minecraft.hub.user.User;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class PvP extends Lobby {

    public PvP(Hub hub) {
        super(hub, true);

        Constants.setServerType(ServerType.PVP_LOBBY);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        setBossbar("§b§lPVP NO BOBMC.COM");
    }

    @Override
    public void handleScoreboard(User user, String displayName) {
        final GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        gameScoreboard.updateTitle(displayName);

        Collection<String> lines = new ArrayList<>();

        final int count = Constants.getServerStorage().count();

        lines.add(" ");
        lines.add("§eArena:");
        lines.add(" §fKills: §b0");
        lines.add(" §fKillstreak: §b0");
        lines.add(" ");
        lines.add("§eFps:");
        lines.add(" §fKills: §b0");
        lines.add(" §fKillstreak: §b0");
        lines.add(" ");
        lines.add("§fCoins: §60");
        lines.add("§fPlayers: §a" + (count == -1 ? "..." : count));
        lines.add(" ");
        lines.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(lines);
    }

    @Override
    public void handleJoin(Player player) {

    }

    @Getter
    @Setter
    public static class Configuration {

        private Location bestPlayersHologram;

        private Location firstBestPlayer;
        private Location secondBestPlayer;
        private Location thirdBestPlayer;

        private Location arenaMaxKillstreakLeaderboard;
        private Location fpsMaxKillstreakLeaderboard;

        private Location statisticsNpc;

        private Location arenaNpc;
        private Location fpsNpc;
        private Location lavaNpc;
        private Location damageNpc;

    }

}