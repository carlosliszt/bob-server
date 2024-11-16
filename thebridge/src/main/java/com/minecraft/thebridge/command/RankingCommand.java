/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.thebridge.command;

import com.google.common.base.Strings;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.thebridge.TheBridge;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Iterator;

public class RankingCommand implements BukkitInterface {

    @Command(name = "ranking", platform = Platform.PLAYER, aliases = "rank")
    public void rankingCommand(Context<Player> context) {
        Account account = context.getAccount();
        RankingTarget target = TheBridge.getInstance().getRankingFactory().getTarget();

        Ranking ranking = account.getRanking();
        Ranking next = ranking.getNextRanking();

        int exp = account.getData(target.getExperience()).getAsInt();
        String expFormatted = account.getData(target.getExperience()).getAsInteger();

        context.sendMessage("§aSeu ranking: §f" + ranking.getColor() + ranking.getSymbol() + ' ' + ranking.getName());
        if (next.getId() != ranking.getId() && next.getId() != Ranking.CHALLENGER.getId())
            context.sendMessage("§aPróximo ranking: §f" + next.getColor() + next.getSymbol() + ' ' + next.getName());
        context.sendMessage("§aExpieriência total: §f" + expFormatted);

        if (next.getId() != Ranking.CHALLENGER.getId()) {
            int comparatorExperience = exp - ranking.getExperience();

            String progress = ranking.getColor() + ranking.getDisplay() + ranking.getSymbol() + " " + progress(comparatorExperience, next.getExperience(), 30) + "§r> " + next.getColor() + next.getDisplay() + next.getSymbol() + " §7" + expFormatted + "/" + Constants.DECIMAL_FORMAT.format(next.getExperience()) + " (" + ((exp * 100) / next.getExperience()) + "%)";
            context.sendMessage(progress);
        }
    }

    @Command(name = "ranks", platform = Platform.PLAYER, aliases = "ranklist")
    public void rankingsCommand(Context<Player> context) {

        Iterator<Ranking> iterator = Arrays.stream(Ranking.values()).iterator();

        StringBuilder builder = new StringBuilder("§aRankings: ");

        while (iterator.hasNext()) {

            Ranking ranking = iterator.next();

            builder.append(ranking.getColor()).append(ranking.getDisplay()).append(ranking.getSymbol());

            if (iterator.hasNext())
                builder.append("§f, ");
        }
        context.sendMessage(builder.toString());
    }

    public String progress(int current, int max, int totalBars) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        return Strings.repeat("§a§m ", progressBars)
                + Strings.repeat("§f§m ", totalBars - progressBars);
    }

}