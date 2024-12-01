package com.minecraft.thebridge.user;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.server.route.BridgeRouteContext;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.thebridge.TheBridge;
import com.minecraft.thebridge.game.Game;
import com.minecraft.thebridge.game.cage.Cage;
import com.minecraft.thebridge.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.UUID;

@Getter
@Setter
public class User {

    private Account account;
    private final BridgeRouteContext routeContext;

    private Player player;

    private Game game;
    private Team team;
    private Cage cage;

    private UUID lastCombat = null;
    private long lastCombatTime = 0L;
    private int kills, points;

    private boolean inCage = false;

    private GameScoreboard scoreboard;

    public User(Account account, BridgeRouteContext context) {
        this.account = account;
        this.routeContext = context;
    }

    public UUID getUniqueId() {
        return getAccount().getUniqueId();
    }

    public String getName() {
        return getAccount().getDisplayName();
    }

    public static User fetch(UUID uuid) {
        return TheBridge.getInstance().getUserStorage().getUser(uuid);
    }

    public User setAccount(Account account) {
        this.account = account;
        account.getDataStorage().loadColumns(Collections.singletonList(Columns.BRIDGE_CAGE));
        Cage cage = TheBridge.getInstance().getCageStorage().getCage(account.getData(Columns.BRIDGE_CAGE).getAsString());
        if(cage != null) {
            this.cage = cage;
        } else {
            this.cage = TheBridge.getInstance().getCageStorage().getDefaultCage();
        }
        return this;
    }

    public void addKill() {
        kills++;
    }

    public void addPoint() {
        points++;
    }

    public boolean isPlaying() {
        return game != null && game.getAliveUsers().contains(this);
    }

    public void lobby() {
        Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.THE_BRIDGE_LOBBY);

        if (server == null)
            server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY);

        if (server != null) {
            account.connect(server);
        } else {
            Bukkit.getScheduler().runTaskLater(TheBridge.getInstance(), () -> getPlayer().kickPlayer("§c" + account.getLanguage().translate("no_server_available", "lobby")), 1);
        }
    }

    public void restoreCombat() {
        lastCombatTime = 0L;
        lastCombat = null;
    }

    public void restorePointsAndKills() {
        kills = 0;
        points = 0;
    }

    public void addCombat(UUID uuid) {
        lastCombatTime = 15000L + System.currentTimeMillis();
        lastCombat = uuid;
    }

    public void giveItems(final Team team) {
        final Player player = getPlayer();

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        if (team.getChatColor() == ChatColor.RED) {
            player.getInventory().setChestplate(TheBridge.getInstance().getConstants().RED_CHESTPLATE);
            player.getInventory().setLeggings(TheBridge.getInstance().getConstants().RED_LEGGINGS);
            player.getInventory().setBoots(TheBridge.getInstance().getConstants().RED_BOOTS);

            final ItemStack CLAY = new ItemStack(Material.STAINED_CLAY, 64, (short) 14);

            player.getInventory().setItem(3, CLAY);
            player.getInventory().setItem(4, CLAY);
        }

        if (team.getChatColor() == ChatColor.BLUE) {
            player.getInventory().setChestplate(TheBridge.getInstance().getConstants().BLUE_CHESTPLATE);
            player.getInventory().setLeggings(TheBridge.getInstance().getConstants().BLUE_LEGGINGS);
            player.getInventory().setBoots(TheBridge.getInstance().getConstants().BLUE_BOOTS);

            final ItemStack CLAY = new ItemStack(Material.STAINED_CLAY, 64, (short) 11);

            player.getInventory().setItem(3, CLAY);
            player.getInventory().setItem(4, CLAY);
        }

        player.getInventory().setItem(0, new ItemFactory(Material.IRON_SWORD).setUnbreakable().getStack());
        player.getInventory().setItem(1, new ItemFactory(Material.BOW).setUnbreakable().getStack());
        player.getInventory().setItem(2, new ItemFactory(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 2).setUnbreakable().getStack());
        player.getInventory().setItem(5, new ItemStack(Material.GOLDEN_APPLE, 8));
        player.getInventory().setItem(8, new ItemStack(Material.ARROW));
        player.updateInventory();
    }

    @Override
    public String toString() {
        return "User{" + getName() + "}";
    }
}