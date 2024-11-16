/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.hall.types;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.bukkit.event.server.ServerPayloadReceiveEvent;
import com.minecraft.core.bukkit.server.hologram.InfoHologram;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.LeaderboardHandler;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardData;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class PvP extends Hall {

    private final Server server;

    private final Leaderboard max_killstreak = new Leaderboard(Columns.PVP_ARENA_MAX_KILLSTREAK, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Location max_killstreak_location;

    private final Leaderboard max_killstreak_fps = new Leaderboard(Columns.PVP_FPS_MAX_KILLSTREAK, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Location max_killstreak_location_fps;

    private final Leaderboard ranking = new Leaderboard(Columns.PVP_RANK_EXP, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 3, Columns.USERNAME, Columns.PVP_RANK, Columns.SKIN).registerHandler(new LeaderboardHandler() {
        @Override
        public void onUpdate() {

        }
    }).query();

    private final Location bestPlayers_location;
    private final Location loc1, loc2, loc3;

    public PvP(Lobby lobby) {
        super(lobby, "PvP Lobby", "pvplobby", "PVP NO BOBMC.COM");

        setSpawn(new Location(getWorld(), 0.5, 70, 0.5));
        getLobby().getAccountLoader().addColumns(Columns.PVP_ARENA_KILLS, Columns.PVP_ARENA_KILLSTREAK, Columns.PVP_FPS_KILLS, Columns.PVP_FPS_KILLSTREAK, Columns.PVP_KITS, Columns.PVP_COINS);

        Constants.setServerType(ServerType.PVP_LOBBY);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);
        server = Constants.getServerStorage().getServer("pvp");

        max_killstreak_location_fps = new Location(this.getWorld(), -9.5, 71.5, 5.5);
        max_killstreak_location = new Location(this.getWorld(), -9.5, 71.5, 17.5);

        bestPlayers_location = new Location(getWorld(), -12.5, 74.5, 11.5);

        loc1 = new Location(getWorld(), -13.5, 71, 11.5, -90, 0);
        loc2 = new Location(getWorld(), -12.5, 70, 13.5, -90, 0);
        loc3 = new Location(getWorld(), -12.5, 69, 9.5, -90, 0);

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(200);

        getWorld().setGameRuleValue("doFireTick", "false");
    }

    @Override
    public void join(User user) {
        super.join(user);
    }

    @Override
    public void handleNPCs(User user) {
        Player player = user.getPlayer();

        Bukkit.getScheduler().runTaskLater(getLobby(), () -> {

            ARENA.clone(player).spawn(true);
            LAVA.clone(player).spawn(true);
            FPS.clone(player).spawn(true);
            DAMAGE.clone(player).spawn(true);

            if (ranking.values().size() >= 3) {
                LeaderboardData data;
                SkinData skin;

                new Hologram(player, bestPlayers_location, user.getAccount().getLanguage().translate("lobby.pvp.best_players")).show();

                data = ranking.values().get(0);
                skin = data.getSkinData();
                new NPC(player, loc1, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                Ranking rank = Ranking.fromId(data.getValue(Columns.PVP_RANK).getAsInt());

                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc1.clone().add(0, 2.1, 0), "§a" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.PVP_RANK_EXP).getAsInteger()).show();

                data = ranking.values().get(1);
                skin = data.getSkinData();
                new NPC(player, loc2, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                rank = Ranking.fromId(data.getValue(Columns.PVP_RANK).getAsInt());

                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc2.clone().add(0, 2.1, 0), "§e" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.PVP_RANK_EXP).getAsInteger()).show();

                data = ranking.values().get(2);
                skin = data.getSkinData();
                new NPC(player, loc3, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                rank = Ranking.fromId(data.getValue(Columns.PVP_RANK).getAsInt());

                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc3.clone().add(0, 2.1, 0), "§c" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.PVP_RANK_EXP).getAsInteger()).show();

            }

            LeaderboardHologram maxKillstreakLeaderboard = new LeaderboardHologram(max_killstreak, "§e§lTOP 100 §b§lARENA MAX KILLSTREAK §7(%s/%s)", player, max_killstreak_location);
            maxKillstreakLeaderboard.show();

            LeaderboardHologram maxFpsKillstreakLeaderboard = new LeaderboardHologram(max_killstreak_fps, "§e§lTOP 100 §b§lFPS MAX KILLSTREAK §7(%s/%s)", player, max_killstreak_location_fps);
            maxFpsKillstreakLeaderboard.show();

            InfoHologram arena = new InfoHologram(player, ARENA.getLocation().clone().add(0, 2.1, 0), null, "§bArena", LeaderboardUpdate.SECOND, this::getArenaCount);
            InfoHologram lava = new InfoHologram(player, LAVA.getLocation().clone().add(0, 2.1, 0), null, "§bLava", LeaderboardUpdate.SECOND, this::getLavaCount);
            InfoHologram fps = new InfoHologram(player, FPS.getLocation().clone().add(0, 2.1, 0), null, "§bFPS", LeaderboardUpdate.SECOND, this::getFpsCount);
            InfoHologram damage = new InfoHologram(player, DAMAGE.getLocation().clone().add(0, 2.1, 0), null, "§bDamage", LeaderboardUpdate.SECOND, this::getDamageCount);

            arena.setInteract(interact);
            lava.setInteract(interact);
            fps.setInteract(interact);
            damage.setInteract(interact);

            arena.show();
            lava.show();
            fps.show();
            damage.show();

        }, (user.getAccount().getVersion() >= 47 ? 0 : 5));
    }

    @Override
    public void quit(User user) {
        super.quit(user);
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lPVP");

        DataStorage storage = user.getAccount().getDataStorage();
        int count = Constants.getServerStorage().count();

        scores.add(" ");
        scores.add("§eArena:");
        scores.add(" §fKills: §b" + storage.getData(Columns.PVP_ARENA_KILLS).getAsInteger());
        scores.add(" §fKillstreak: §b" + storage.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§eFPS:");
        scores.add(" §fKills: §b" + storage.getData(Columns.PVP_FPS_KILLS).getAsInteger());
        scores.add(" §fKillstreak: §b" + storage.getData(Columns.PVP_FPS_KILLSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§fCoins: §6" + storage.getData(Columns.PVP_COINS).getAsInteger());
        scores.add("§fPlayers: §a" + (count == -1 ? "..." : count));
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(scores);
    }

    private final NPC ARENA = NPC.builder().location(new Location(Bukkit.getWorld("world"), 2.5, 69.5, 22.5, 175, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTU5NjUzNTU4MDc4MSwKICAicHJvZmlsZUlkIiA6ICJiYWE1Yjg0YzA2NGM0NTBlYjU2NTU4ZDQxOWVmYTkzMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYW1lbGxpYWFkYW1zIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNlZGQyOTkyYjVhNzZmOGU2YzFjMTIyMTUxM2RiNDU2OWFjYzk4MDM0OWNlYjM0OTdjOTRkN2JmMzIxOWM5Y2QiCiAgICB9CiAgfQp9", "A16C1rI3LOIH0CDI3UgBnMU47bSVAwV6BwhOh7/g33jmzKKGNVaN4/vpduPkgqd5OsjWS77STUmCDUzeN16aj7os2dsYct5d/9FewfGQyH+LQfpXMBvy/dH8Jo0DVkoZ3FyMB7FC15Dp9fKco8CMoiQuipMuwC5RIr6ji4MuB/0lewcgGNgQDnJYITAJS0mIZg7wAEFEPoMcdUXflltwPuqpJUwwWoef95jngMWv7T18xycrN6GCLHMy/iqSEKIVVTQDC55H3C5R2Duai5ovaGnQ+8XaYq9Xh0sIVvRgcO2HRAYdwGq7PVSn9Twwq+H3mscoD6oc51bTSNL8c6UjqbnPK5dgndFEZukA21V3CA/dkhTTwo19fapIDGGvQ8xL0nbMFLxi8Nh3nSYZinpPoQDOTQ5nsjSMfv5tTLtU+/xrj9Eu4lfg/kVgvNVejztuwhbZlXV5ApDDSk0gnNLeQc/xJOsQypy2vCkb3fmdv5x9XcMQbD81ox17qTcR/yXf2rHTX/plDszwCq2q/x/jLpNNjl+M2Vec7pW0MLQHvM1/g77bv0hWyVkQvJHSliAXGeDjDBpfxhEoiKZZGcHNVlAB23Z1ZRdhfC0FTzYB56nOLhTjALwFbw8F0rvIkNQp1x8Hqe60idR7YKn9D5JSL0p8BfDlqH/wa7sDKi0Dojo=")).interactExecutor((player, npc, type) -> connectPvP(player, "Arena")).build();

    private final NPC LAVA = NPC.builder().location(new Location(Bukkit.getWorld("world"), -4.5, 69.5, 21.5, -165, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1NjU5NzU0Mjg3NDEsInByb2ZpbGVJZCI6Ijc1MTQ0NDgxOTFlNjQ1NDY4Yzk3MzlhNmUzOTU3YmViIiwicHJvZmlsZU5hbWUiOiJUaGFua3NNb2phbmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk5ZmJhOGQ3YjFiYzI5MDFhZTE5MzM3NjMyOTk0MTgyNDc0OWRjOWQxZDMxMmU4ODI4MDJjYzU1ZmY5MjRkZTEifX19", "P+w3Rogxk27V6zqa02O6DgVQUFoZfr9hGICpVHeHj9xdozTXMHGy1Jtt2UVe+S4f0EpTLcgGZnrxuF5wiq4vp8+UbzazOxfuApb1lzzmJSneya46lYYtE3D/xHbExCmUT2Kd4md/U5urLuCHG+pmmQiTm6vdOV4GRCYDpZjT/gSjoYWiZxPHOHuBj3vpjZ3SohhXWVDkp104Py1Y4dGy8Fijzo0XoD+KvK5LjKVkaur7E8/arbZu2wd2NP65oT9w06uyRr5bwVphUszpxHViHB3g+Q2cbuA9X72IbUJCerEj6LSacHZHDUscdiijzAv6BenrWL5xFIR5hZ5dUAQV2rKJAk/Kqlxio9AebxXmXE3v3FhQ3wzc4Z79xzmYTYSEdOW0b0Y0O2FaWpzCeJQDSLdwGfh4pDUK5TE6n2lf8mfzooOwHZisYUZktjL3ULU1jw8tcmeloDMnw70u3E/l8nL+lRPUdBrEA0nldQW9uLQbi8KWpGZFUlxGmJAIor3mu0h0MSrVaS0SBRSQIzrURb1pI54s1H5yWdjJY4Mjqh3UQW5SNMKoq9eFHUOxwfZ8MYlCpVdA5Ezs/Be4McaPAJWVa1jAeIkRun6zQRV91alWiQ0QFwk26VLgvbwvKC7BVbALXurQ0EoOqYcEmxdNrpUM3nZtnKbmBn6L8/zXcyg=")).interactExecutor((player, npc, type) -> connectPvP(player, "Lava")).build();

    private final NPC FPS = NPC.builder().location(new Location(Bukkit.getWorld("world"), 5.5, 69.5, 21.5, 165, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYwMTUwMzk2MjI1MywKICAicHJvZmlsZUlkIiA6ICI1M2U3YjJmMTI2OGY0ZGFjOTRhMDBhOWE0ODllZGUxNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXlkZW1zIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhMTMxN2M0ZjRlODMzN2QxY2RlMjNiNTJjYzVmM2VlMzRjYTUyYTcxODQ4MjE5NTBjMDAwZmY1OWI4MThjZWYiCiAgICB9CiAgfQp9", "NSAUESj63UlFMhhsuFMI58y7uKkbcTmlSny3/Y28Dj7+HDXv99dVG3//SNPunBteCf1LKCCMs9Uq4B2DQeFaupR2nmXl76H548p7Qwkyqm8PpFcJQgBVuiLFPHObKHEAJT8BTdrJxTXg5LGuUGert41fOtmJNoAoQ5B+lUtmvl6Y9Ccwt4+6rF89ELLnyIOtOTpEvfWVIuFsXEyaHLCrlozIuRzN+ZDb0QyHCywzgF2n4YkCmhSyH69Pml/m+iI9vww9x0OrEo6D3yz+5mPkvpDZfqZDlmh1g+565yT3rk411ICNizoCdnBNw5BWw7eBYXqFxkj/BBVjfvcnTfMD+e6XyX8v1iBVbgfz47NVF+tDP5XC9tCA1UU2E7Vr2tuLGdmD+02gkUtRdpbMH1KLVNOPyqpcfZZO3j3c5tx4JaX9X05hwJKQHi9shch9I6uI5QiBybwMeqtoeAoyjN3yu/X3K9heyWQdT2+/NxTsoxoTNjRTx9pHaDlkzn1RYG0DRGJJ5zHjm7PXchLNvLNEWMHrtOwddETpnDYuCyKPaNoThUsafsFDO3TPKsYK+kfzmVrqX9bNzigDFJIgBvsbyJX5ZA7Mg4Idb1a+0d7l0G9sHztWkBhujIOQbMbsunW3SupC6x6XotWFEyFwOO+736a4K5gvzTcfL2a7LBW/gFU=")).interactExecutor((player, npc, type) -> connectPvP(player, "Fps")).build();

    private final NPC DAMAGE = NPC.builder().location(new Location(Bukkit.getWorld("world"), -1.5, 69.5, 22.5, -175, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxNTY0ODQyNjQyNiwKICAicHJvZmlsZUlkIiA6ICJhYTZhNDA5NjU4YTk0MDIwYmU3OGQwN2JkMzVlNTg5MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiejE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzYjQ1MzkwMThkYWYxZjI5MGMxZjQyYTcxMjU2MTJhYjc1YjBmNjIzNDQ2MTI3YzY0MDAzM2U4NjY1NzhhOTQiCiAgICB9CiAgfQp9", "wiuAbBgRGkpwknuaPoGRQS2/K39uktJQWeRN2YvgBOE593rxeE7DZYMxLRXGATAQ0eLBuAPmUQDjuQWpXYzCPZOthQsXJvXxH5Oea/mlwESdMAHxiB53gezYPqxK1f3SKi48Bah5vQoF4c8SRb1b4LUAVjDO2bm3iFmBhgNFbihVrYW+1+UY5bs9/G36M3zDRRFOEpoE/wCHHRM+FzIRBBHhr5xmgX16c01Q80cQEH7TKalyVi6Y09qmii+817MmoZBIrnkrNrXCPHf0NSlI1rOtSxYOHeWMNl6XZ+Rdi+zJryZ865WyhM3ue4G1YDNG3lJDPT/YPEr7TlCQY+mhsz+EI4DwTmbhh/NM/Emwao6Fw8cCTV71lvhPlf+kX2ttJbxHIS3RBuvVzuFnKAQGt5J0i0pCI9HJkTeiVM5nTKV8lZNfZ0pWFreSzK3MMsx3m5osklmkKZzmf4V/FhIVyrxGfMjcOY35uzN0ZSJtyxHLMWbZCwjv3lth7ux31j9fW/HR4LUC0ItpMurwgVgHgRAKKgKN6IfftQWNzLdZi4ND9bC0yOfgmZLpKSBBslY6gJ/4NN3p7EvRvpeZhBtdWE58s8VvETaj2psBGZuVB2ALHWp3ntHXmljW+2Tu5aSdHLmBgwvGOCRBpKYc/mGJjtqMEocD4POF840Z5cJQT8s=")).interactExecutor((player, npc, type) -> connectPvP(player, "Damage")).build();

    protected void connectPvP(Player player, String pvp_server) {
        if (isConnectionCooldown(player.getUniqueId())) {
            Account account = Account.fetch(player.getUniqueId());
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());

        Account account = Account.fetch(player.getUniqueId());

        if (server == null || server.isDead()) {
            player.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("no_server_available", "pvp")));
            return;
        }

        player.sendMessage(account.getLanguage().translate("arcade.room.searching"));

        ServerRedirect redirect = new ServerRedirect(player.getUniqueId(), new ServerRedirect.Route(server, pvp_server));
        account.connect(redirect);
    }

    public int arena_count, fps_count, lava_count, damage_count;

    public int getArenaCount() {
        return (server.isDead() ? -1 : arena_count);
    }

    public int getFpsCount() {
        return (server.isDead() ? -1 : fps_count);
    }

    public int getLavaCount() {
        return (server.isDead() ? -1 : lava_count);
    }

    public int getDamageCount() {
        return (server.isDead() ? -1 : damage_count);
    }

    @EventHandler
    public void onServerPayloadReceiveEvent(ServerPayloadReceiveEvent event) {
        if (event.getServer().getServerCategory() != ServerCategory.PVP)
            return;
        arena_count = ((Double) event.getPayload().get("Arena")).intValue();
        fps_count = ((Double) event.getPayload().get("Fps")).intValue();
        lava_count = ((Double) event.getPayload().get("Lava")).intValue();
        damage_count = ((Double) event.getPayload().get("Damage")).intValue();
    }

}