/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.ranking;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Ranking;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Getter
public class RankingFactory {

    private final RankingTarget target;

    public RankingFactory(RankingTarget target) {
        this.target = target;
    }

    private final Set<RankingHandler> rankingHandlers = new HashSet<>();

    private volatile List<UUID> bestPlayers = new ArrayList<>();

    public void registerRankingHandler(RankingHandler rankingHandler) {
        rankingHandlers.add(rankingHandler);
    }

    public void removeRankingHandler(RankingHandler rankingHandler) {
        rankingHandlers.remove(rankingHandler);
    }

    public void verify(Account account) {
        Ranking ranking = Ranking.fromId(account.getData(target.getRanking()).getAsInt()); // Fetching the real rank.
        int exp = account.getData(target.getExperience()).getAsInt();

        boolean spoofed = ranking.getId() != account.getRanking().getId(); // The player ranking is spoofed. (/nick)

        Ranking previous = ranking.getPreviousRanking();

        Ranking next = ranking.getNextRanking();

        if (ranking == Ranking.MASTER_IV && !spoofed && getBestPlayers().contains(account.getUniqueId())) {
            account.setRanking(Ranking.CHALLENGER);
            getRankingHandlers().forEach(handler -> handler.onChallengerAssign(account));
        } else if (account.getRanking() == Ranking.CHALLENGER && !getBestPlayers().contains(account.getUniqueId()) || account.getRanking() == Ranking.CHALLENGER && ranking != Ranking.MASTER_IV) {
            account.setRanking(ranking);
            getRankingHandlers().forEach(handler -> handler.onChallengerDesign(account));
        } else if (exp >= next.getExperience()) {
            if (!spoofed) {
                account.setRanking(next);
                if (next == Ranking.MASTER_IV && bestPlayers.size() < 5 && !bestPlayers.contains(account.getUniqueId()))
                    bestPlayers.add(account.getUniqueId());
            }
            account.getData(target.getRanking()).setData(next.getId());
            getRankingHandlers().forEach(handler -> handler.onRankingUpgrade(account, ranking, next));
        } else if (exp < ranking.getExperience()) {
            if (!spoofed)
                account.setRanking(previous);
            account.getData(target.getRanking()).setData(previous.getId());
            getRankingHandlers().forEach(handler -> handler.onRankingDowngrade(account, previous));
        }
    }

    public Set<RankingHandler> getRankingHandlers() {
        return rankingHandlers;
    }

    public List<UUID> getBestPlayers() {
        return bestPlayers;
    }

    public void query() {

        Columns ranking = getTarget().getRanking();
        Columns exp = getTarget().getExperience();

        String query = "SELECT `unique_id` FROM `" + ranking.getTable().getName() + "` " +
                "WHERE `" + ranking.getField() + "` IS NOT NULL " +
                "AND `" + exp.getField() + "` IS NOT NULL " +
                "AND `" + ranking.getField() + "` = ? " +
                "ORDER BY `" + exp.getField() + "` DESC " +
                "LIMIT 5;";

        List<UUID> besties = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(query);

            preparedStatement.setInt(1, Ranking.MASTER_IV.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                besties.add(UUID.fromString(resultSet.getString("unique_id")));
            }

            this.bestPlayers = besties;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
