/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby.hall.types;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.bukkit.server.hologram.InfoHologram;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardData;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HungerGames extends Hall {

    private final Leaderboard wins = new Leaderboard(Columns.HG_WINS, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard killsRecord = new Leaderboard(Columns.HG_MAX_GAME_KILLS, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard ranking = new Leaderboard(Columns.HG_RANK_EXP, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 3, Columns.USERNAME, Columns.HG_RANK, Columns.SKIN).query();

    private final Location wins_location;
    private final Location killsRecord_location;
    private final Location bestPlayers_location;
    private final Location loc1, loc2, loc3;

    public HungerGames(Lobby lobby) {
        super(lobby, "HG Lobby", "hglobby", "HG NO BOBMC.COM.BR");

        setSpawn(new Location(getWorld(), 0.5, 62.2, -21.5));
        getLobby().getAccountLoader().addColumns(Columns.HG_WINS, Columns.HG_KILLS, Columns.SCRIM_WINS, Columns.SCRIM_KILLS, Columns.HG_COINS);

        Constants.setServerType(ServerType.HG_LOBBY);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        wins_location = new Location(this.getWorld(), 17.5, 63.5, 6.5);
        killsRecord_location = new Location(this.getWorld(), 17.5, 63.5, -5.5);
        bestPlayers_location = new Location(getWorld(), 20.5, 66, 0.5);

        loc1 = new Location(getWorld(), 21.5, 63, 0.5, 90, 0);
        loc2 = new Location(getWorld(), 20.5, 62, -1.5, 90, 0);
        loc3 = new Location(getWorld(), 20.5, 61, 2.5, 90, 0);

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(200);
    }

    @Override
    public void join(User user) {
        super.join(user);
    }

    @Variable(name = "scrim.schedule")
    public String schedule = "15h 17h 19h 21h";

    @Override
    public void handleNPCs(User user) {

        Player player = user.getPlayer();

        Bukkit.getScheduler().runTaskLater(getLobby(), () -> {

            HGMIX.clone(player).spawn(true);
            EVENT.clone(player).spawn(true);
            SCRIM.clone(player).spawn(true);
            CLANXCLAN.clone(player).spawn(true);

            if (ranking.values().size() >= 3) {
                LeaderboardData data;
                SkinData skin;

                new Hologram(player, bestPlayers_location, user.getAccount().getLanguage().translate("lobby.hg.best_players")).show();

                data = ranking.values().get(0);
                skin = data.getSkinData();

                new NPC(player, loc1, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                Ranking rank = Ranking.fromId(data.getValue(Columns.HG_RANK).getAsInt());
                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc1.clone().add(0, 2.1, 0), "§a" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.HG_RANK_EXP).getAsInteger()).show();

                data = ranking.values().get(1);
                skin = data.getSkinData();
                new NPC(player, loc2, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);

                rank = Ranking.fromId(data.getValue(Columns.HG_RANK).getAsInt());
                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc2.clone().add(0, 2.1, 0), "§e" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.HG_RANK_EXP).getAsInteger()).show();

                data = ranking.values().get(2);
                skin = data.getSkinData();
                new NPC(player, loc3, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);

                rank = Ranking.fromId(data.getValue(Columns.HG_RANK).getAsInt());
                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc3.clone().add(0, 2.1, 0), "§c" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.HG_RANK_EXP).getAsInteger()).show();
            }

            LeaderboardHologram killsLeaderboard = new LeaderboardHologram(wins, "§e§lTOP 100 §b§lHG WINS §7(%s/%s)", player, wins_location);
            killsLeaderboard.show();

            LeaderboardHologram maxKillsLeaderboard = new LeaderboardHologram(killsRecord, "§e§lTOP 100 §b§lHG KILLS RECORD §7(%s/%s)", player, killsRecord_location);
            maxKillsLeaderboard.show();

            InfoHologram hgMix = new InfoHologram(player, HGMIX.getLocation().clone().add(0, 2.1, 0), null, "§BHG MIX", LeaderboardUpdate.SECOND, () -> Constants.getServerStorage().count(ServerType.HGMIX));
            hgMix.setInteract(interact);
            hgMix.show();

            new Hologram(player, CLANXCLAN.getLocation().clone().add(0, 1.9, 0), "§d§lEM BREVE").show();

            InfoHologram events = new InfoHologram(player, EVENT.getLocation().clone().add(0, 2.1, 0), null, "§bEventos", LeaderboardUpdate.SECOND, () -> Constants.getServerStorage().count(ServerType.EVENT));
            events.setInteract(interact);
            events.show();

            InfoHologram scrim = new InfoHologram(player, SCRIM.getLocation().clone().add(0, 2.3, 0), "§e" + schedule, "§bScrim", LeaderboardUpdate.SECOND, () -> Constants.getServerStorage().count(ServerType.SCRIM));
            scrim.setInteract(interact);
            scrim.show();

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

        gameScoreboard.updateTitle("§b§lHARDCORE GAMES");

        DataStorage storage = user.getAccount().getDataStorage();
        int count = Constants.getServerStorage().count();

        scores.add(" ");
        scores.add("§eHG Mix:");
        scores.add(" §fWins: §b" + storage.getData(Columns.HG_WINS).getAsInteger());
        scores.add(" §fKills: §b" + storage.getData(Columns.HG_KILLS).getAsInteger());
        scores.add(" ");
        scores.add("§eScrim:");
        scores.add(" §fWins: §b" + storage.getData(Columns.SCRIM_WINS).getAsInteger());
        scores.add(" §fKills: §b" + storage.getData(Columns.SCRIM_KILLS).getAsInteger());
        scores.add(" ");
        scores.add("§fCoins: §6" + storage.getData(Columns.HG_COINS).getAsInteger());
        scores.add("§fPlayers: §a" + (count == -1 ? "..." : count));
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void run() {
        super.run();

        if (isPeriodic(20))
            sync(this::update);
    }

    private Selector brazilianSelector = Selector.builder().build();
    private Selector englishSelector = Selector.builder().build();

    public Selector updateSelector(Language language, Selector other) {
        List<Server> startedServers = Constants.getServerStorage().getServers().stream()
                .filter(c -> c.getServerType() == ServerType.HGMIX && !c.isDead() &&
                        !c.getBreath().get("stage").toString().equals("WAITING"))
                .collect(Collectors.toList());

        List<ItemStack> stacks = new ArrayList<>();

        Server suggestedServer = ServerCategory.HG.getServerFinder().getBestServer(ServerType.HGMIX);

        if (suggestedServer != null) {

            int time = Double.valueOf((double) suggestedServer.getBreath().get("time")).intValue();

            ItemFactory itemFactory = new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(5).setName("§aHG MIX #" + suggestedServer.getName().replace("hgmix", ""));
            int online = suggestedServer.getBreath().getOnlinePlayers();
            itemFactory.setDescription(100, language.translate(online < 5 ? "lobby.hg.waiting_for_players.regexp" : Stage.WAITING.getTranslation(), formatTime(time, language), online));
            stacks.add(itemFactory.getStack());
        } else if (!startedServers.isEmpty()) {
            stacks.add(new ItemFactory(Material.STAINED_GLASS_PANE).setName("§cOps!").setDescription(35, "§7Não há nenhuma sala disponível.\n\n§cCompre vip em §e" + Constants.SERVER_STORE + " §cpara entrar §caté os 5 minutos em partidas já iniciadas.").setDurability(14).getStack());
        }

        for (Server server : startedServers) {

            int time = Double.valueOf((double) server.getBreath().get("time")).intValue();

            ItemFactory itemFactory = new ItemFactory(Material.STAINED_GLASS_PANE).setDurability(15).setName("§cHG MIX #" + server.getName().replace("hgmix", ""));
            itemFactory.setDescription(100, language.translate(Stage.getStage(server.getBreath().get("stage").toString()).getTranslation(), formatTime(time, language), server.getBreath().getOnlinePlayers()));
            stacks.add(itemFactory.getStack());
        }

        Selector selector = Selector.builder().build();

        selector.setName(language.translate("lobby.hg.container.selector"));

        if (stacks.size() == 0) {
            selector.setItems(Collections.singletonList(new ItemFactory(Material.STAINED_GLASS_PANE).setName("§cOps!").setDescription(35, "§7Não há nenhuma sala disponível :(").setDurability(14).getStack()));
            selector.setSize(27);
            selector.setPreviousPageSlot(0);
            selector.setNextPageSlot(8);
            selector.setAllowedSlots(Collections.singletonList(13));
            selector.build();
            other.getPlayers().forEach(selector::open);
            return selector;
        }

        int hotbars = getSize(stacks.size());

        selector.setItems(stacks);
        selector.setSize((hotbars * 9) + 18);
        selector.setPreviousPageSlot(0);
        selector.setNextPageSlot(8);
        selector.setAllowedSlots(Arrays.asList(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 34));
        selector.build();
        other.getPlayers().forEach(selector::open);
        return selector;
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {

        if (event.getClickedInventory() != null) {

            if (event.getClickedInventory().getName().contains("HG MIX") && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {

                if (event.getCurrentItem().getDurability() == 14)
                    return;

                HumanEntity entityHuman = event.getWhoClicked();

                entityHuman.closeInventory();

                Account account = Account.fetch(entityHuman.getUniqueId());

                if (isConnectionCooldown(entityHuman.getUniqueId())) {
                    Cooldown cooldown = this.getCooldown(entityHuman.getUniqueId());

                    entityHuman.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
                    return;
                }

                Server server = Constants.getServerStorage().getServer("hgmix" + event.getCurrentItem().getItemMeta().getDisplayName().split("#")[1]);

                addCooldown(entityHuman.getUniqueId());

                account.connect(server);
            }
        }
    }

    public void update() {
        this.brazilianSelector = updateSelector(Language.PORTUGUESE, brazilianSelector);
        this.englishSelector = updateSelector(Language.ENGLISH, englishSelector);
    }

    public static String formatTime(int i, Language language) {
        if (language == Language.PORTUGUESE) {
            if (i >= 60) {
                int minutes = i / 60;
                int seconds = i - minutes * 60;
                if (seconds == 0) {
                    if (minutes > 1) {
                        return minutes + " minutos";
                    } else {
                        return minutes + " minuto";
                    }
                }
                String min = "minuto";
                if (minutes > 1)
                    min = min + "s";
                return minutes + " " + min;
            }
            if (i > 1)
                return i + " segundos";
            return i + " segundo";
        } else {
            if (i >= 60) {
                int minutes = i / 60;
                int seconds = i - minutes * 60;
                if (seconds == 0) {
                    if (minutes > 1) {
                        return minutes + " minutes";
                    } else {
                        return minutes + " minute";
                    }
                }
                String min = "minute";
                if (minutes > 1)
                    min = min + "s";
                return minutes + " " + min;
            }
            if (i > 1)
                return i + " seconds";
            return i + " second";
        }
    }

    private final NPC HGMIX = NPC.builder().location(new Location(getSpawn().getWorld(), 2.5, 61.5, 11.5, 180, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYzNjE5NjA3ODk1NiwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJmMjNiMGQ1NjkyNWEwYTI5YmU0ZDUzMDExODAzODFjMGIxODdmZDRmNTJkOTAyOTA4ODY1MDQxNDdmY2VmZCIKICAgIH0KICB9Cn0=", "Q+M5kRV6Vsl0tgFl08yeulUhJdVN1j8NpaH88w7sQ3qShBvgzwixn0HZN6+Uh9fTLXkmypE8LLXmXQDioXtDBlFfDlIxo1VjfszLsDAP6l2UHrCA67Qeg0N0zVn2NlqapoTIKuL4loa/VnY1BStTIdoKZBLpKMYwY0XBlFwnGIjGVlyLAGNINfrUpH53gf0ugBZi4MtQJzxQkGqQuTOzt30mPWMgR5lhqLj5J5emgiXXFxZQOXOXpkC2S3Q9zk9uPKM31+ekMnvNILlreEA1hV5rU1jlnT3ujVT+5EZqXjmd32QBrWNgm2i7MHc0P5Rd30urH0na7hoB8LzfrbNXj7rHnmxTROC6Ktpnz6S08RE9Z4RvdqA2Z4mlxFvT5pirXCWjEAY0goHXR2HBewTsep6WJNNNyCERH46cNOfQ/oGrFuYujZoyEGr71YacNcbOE84QEz2aIe+a1b937+JHg0Opd65ef/cVLEidgC4bmYyqUk673vEf6Xf6z59WwmFWMgpJedUx6JaWmdCtXKkCT/mxleMlJ72OoZ30xF5Avq0WfWBlaYW3ZNsDugHX5i+JzuNfh0VAyF4ReQDsWMeT3pTeaoZB46eVwDC7REeRkeNH00R4Xr81dda6LD9YTFrUlDHts3tGXPFWWZflEBlSmqcCvXfgBT0vme31DJeYld0=")).interactExecutor((player, npc, type) -> {

        User user = getLobby().getUserStorage().getUser(player.getUniqueId());
        Selector selector = user.getAccount().getLanguage() == Language.PORTUGUESE ? brazilianSelector : englishSelector;
        selector.open(user.getPlayer());

    }).build();

    private final NPC SCRIM = NPC.builder().location(new Location(getSpawn().getWorld(), -4.5, 61.5, 10.5)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTU4ODIzOTc2NTQ3MiwKICAicHJvZmlsZUlkIiA6ICI1MDAwZGJkNmVlMjY0ZDY4ODViM2M2YWFjOGEzNmE1YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaWN0aW1zX0ZyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFmMjFkMzllZTZiMzI4ODZhZDU4ZDU3MGU1MjdhNDAzMzYwZTI2OGE0NzVkN2E4NzE2MjdiNDVhZDE5ZmQ2YTYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "x/pV98Kid4/nt4VxwVvsqwSOesyeUAgRCyX8D34m/k6ANAriwtzkxc+i/RV/NC7IuMnJPyHD35NOLtYOh0Z+cRazN7qmQMuz/J/b/cwhiq93LcnHPwLCAg1rJJmS0NlRJzFZArip0XBozYEvQWnPKw+0VC5DAF79cD0maOvLTC165uT0UA88rizSasITVT3xHjZ7njVrYllj/x4M5hhfLMS4e/MSKgU+uceixnv3nQD9iCnNfVZS8921/zOi7HG65nm9BlbDTPYElclwD/PDXTD7Eylja6pHJkMrvSdCttXP6ejWbGmDE075Svsjw4xRejywfkptblJbh12ZavbuID1az653ExalNREB2YBzXm1VmUTkB2c30q+oG1vtw2wYoIRJwwOGBk5UEyjSGfdYsjfKemJm9sWIw6OGltFR6h4jjxqaHBi44Nzxn6SSRKrD3KO5H2MDQlcwP+s0n0FB0x2zL8pIoOdwQXTuwn+PntsFccL1pOIc5lyIQaZFZ+HRqANwRqlk8bTAKpyB0PiJyTvg5A6yCjM9AZbQxBskpfGOUCx6wijoQKtZ8AG3PGsYVwnREOKXpifda97BvyedB57mJHGpcWpS8T7BRWhnyPI0aGXCxY6T856njU+AKl1TfbB+IMHCn/gbDPp/Y9qqr/GSK4iosWHXNUo8s5aY5EA=")).interactExecutor((player, npc, type) -> {

        Account account = Account.fetch(player.getUniqueId());

        if (isConnectionCooldown(player.getUniqueId())) {
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());

        Server server = ServerCategory.HG.getServerFinder().getBestServer(ServerType.SCRIM);

        if (server == null) {
            player.sendMessage(account.getLanguage().translate("no_server_available", "scrim"));
            return;
        }

        account.connect(server);
    }).build();

    private final NPC EVENT = NPC.builder().location(new Location(getSpawn().getWorld(), 5.5, 61.5, 10.5, 175, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyNjc0NDc0NDA4OCwKICAicHJvZmlsZUlkIiA6ICI5MzI0N2IzMzllMTQ0MDBkYjk5Y2ViM2Y0NzA4ZTBhNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJBemFyb3dfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UxMDk2MTc4ZDlkY2JiNDZkOGUyYTJiNWMzMzUyYTEzYjY5NTE3Y2ZiM2ViZmFhODBlOTM0YjJmMmI2MDc0MTkiCiAgICB9CiAgfQp9", "k7Gt5/W/tiu7C8gcfK6AE0CA3BYVMUAP7Hs268YjIm8r6smuT5/W2Hlu7aoi/nfcUcS5BhYBzOdqkmwOsYod56RkhgiKhZaBJ5KykHZI2peY4yz7vX/Ck8FyDwhblZ5bUg7MtTP7Cp2tgp2i2BG2QMtWc6wKkP59ynXd9OFc4RwldOagLzEqWQFPe/0jJ0+RICr/DGDK0mW16N6uB6+rCxwD2mi95/xpt2unZtjsrgFoc24tfR32IFI/XpthGM6kQXRqJC4QoLR/Vqk9DCRsySxgwKyRKe8rMXuQFmMH5anaHHPh365jhUs9t/GrZIc/z9ABRO490wpd6eqgEvGEOdDxYe+3FQO3vLqMjMlXR/msJXZMlhzETVr+Kei1ZoE6/RTOOaCjRUSYnm5P1YaLOmmSf16+e746JGUtnTuUDQRH9bOrC6JAvZjz32vglEP+JweYE1QrUA1joxYlm6bV2e8OHIYsySlT0uI0LEx25yB0CtJqlKPwgmdG2rOW/gtSEg3LrNubHwi4zyAEJ65Qbpcog6e+2riaJXjFdQh9XsEu70esq42+7TQ2UDFp4Xy9CACRCfAKM8fk/HUuM4JY6vyf3Ce3eI/MeNcEMbObtXGoXD/7CfwNFHUNJkwfzYcjENDXB7eYujFqY0coAgA6Zym3anxD2nCLHm4y48dDm5A=")).interactExecutor((player, npc, type) -> {

        Account account = Account.fetch(player.getUniqueId());
        if (isConnectionCooldown(player.getUniqueId())) {
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());

        Server server = ServerCategory.HG.getServerFinder().getBestServer(ServerType.EVENT);

        if (server == null) {
            player.sendMessage(account.getLanguage().translate("no_server_available", "event"));
            return;
        }

        account.connect(server);
    }).build();

    private final NPC CLANXCLAN = NPC.builder().location(new Location(getSpawn().getWorld(), -1.5, 61.5, 11.5, 175, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyODc4NTAxMzE0OCwKICAicHJvZmlsZUlkIiA6ICI2MTZiODhkNDMwNzM0ZTM3OWM3NDc1ODdlZTJkNzlmZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJfX25vdGFodW1hbl9fIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdmNzU5NjYzNTA0M2M4YTYxZTc4Yjg2NjY4MDRkYzdjZTYyY2NmOGQ4MDdkNDk5Y2IwZmNiMDk3ZjljZWRlYWUiCiAgICB9CiAgfQp9", "BbhkTrlFKGETIyNV8/FH5pWpJdy3Zz7KdgKImSiegZNGJtVLJlSj9qwlwtsNxsLWA6Ns+tTjUm8jflS23ReiPcs4yFexlKDwB3Dj+5M8ZEeBiLRkvO1NZyxUijmCi35m4/3kMHFdoSGH6aYfqnwAPNh1Ui0GuO/pDYYrrNiSabfpGWp+s6D5kqhs8A84Ft13g8SpaOH7DLq9NDqDiQ4i2PysqoGV8HCrFSytvB7tM+RRNXVRv+rfHV9y2u93VluOFGmwKUOar91v0VYxy1NlSYtMBsBRNhYJcwIh/KHpuaiwfN9KOxUs4uOSEbWHd9b/mxEbGfeEBRVcBeUN8yuSPXJRgl1QZIkfHyi32WSCSoZBTF4bqM4i4ysmlIblry1XSDCE5Aw3IYw8NRzRl0X5MqtKCqMJxte0Ax+pgRY/u/KwJHW6UmaMKTMoh8w2EpyDvnNxNRp5MZfIKO2xTYMphEKb8QTODNJPudUGkn+oPymkysIaC5n2eiIBCq3Y7MshXJoHAJk/MgS5dBqnypP+N9mXWmojh1W2+TqyruHkx6FKAn0YtxhbDKrazVMEZn1b7y2oO5x4eb2tuFAexan3ijPfuUMk2VHSF0xvCuIBT/tUwNhHa1V+bt519k8hKxs5ta3T4QU2nS/Oy08k3PKzauotJIgY4Osm0hw2bPIL+CE=")).interactExecutor((player, npc, type) -> {

        Account account = Account.fetch(player.getUniqueId());

        if (!account.hasPermission(Rank.PARTNER_PLUS)) {
            player.sendMessage("§cEm breve!");
            return;
        }

        if (isConnectionCooldown(player.getUniqueId())) {
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());

        Server server = ServerCategory.HG.getServerFinder().getBestServer(ServerType.MM_CLANXCLAN);

        if (server == null) {
            player.sendMessage(account.getLanguage().translate("no_server_available", "clanxclan"));
            return;
        }

        account.connect(server);
    }).build();

    @Getter
    @AllArgsConstructor
    private enum Stage {

        WAITING("lobby.hg.starting_game"),
        INVINCIBILITY("lobby.hg.invincibility_wears_off"),
        PLAYING("lobby.hg.started_game");

        private final String translation;

        public static Stage getStage(String n) {
            return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(n)).findAny().orElse(PLAYING);
        }

    }
}