package com.minecraft.lobby.hall.types;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.server.ServerType;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import org.bukkit.Location;
import org.bukkit.WorldBorder;

import java.util.ArrayList;
import java.util.List;

public class Prototype extends Hall {

    public Prototype(Lobby lobby) {
        super(lobby, "Prototype Lobby", "prototypelobby", "EM DESENVOLVIMENTO!");

        setSpawn(new Location(getWorld(), 0, 65, 0));
        getLobby().getAccountLoader().addColumns(Columns.TIOGERSON_WINSTREAK, Columns.TIOGERSON_MAX_WINSTREAK);

        Constants.setServerType(ServerType.PROTOTYPE);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(500);

        getWorld().setGameRuleValue("doFireTick", "false");
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lPROTOTYPE");

        DataStorage storage = user.getAccount().getDataStorage();
        int count = Constants.getServerStorage().count();

        scores.add(" ");
        scores.add("§eTio Gerson:");
        scores.add(" §fWins: §b" + storage.getData(Columns.TIOGERSON_WINS).getAsInteger());
        scores.add(" §fWinstreak: §b" + storage.getData(Columns.TIOGERSON_WINSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§eBatata Quente:");
        scores.add(" §fWins: §b0");
        scores.add(" §fWinstreak: §b0");
        scores.add(" ");
        scores.add("§fPlayers: §a" + (count == -1 ? "..." : count));
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void handleNPCs(User user) {

    }
}
