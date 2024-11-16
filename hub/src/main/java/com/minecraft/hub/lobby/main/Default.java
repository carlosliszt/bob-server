package com.minecraft.hub.lobby.main;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.scoreboard.AnimatedString;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.enums.Tag;
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

public class Default extends Lobby {

    public Default(Hub hub) {
        super(hub, true);

        setSpawn(new Location(getWorld(), 0.5, 70, 0.5));
        Constants.setServerType(ServerType.DOUBLEKIT);

        setAnimatedString(new AnimatedString(Constants.SERVER_NAME.toUpperCase(), "§e§l", "§f§l", "§b§l"));

        setBossbar("§b§lJOGANDO NO BOBMC.COM");
    }

    @Override
    public void handleScoreboard(User user, String displayName) {
        final GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        gameScoreboard.updateTitle(displayName);

        Collection<String> lines = new ArrayList<>();

        final Tag tag = user.getAccount().getTagList().getHighestTag();
        final int playerCount = Constants.getServerStorage().count();

        lines.add(" ");
        lines.add("§fRank: §r" + tag.getColor() + tag.getName());
        lines.add(" ");
        lines.add("§fLobby: §7#" + getId());
        lines.add("§fPlayers: §a" + (playerCount == -1 ? "..." : playerCount));
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

        private Location hgNpc;
        private Location pvpNpc;
        private Location duelsNpc;
        private Location bridgeNpc;

        private Location parkourHologram;

        private Location bansLeaderboard;
        private Location mutesLeaderboard;
        private Location eventLeaderboard;

    }

}