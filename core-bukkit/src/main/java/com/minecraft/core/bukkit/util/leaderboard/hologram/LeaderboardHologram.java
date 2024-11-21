/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.leaderboard.hologram;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardData;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

@Getter
@Setter
public class LeaderboardHologram extends Hologram {

    private final Leaderboard leaderboard;
    private final String title;
    private int page;

    public LeaderboardHologram(Leaderboard leaderboard, String title, Player p, Location location) {
        super(p, location, title, "position1", "position2", "position3", "position4", "position5", "position6", "position7", "position8", "position9", "position10");
        this.title = title;
        this.leaderboard = leaderboard;
        setup();
    }

    public void setup() {

        int maxPages = leaderboard.getLimit() / 10;

        if (maxPages == 1)
            return;

        setInteract((player, hologram, line, type) -> {
            int page = getPage();

            if (page != 0)
                player.playSound(player.getLocation(), Sound.CLICK, 3F, 3.7F);

            if (page != maxPages) {
                page++;
                setPage(page);
            } else {
                setPage(page = 1);
            }

            hologram.updateText(0, String.format(getTitle(), page, leaderboard.getLimit() / 10));

            List<LeaderboardData> datas = getLeaderboard().values();

            int x = 1;

            for (int i = (10 * page) - 9; i < 10 * page + 1; i++) {
                String text = "§e" + i + ". ...";

                if (i <= datas.size()) {
                    LeaderboardData data = datas.get(i - 1);

                    JsonArray jsonArray = data.getValue(Columns.RANKS).getAsJsonArray();
                    Iterator<JsonElement> iterator = jsonArray.iterator();

                    Rank currentRank = Rank.MEMBER;

                    while (iterator.hasNext()) {

                        JsonObject object = iterator.next().getAsJsonObject();

                        String code = object.get("rank").getAsString();
                        long expiration = object.get("expiration").getAsLong();

                        Rank rank = Rank.fromUniqueCode(code);

                        if (rank == null) {
                            iterator.remove();
                            continue;
                        }

                        if (expiration != -1 && expiration < System.currentTimeMillis()) {
                            iterator.remove();
                            continue;
                        }

                        if (rank.getId() > currentRank.getId()) {
                            currentRank = rank;
                        }

                    }

                    text = "§e" + i + ". " + currentRank.getDefaultTag().getFormattedColor() + data.getName() + " §7- §e" + data.getStatistic().getAsObject();
                }

                hologram.updateText(x, text);
                x++;
            }
        });
    }

    @Override
    public void show() {
        super.show();
        this.getInteract().handle(getTarget().getPlayer(), this, 0, null);
    }

    @Override
    public void hide() {
        super.hide();
        this.page = 0;
    }
}
