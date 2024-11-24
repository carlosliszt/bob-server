package com.minecraft.arcade.duels.game.list;

import com.minecraft.arcade.duels.Duels;
import com.minecraft.arcade.duels.game.Game;
import com.minecraft.arcade.duels.user.User;
import com.minecraft.core.bukkit.arcade.game.GameType;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;

import java.util.ArrayList;
import java.util.List;

public class Gladiator extends Game {

    public Gladiator(Duels plugin, Integer minRooms, Integer maxRooms, String mapDirectory) {
        super(plugin, minRooms, maxRooms, GameType.GLADIATOR, mapDirectory);
    }

    @Override
    public void operateScoreboard(User user) {

        GameScoreboard scoreboard = user.getScoreboard();

        if (scoreboard == null)
            user.setScoreboard(scoreboard = new GameScoreboard(user.getPlayer()));

        List<String> stringList = new ArrayList<>();

        scoreboard.updateTitle("§b§lDUELS");
        stringList.add("");
        stringList.add("§fPing: §e" + user.getPlayer().spigot().getPing() + "ms");
        stringList.add("");
        stringList.add("§eblazemc.com.br");

        scoreboard.updateLines(stringList);
    }
}