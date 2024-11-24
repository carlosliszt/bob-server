/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.game;

import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class GameVariables implements VariableStorage {

    @Variable(name = "hg.timer.invincibility")
    private int invincibility = 120;

    @Variable(name = "hg.timer.feast")
    private int feast = 750;

    @Variable(name = "hg.timer.bonus_feast")
    private int extraFeast = 1680;

    @Variable(name = "hg.timer.timeout")
    private int timeout = 40;

    @Variable(name = "hg.timer.final_battle")
    private int finalArena = 2100;

    @Variable(name = "hg.timer.final_combat")
    private int finalCombat = 2700;

    @Variable(name = "hg.timer.most_killer_end")
    private int mostEndGame = 3000;

    @Variable(name = "hg.timer.mini_feast_interval")
    private int miniFeastDelay = 300;

    @Variable(name = "hg.structure.feast")
    private boolean feastSpawn = true;

    @Variable(name = "hg.structure.minifeast")
    private boolean minifeastSpawn = true;

    @Variable(name = "hg.structure.final_battle")
    private boolean finalArenaSpawn = true;

    @Variable(name = "hg.structure.final_combat")
    private boolean finalCombatSpawn = true;

    @Variable(name = "hg.structure.bonus_feast")
    private boolean extraFeastSpawn = true;

    @Variable(name = "hg.most_killer_end")
    private boolean mostEndGameMechanic = true;

    @Variable(name = "hg.border.max_y", permission = Rank.SECONDARY_MOD)
    private int worldHeight = 128;

    @Variable(name = "hg.border.max_radius", permission = Rank.SECONDARY_MOD)
    private int worldSize = 404;

    @Variable(name = "hg.min_players", permission = Rank.PRIMARY_MOD)
    private int minimumPlayers = 5;

    @Variable(name = "hg.pregame_tpall", permission = Rank.SECONDARY_MOD)
    private boolean teleportAll = true;

    @Variable(name = "hg.count_stats", permission = Rank.PRIMARY_MOD)
    private boolean countStats = true;

    private transient Location defaultSpawnpoint, spawnpoint;

    private int spawnRange = 5;

    public GameVariables() {
        loadVariables();
    }
}
