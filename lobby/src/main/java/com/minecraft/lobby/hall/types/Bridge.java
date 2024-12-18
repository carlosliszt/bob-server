package com.minecraft.lobby.hall.types;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.bukkit.event.server.ServerPayloadReceiveEvent;
import com.minecraft.core.bukkit.server.hologram.InfoHologram;
import com.minecraft.core.bukkit.server.route.BridgeRouteContext;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.server.thebridge.BridgeCageConfig;
import com.minecraft.core.bukkit.server.thebridge.BridgeCageList;
import com.minecraft.core.bukkit.server.thebridge.GameType;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.leaderboard.Leaderboard;
import com.minecraft.core.bukkit.util.leaderboard.LeaderboardHandler;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardData;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardType;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.duel.inventory.DuelsSettingsInventory;
import com.minecraft.lobby.duel.inventory.enums.FactorySettings;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Bridge extends Hall {

    private final Server server;

    private final Leaderboard winsLeaderboard = new Leaderboard(Columns.BRIDGE_SOLO_WINS, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard killsLeaderboard = new Leaderboard(Columns.BRIDGE_SOLO_POINTS, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard winstreakLeaderboard = new Leaderboard(Columns.BRIDGE_SOLO_WINSTREAK, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();

    private final Leaderboard winsDuoLeaderboard = new Leaderboard(Columns.BRIDGE_DOUBLES_WINS, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard killsDuoLeaderboard = new Leaderboard(Columns.BRIDGE_DOUBLES_POINTS, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();
    private final Leaderboard winstreakDuoLeaderboard = new Leaderboard(Columns.BRIDGE_DOUBLES_WINSTREAK, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 100, Columns.USERNAME, Columns.RANKS).query();


    private final Location winsLocation, winstreakLocation, killsLocation, killsDuoLocation, winstreakDuoLocation, winsDuoLocation;

    private final Leaderboard ranking = new Leaderboard(Columns.BRIDGE_RANK_EXP, LeaderboardUpdate.HALF_HOUR, LeaderboardType.PLAYER, 3, Columns.USERNAME, Columns.BRIDGE_RANK, Columns.SKIN).registerHandler(new LeaderboardHandler() {
        @Override
        public void onUpdate() {

        }
    }).query();

    InteractableItem shop = new InteractableItem(new ItemFactory().setType(Material.EMERALD).setName("§aShop do Bridge §7(Clique Direito)")
            .getStack(), new InteractableItem.Interact() {
        @Override
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            openCages(player);
            return true;
        }
    });

    private final Location bestPlayers, loc1, loc2, loc3, stats;

    private final Set<BridgeCageConfig> cagesConfig = new HashSet<>();

    public Bridge(Lobby lobby) {
        super(lobby, "The Bridge Lobby", "bridgelobby", "THE BRIDGE NO BOBMC.COM.BR");

        setSpawn(new Location(Bukkit.getWorld("world"), 0.5, 65.0, 0.5, 0, 0));
        getLobby().getAccountLoader().addColumns(Columns.BRIDGE_CAGE, Columns.BRIDGE_SOLO_WINS, Columns.BRIDGE_SOLO_WINSTREAK, Columns.BRIDGE_DOUBLES_WINS, Columns.BRIDGE_DOUBLES_WINSTREAK, Columns.BRIDGE_SOLO_KILLS, Columns.BRIDGE_SOLO_DEATHS, Columns.BRIDGE_DOUBLES_KILLS, Columns.BRIDGE_DOUBLES_DEATHS, Columns.BRIDGE_COINS);

        Constants.setServerType(ServerType.THE_BRIDGE_LOBBY);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        server = Constants.getServerStorage().getServer("bridge1a");

        this.winsLocation = new Location(Bukkit.getWorld("world"), 11.5, 67, 12.5);
        this.killsLocation = new Location(Bukkit.getWorld("world"), 16.5, 67, 16.5);
        this.winstreakLocation = new Location(Bukkit.getWorld("world"), 13.5, 67, 14.5);

        this.winsDuoLocation = new Location(Bukkit.getWorld("world"), -10.5, 67, 11.5);
        this.killsDuoLocation = new Location(Bukkit.getWorld("world"), -15.5, 67, 15.5);
        this.winstreakDuoLocation = new Location(Bukkit.getWorld("world"), -12.5, 67, 13.5);

        this.bestPlayers = new Location(Bukkit.getWorld("world"), -7.5, 68, 33.5);

        this.loc1 = new Location(Bukkit.getWorld("world"), -7.5, 64.5, 33.5, -130, 0);
        this.loc2 = new Location(Bukkit.getWorld("world"), -7.5, 64.0, 31.5, -117, 0);
        this.loc3 = new Location(Bukkit.getWorld("world"), -5.5, 63.5, 34.5, -145, 0);

        this.stats = new Location(Bukkit.getWorld("world"), -2.5, 65.0, 4.5, 217, 0);

        this.cagesConfig.addAll(jedisCages());
    }

    @Override
    public void join(User user) {
        super.join(user);

    }

    protected Set<BridgeCageConfig> jedisCages() {
        Set<BridgeCageConfig> bridgeCageConfigSet = new HashSet<>();

        try (Jedis jedis = Constants.getRedis().getResource(Redis.SERVER_CACHE)) {
            if (!jedis.exists("bridge.cages"))
                return bridgeCageConfigSet;
            bridgeCageConfigSet.addAll(Constants.GSON.fromJson(jedis.get("bridge.cages"), BridgeCageList.class).getCageList());
        }

        return bridgeCageConfigSet;
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lTHE BRIDGE");

        DataStorage storage = user.getAccount().getDataStorage();
        int count = Constants.getServerStorage().count();

        scores.add(" ");
        scores.add("§eSolo:");
        scores.add(" §fWins: §b" + storage.getData(Columns.BRIDGE_SOLO_WINS).getAsInteger());
        scores.add(" §fWinstreak: §b" + storage.getData(Columns.BRIDGE_SOLO_WINSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§eDuplas:");
        scores.add(" §fWins: §b" + storage.getData(Columns.BRIDGE_DOUBLES_WINS).getAsInteger());
        scores.add(" §fWinstreak: §b" + storage.getData(Columns.BRIDGE_DOUBLES_WINSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§fCoins: §6" + storage.getData(Columns.BRIDGE_COINS).getAsInteger());
        scores.add("§fPlayers: §a" + (count == -1 ? "..." : count));
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void handleNPCs(User user) {
        Player player = user.getPlayer();

        player.getInventory().setItem(3, shop.getItemStack());


        boolean show = user.getAccount().getRank().getId() >= Rank.ADMINISTRATOR.getId();

        Bukkit.getScheduler().runTaskLater(getLobby(), () -> {

            if (show) {

                SHOP.clone(player).spawn(true);

                Hologram shop = new Hologram(player, SHOP.getLocation().clone().add(0, 2.1, 0), "§bComerciante", "§eCabines & Cosméticos");

                shop.setInteract(interact);
                shop.show();
            }

            SOLO.clone(player).spawn(true);
            DOUBLE.clone(player).spawn(true);

            InfoHologram infoHologram = new InfoHologram(player, SOLO.getLocation().clone().add(0, 2.1, 0), null, "§bSolo", LeaderboardUpdate.SECOND, this::getSoloCount);
            InfoHologram infoHologram1 = new InfoHologram(player, DOUBLE.getLocation().clone().add(0, 2.1, 0), null, "§bDuplas", LeaderboardUpdate.SECOND, this::getDoublesCount);

            infoHologram.setInteract(interact);
            infoHologram1.setInteract(interact);

            infoHologram.show();
            infoHologram1.show();

            LeaderboardHologram leaderboardHologram = new LeaderboardHologram(winsLeaderboard, "§e§lTOP 100 §b§lWINS SOLO §7(%s/%s)", player, winsLocation);
            leaderboardHologram.show();

            LeaderboardHologram leaderboardHologram1 = new LeaderboardHologram(killsLeaderboard, "§e§lTOP 100 §b§LPONTOS SOLO §7(%s/%s)", player, killsLocation);
            leaderboardHologram1.show();

            LeaderboardHologram leaderboardHologram2 = new LeaderboardHologram(winstreakLeaderboard, "§e§lTOP 100 §b§lWINSTREAK SOLO §7(%s/%s)", player, winstreakLocation);
            leaderboardHologram2.show();

            LeaderboardHologram leaderboardHologram3 = new LeaderboardHologram(winsDuoLeaderboard, "§e§lTOP 100 §b§lWINS DUPLAS §7(%s/%s)", player, winsDuoLocation);
            leaderboardHologram3.show();

            LeaderboardHologram leaderboardHologram4 = new LeaderboardHologram(killsDuoLeaderboard, "§e§lTOP 100 §b§lPONTOS DUPLAS §7(%s/%s)", player, killsDuoLocation);
            leaderboardHologram4.show();

            LeaderboardHologram leaderboardHologram5 = new LeaderboardHologram(winstreakDuoLeaderboard, "§e§lTOP 100 §b§lWINSTREAK DUPLAS §7(%s/%s)", player, winstreakDuoLocation);
            leaderboardHologram5.show();

            EntityPlayer playerNMS = ((CraftPlayer) player).getHandle();

            final NPC statistics = new NPC(player, stats, playerNMS.getProfile().getProperties().get("textures").iterator().next());

            statistics.setInteractExecutor((p, npc, type) -> openStatistics(p));
            statistics.spawn(true);

            final Hologram statisticsHologram = new Hologram(player, stats.clone().add(0, 2.1, 0), "§b§lSUAS ESTATÍSTICAS", "§eClique para ver");

            statisticsHologram.setInteract(interact);

            statisticsHologram.show();

            if (ranking.values().size() >= 3) {
                LeaderboardData data;
                SkinData skin;

                new Hologram(player, bestPlayers, user.getAccount().getLanguage().translate("lobby.bridge.best_players")).show();

                data = ranking.values().get(0);
                skin = data.getSkinData();
                new NPC(player, loc1, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                Ranking rank = Ranking.fromId(data.getValue(Columns.BRIDGE_RANK).getAsInt());

                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc1.clone().add(0, 2.1, 0), "§a" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.BRIDGE_RANK_EXP).getAsInteger()).show();

                data = ranking.values().get(1);
                skin = data.getSkinData();
                new NPC(player, loc2, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                rank = Ranking.fromId(data.getValue(Columns.BRIDGE_RANK).getAsInt());

                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc2.clone().add(0, 2.1, 0), "§e" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.BRIDGE_RANK_EXP).getAsInteger()).show();

                data = ranking.values().get(2);
                skin = data.getSkinData();
                new NPC(player, loc3, new Property("textures", skin.getValue(), skin.getSignature())).spawn(true);
                rank = Ranking.fromId(data.getValue(Columns.BRIDGE_RANK).getAsInt());

                if (rank == Ranking.MASTER_IV)
                    rank = Ranking.CHALLENGER;

                new Hologram(player, loc3.clone().add(0, 2.1, 0), "§c" + data.getName() + " " + rank.getColor() + rank.getSymbol() + rank.getDisplay(), "§7Exp: §f" + data.getValue(Columns.BRIDGE_RANK_EXP).getAsInteger()).show();

            }

        }, (user.getAccount().getVersion() >= 47 ? 0 : 5));
    }

    private final NPC SOLO = NPC.builder().location(new Location(Bukkit.getWorld("world"), 1.5, 64.375, 41.5, -180, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1MzQxMzY3NzA4MTMsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81OGUzZTdmOGY0MWYxOTJmNzQzOWI0YjllOTU2ZDk4ZjQzYzAzOGNiODQwZjIzYWJlYjg1YmI2ZmY2MDBkYjY1In19fQ==", "OJzADdLod8MMXbIEqKdKmqdcOyNh3OuUPXxQOBruCy6rMPiWv8cA7S1mf9YNsERCTj8Fxe3uqnEA3Z9eDt9ROkL3RTg8MQvC18Yr3o+dqriwRRrOwFuFShutTg1vb239Zv3O99YaLYHg6b7+RvBDFUldM9hzlSTsZ9YucUTOLvfS5kA4+n8o9w/ZhIMP045FciNuGHSR8f/HANJLIpa2bXv/38VRnp7V9i9OcPoODctE8YqbZ/MfY5lgkWjVcqn+hrYISkKP1ICABE1+/ns3zL3uvc2FBYQZ0hpO17Y4OlZ4Zi9WQFsD0vGRWOMhgtP4Q4+tq0nH6gqQ6kQYK8rXKMYU0EgShCFtZynFwjSmTOE51lhuxhjYSWGQP1Ux/uK6ltF8CK6bsvcjEZIN8Tyn+GvjsffEv48uIjL/z7hHNVv0gsulUtslcNuikmdbwoMFjFQGbshRzhUDK1LyM9u7n8d42VU1VCzmqYvXM864vj0Ledfrp5GI3UOUlBq1Fdaiw8pbnp1L1tLnaAcl9qOX/EB5KE43/1kkCQVnzF4I9XCMKmIVOh4VXOlrhZiorkXkP+Zm4B3pJU8qg3TsuxDBZQRJXUB8uK3HfySzDim2y57ww12vv0vsgIl/PM8MjvqJ6oP9GfdlCTZiMkLC43HqfYY52iFCU0doVuU/d6R3JDk=")).interactExecutor((player, npc, type) -> connect(player, GameType.SOLO)).build();

    private final NPC DOUBLE = NPC.builder().location(new Location(Bukkit.getWorld("world"), -0.5, 64.375, 41.5, -180, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1MzQxMzY3NzA4MTMsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81OGUzZTdmOGY0MWYxOTJmNzQzOWI0YjllOTU2ZDk4ZjQzYzAzOGNiODQwZjIzYWJlYjg1YmI2ZmY2MDBkYjY1In19fQ==", "OJzADdLod8MMXbIEqKdKmqdcOyNh3OuUPXxQOBruCy6rMPiWv8cA7S1mf9YNsERCTj8Fxe3uqnEA3Z9eDt9ROkL3RTg8MQvC18Yr3o+dqriwRRrOwFuFShutTg1vb239Zv3O99YaLYHg6b7+RvBDFUldM9hzlSTsZ9YucUTOLvfS5kA4+n8o9w/ZhIMP045FciNuGHSR8f/HANJLIpa2bXv/38VRnp7V9i9OcPoODctE8YqbZ/MfY5lgkWjVcqn+hrYISkKP1ICABE1+/ns3zL3uvc2FBYQZ0hpO17Y4OlZ4Zi9WQFsD0vGRWOMhgtP4Q4+tq0nH6gqQ6kQYK8rXKMYU0EgShCFtZynFwjSmTOE51lhuxhjYSWGQP1Ux/uK6ltF8CK6bsvcjEZIN8Tyn+GvjsffEv48uIjL/z7hHNVv0gsulUtslcNuikmdbwoMFjFQGbshRzhUDK1LyM9u7n8d42VU1VCzmqYvXM864vj0Ledfrp5GI3UOUlBq1Fdaiw8pbnp1L1tLnaAcl9qOX/EB5KE43/1kkCQVnzF4I9XCMKmIVOh4VXOlrhZiorkXkP+Zm4B3pJU8qg3TsuxDBZQRJXUB8uK3HfySzDim2y57ww12vv0vsgIl/PM8MjvqJ6oP9GfdlCTZiMkLC43HqfYY52iFCU0doVuU/d6R3JDk=")).interactExecutor((player, npc, type) -> connect(player, GameType.DOUBLE)).build();

    private final NPC SHOP = NPC.builder().location(new Location(Bukkit.getWorld("world"), 3.5, 65.0, 4.5, 148, 0)).property(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1ODU0MzE3NjYyMTEsInByb2ZpbGVJZCI6ImRkZWQ1NmUxZWY4YjQwZmU4YWQxNjI5MjBmN2FlY2RhIiwicHJvZmlsZU5hbWUiOiJEaXNjb3JkQXBwIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNWMyMjMzMWNiMGYzOGIxZDExMmQyZDFjNzk2ZDZiZWQ3MGI5YWU1ZTM0NTM2ODU4Njg2N2MyMjI5NDk2MGZjIn19fQ==", "d5PemnvK8zDQJuTWn+XRYHXOnnAFz0X2NRuI775qzj5tmuH82921YMiKTfhbMlMVkLN5cmE4FHGzJ0HXUgxalTqlGVnvZuoE3LWVsERUHMsspqvt0nLInSZW4ZazAr9/fIgcnZ/0GI0DQhR1vm6KzBRv5szMOT2Ityr7g6Vy69UM376efxJDsJZ5iwW50Op7FiPN+YaVS+zaWsL99H6EDA6vpsWArp8GvLKkE8Y7c0+Gc24znPwiaPjSMsrQw1gNvjN+7P8A8vW2rI0LBXfxJgAOEBltYjG0dr7VB0Pv+gH9zf/mJ6J+t0iNpnSa5i+B8tkk0affxImR1+0R55uetlZgixvbg1RBrXsNWN/i68hv0x8Afd1nOvT0R8FJSGff8WJJC+RYphYmpZ9V791W8zdFrzkAnac499V6+gxIJw1gzoW6oUpDY53wCx+kd7DIeG7F7bz2aO+R+KjoCoVdzzsjZlF6OIYFvhenmtdVR4VIZZtlOjcrGVN4hTA1gxEbftokjjVvQPyMFpa+fkjrMjbxIygw2jhAWVqmpjD5tVGQ9GfYkF5q2IO6isG/QyIlxuEy6cl7IKBTw30XR9BznP5jP4+3O4KFPVMtuerG37rUyTh8gsIWo9+8mwAto3EHgXIjhk+8TMIBSfkdhJON2J9MJBGhdoLuj1nbdbm+qp8=")).interactExecutor((player, npc, type) -> getShopMenu().open(player)).build();

    private final Selector shopMenu = Selector.builder().withSize(27).withName("Comerciante")
            .withCustomItem(11, new InteractableItem(new ItemFactory(Material.DIAMOND_HELMET).setName("§aChapéus").setDescription("§7Selecione um chapéu para usar durante a partida.\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    return true;
                }
            }).getItemStack())
            .withCustomItem(13, new InteractableItem(new ItemFactory(Material.ENDER_PORTAL_FRAME).setName("§aPortais").setDescription("§7Selecione um estilo de portal para usar na partida.\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    return true;
                }
            }).getItemStack())
            .withCustomItem(15, new InteractableItem(new ItemFactory(Material.MOB_SPAWNER).setName("§aCabines").setDescription("§7Selecione uma cabine para nascer antes de cada rodada da partida.\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {

                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openCages(player);
                    return true;
                }
            }).getItemStack())
            .build();

    public Selector getShopMenu() {
        return shopMenu;
    }

    public final List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    protected void openCages(final Player player) {
        final Account account = Account.fetch(player.getUniqueId());

        final Selector.Builder builder = Selector.builder().withAllowedSlots(allowedSlots).withSize(54).withName("Cabines");

        final List<ItemStack> itemStacks = new ArrayList<>();

        getCagesConfig().forEach(bridgeCageConfig -> {
            boolean hasCage = account.hasPermission(bridgeCageConfig.getRank());

            ItemFactory itemFactory = new ItemFactory(bridgeCageConfig.getIcon().getMaterial());

            if(account.getData(Columns.BRIDGE_CAGE).getAsString().equalsIgnoreCase(bridgeCageConfig.getDisplayName())) {
                itemFactory.addEnchantment(Enchantment.DURABILITY, 1);
            }

            itemFactory.setDurability(bridgeCageConfig.getIcon().getData());
            itemFactory.setName((hasCage ? "§a" : "§c") + bridgeCageConfig.getDisplayName());
            itemFactory.setDescription("\n" + "§7Raridade: §r" + bridgeCageConfig.getRarity().getDisplayName() + "\n" + "§7Exclusivo para " + bridgeCageConfig.getRank().getDefaultTag().getColor() + bridgeCageConfig.getRank().getName() + "\n\n" +                     (hasCage ? (account.getData(Columns.BRIDGE_CAGE).getAsString().equalsIgnoreCase(bridgeCageConfig.getDisplayName()) ? "§cClique para remover" : "§eClique para selecionar.") : "§cVocê não possui essa cage."));
            itemFactory.addItemFlag(ItemFlag.HIDE_ENCHANTS);

            InteractableItem interactableItem = new InteractableItem(itemFactory.getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    if (!hasCage) {
                        player.sendMessage("§cVocê não possui essa cabine.");
                        return true;
                    }

                    if (account.getData(Columns.BRIDGE_CAGE).getAsString().equalsIgnoreCase(bridgeCageConfig.getDisplayName())) {
                        account.getDataStorage().getData(Columns.BRIDGE_CAGE).setData("Default");
                        player.sendMessage("§cCabine removida.");
                        async(() -> account.getDataStorage().saveColumn(Columns.BRIDGE_CAGE));
                        player.closeInventory();
                        return true;
                    }


                    account.getDataStorage().getData(Columns.BRIDGE_CAGE).setData(bridgeCageConfig.getDisplayName());
                    async(() -> account.getDataStorage().saveColumn(Columns.BRIDGE_CAGE));
                    player.sendMessage("§aVocê selecionou a cabine §f" + bridgeCageConfig.getDisplayName() + "§a.");
                    player.closeInventory();
                    return true;
                }
            });

            itemStacks.add(interactableItem.getItemStack());
        });

        builder.withItems(itemStacks).build().open(player);
    }

    protected void openStatistics(final Player player) {
        final Account target = Account.fetch(player.getUniqueId());
        CompletableFuture.runAsync(() -> target.getDataStorage().loadIfUnloaded(Tables.THE_BRIDGE.getColumns())).thenRun(() -> {
            final Selector.Builder builder = Selector.builder().withSize(45).withName("Estatísticas: The Bridge");

            builder.withCustomItem(13, new ItemFactory(Material.PAPER).setName("§aEstatísticas gerais").setDescription("§7Wins: §a" + (target.getData(Columns.BRIDGE_SOLO_WINS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_WINS).getAsInt()) + "\n§7Losses: §a" + (target.getData(Columns.BRIDGE_SOLO_LOSSES).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_LOSSES).getAsInt()) + "\n§7Kills: §a" + (target.getData(Columns.BRIDGE_SOLO_KILLS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_KILLS).getAsInt()) + "\n§7Deaths: §a" + (target.getData(Columns.BRIDGE_SOLO_DEATHS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_DEATHS).getAsInt()) + "\n§7Points: §a" + (target.getData(Columns.BRIDGE_SOLO_POINTS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_POINTS).getAsInt()) + "\n§7Rounds: §a" + (target.getData(Columns.BRIDGE_SOLO_ROUNDS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_ROUNDS).getAsInt()) + "\n§7Exp: §a" + target.getData(Columns.BRIDGE_RANK_EXP).getAsInteger()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());

            builder.withCustomItem(30, new ItemFactory(Material.STONE_SWORD).setName("§aEstatísticas no Solo").setDescription("§7Wins: §a" + target.getData(Columns.BRIDGE_SOLO_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.BRIDGE_SOLO_LOSSES).getAsInteger() + "\n§7Kills: §a" + target.getData(Columns.BRIDGE_SOLO_KILLS).getAsInteger() + "\n§7Deaths: §a" + target.getData(Columns.BRIDGE_SOLO_DEATHS).getAsInteger() + "\n§7Points: §a" + target.getData(Columns.BRIDGE_SOLO_POINTS).getAsInteger() + "\n§7Rounds: §a" + target.getData(Columns.BRIDGE_SOLO_ROUNDS).getAsInteger() + "\n§7Winstreak: §a" + target.getData(Columns.BRIDGE_SOLO_WINSTREAK).getAsInteger() + "\n§7Best winstreak: §a" + target.getData(Columns.BRIDGE_SOLO_MAX_WINSTREAK).getAsInteger()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());

            builder.withCustomItem(32, new ItemFactory(Material.IRON_SWORD).setName("§aEstatísticas no Duplas").setDescription("§7Wins: §a" + target.getData(Columns.BRIDGE_DOUBLES_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.BRIDGE_DOUBLES_LOSSES).getAsInteger() + "\n§7Kills: §a" + target.getData(Columns.BRIDGE_DOUBLES_KILLS).getAsInteger() + "\n§7Deaths: §a" + target.getData(Columns.BRIDGE_DOUBLES_DEATHS).getAsInteger() + "\n§7Points: §a" + target.getData(Columns.BRIDGE_DOUBLES_POINTS).getAsInteger() + "\n§7Rounds: §a" + target.getData(Columns.BRIDGE_DOUBLES_ROUNDS).getAsInteger() + "\n§7Winstreak: §a" + target.getData(Columns.BRIDGE_DOUBLES_WINSTREAK).getAsInteger() + "\n§7Best winstreak: §a" + target.getData(Columns.BRIDGE_DOUBLES_MAX_WINSTREAK).getAsInteger()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());

            builder.build().open(player);
        });
    }

    protected void connect(Player player, GameType gameType) {
        if (isConnectionCooldown(player.getUniqueId())) {
            Account account = Account.fetch(player.getUniqueId());
            Cooldown cooldown = this.getCooldown(player.getUniqueId());

            player.sendMessage(account.getLanguage().translate("wait_to_connect", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        }

        addCooldown(player.getUniqueId());

        Account account = Account.fetch(player.getUniqueId());

        if (server == null || server.isDead()) {
            player.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("no_server_available", "the_bridge")));
            return;
        }

        player.sendMessage(account.getLanguage().translate("arcade.room.searching"));

        BridgeRouteContext bridgeRouteContext = new BridgeRouteContext();

        bridgeRouteContext.setGameType(gameType);
        bridgeRouteContext.setPlayMode(Vanish.getInstance().isVanished(account) ? PlayMode.VANISH : PlayMode.PLAYER);

        ServerRedirect.Route route = new ServerRedirect.Route(server, Constants.GSON.toJson(bridgeRouteContext));

        ServerRedirect redirect = new ServerRedirect(account.getUniqueId(), route);
        account.connect(redirect);
    }

    @EventHandler
    public void onServerPayloadReceiveEvent(ServerPayloadReceiveEvent event) {
        if (event.getServer().getServerCategory() != ServerCategory.THE_BRIDGE)
            return;
        solo_count = ((Double) event.getPayload().get(GameType.SOLO.name())).intValue();
        doubles_count = ((Double) event.getPayload().get(GameType.DOUBLE.name())).intValue();
    }

    public int solo_count, doubles_count;

    public int getSoloCount() {
        return (server.isDead() ? -1 : solo_count);
    }

    public int getDoublesCount() {
        return (server.isDead() ? -1 : doubles_count);
    }

    public Set<BridgeCageConfig> getCagesConfig() {
        return cagesConfig;
    }
}
