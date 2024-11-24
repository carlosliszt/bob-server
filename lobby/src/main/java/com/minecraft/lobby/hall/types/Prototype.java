package com.minecraft.lobby.hall.types;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.server.hologram.InfoHologram;
import com.minecraft.core.bukkit.server.route.GameRouteContext;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Prototype extends Hall {
    private final NPC TIOGERSON = NPC.builder().equipment(NPC.Equipment.builder().hand(new ItemStack(Material.STICK)).build()).location(new Location(Bukkit.getWorld("world"), 0.5, 64.375, 41.5, 180, 0)).property(new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTczMjIzMzA2MjAyNywKICAicHJvZmlsZUlkIiA6ICJlM2Y1MDJmNDQ5OTU0NDgzYmE1NGRlYWMyMzRkMTg1NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzb3JpZW5jYWl4YWRhZ3VhIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFmZjA4MWRkZjM0YWRkY2I4MmY5MTAzOTE4ODNjODg0ZWI5OTNlZDI0Njk0MDNhYzA4NjUxMzUyYjk2MjJjMWYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hZmQ1NTNiMzkzNThhMjRlZGZlM2I4YTlhOTM5ZmE1ZmE0ZmFhNGQ5YTljM2Q2YWY4ZWFmYjM3N2ZhMDVjMmJiIgogICAgfQogIH0KfQ==")).interactExecutor((player, npc, type) -> {
        if (isConnectionCooldown(player.getUniqueId())) {
            Account account = Account.fetch(player.getUniqueId());
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());

        Account account = Account.fetch(player.getUniqueId());
        Server server = ServerCategory.TIOGERSON.getServerFinder().getBestServer(ServerType.TIOGERSON);

        if (server == null) {
            player.sendMessage(account.getLanguage().translate("no_server_available", "tiogerson"));
            return;
        }

        GameRouteContext context = new GameRouteContext();

        context.setGame(DuelType.BOXING_1V1);
        context.setPlayMode(Vanish.getInstance().isVanished(account) ? PlayMode.VANISH : PlayMode.PLAYER);

        ServerRedirect.Route route = new ServerRedirect.Route(server, Constants.GSON.toJson(context));
        ServerRedirect serverRedirect = new ServerRedirect(account.getUniqueId(), route);

        account.connect(serverRedirect);

    }).build();

    //
    public Prototype(Lobby lobby) {
        super(lobby, "Prototype Lobby", "prototypelobby", "EM DESENVOLVIMENTO!");

        setSpawn(new Location(getWorld(), 0, 65, 0));
        getLobby().getAccountLoader().addColumns(Columns.TIOGERSON_WINSTREAK, Columns.TIOGERSON_WINS);

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
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void handleNPCs(User user) {
        Player player = user.getPlayer();
        Bukkit.getScheduler().runTaskLater(getLobby(), () -> {
            TIOGERSON.clone(player).spawn(true);

            InfoHologram tioGerson = new InfoHologram(player, TIOGERSON.getLocation().clone().add(0, 2.1, 0), null, "§bTio Gerson", LeaderboardUpdate.SECOND, () -> Constants.getServerStorage().count(ServerType.TIOGERSON));
            tioGerson.setInteract(interact);
            tioGerson.show();

        }, (user.getAccount().getVersion() >= 47 ? 0 : 5));
    }
}
