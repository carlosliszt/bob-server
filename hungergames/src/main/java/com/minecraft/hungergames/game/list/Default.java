/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.list;

import com.minecraft.core.Constants;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.Loadable;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;

@Loadable
public class Default extends Game {

    public Default(HungerGames hungerGames) {
        super(hungerGames);
        Constants.setServerType(ServerType.HGMIX);
        hungerGames.setRankingFactory(new RankingFactory(RankingTarget.HG));
        hungerGames.getAccountLoader().addColumns(Columns.HG_RANK, Columns.HG_RANK_EXP);
    }

    @Override
    public void load() {
        super.load();
        setName(getType().getName() + ": #" + getRoom());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (getStage() == GameStage.WAITING)
            return;

        if (getGame().getStage() == GameStage.PLAYING && getTime() > 300)
            return;

        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL)
            return;

        if (event.getBlock().getType().name().contains("ORE")) {
            event.getPlayer().sendMessage("§cMinérios só podem ser quebrados após os 5 minutos de jogo.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        if (getStage() == GameStage.WAITING)
            return;

        if (getGame().getStage() == GameStage.PLAYING && getTime() > 300)
            return;

        event.blockList().removeIf(block -> block.getType().name().contains("ORE"));
    }

}