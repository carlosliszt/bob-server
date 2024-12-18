package com.minecraft.hub.lobby.duels;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.server.ServerType;
import com.minecraft.hub.Hub;
import com.minecraft.hub.lobby.Lobby;
import com.minecraft.hub.lobby.duels.modules.DuelProvider;
import com.minecraft.hub.user.User;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class Duels extends Lobby {

    private final ItemStack itemStack;
    private final DuelProvider duelProvider;

    public Duels(Hub hub) {
        super(hub, true);

        this.duelProvider = new DuelProvider(hub);

        Constants.setServerType(ServerType.DUELS_LOBBY);
        Constants.setLobbyType(ServerType.MAIN_LOBBY);

        setBossbar("§b§lDUELS NO BOBMC.COM.BR");

        this.itemStack = new ItemFactory(Material.BLAZE_ROD).setName("§aDesafiar §7(Direito no jogador)").getStack();
    }

    @Override
    public void handleScoreboard(User user, String displayName) {
        final GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        gameScoreboard.updateTitle(displayName);

        Collection<String> lines = new ArrayList<>();

        final int playerCount = Constants.getServerStorage().count();

        lines.add(" ");
        lines.add("§eUse §6/duel <player> §epara");
        lines.add("§edesafiar outros jogadores.");
        lines.add(" ");
        lines.add("§eBoxing:");
        lines.add(" §fWins: §b0");
        lines.add(" §fWinstreak: §b0");
        lines.add(" ");
        lines.add("§eSimulator:");
        lines.add(" §fWins: §b0");
        lines.add(" §fWinstreak: §b0");
        lines.add(" ");
        lines.add("§fPlayers: §a" + (playerCount == -1 ? "..." : playerCount));
        lines.add(" ");
        lines.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(lines);
    }

    @Override
    public void handleJoin(Player player) {

    }

    @Override
    public void handleItems(Player player, Account account) {
        super.handleItems(player, account);

        player.getInventory().setItem(2, itemStack);
    }

    @EventHandler
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        if (event.getAction() == Action.PHYSICAL) return;
    }

    @Getter
    @Setter
    public static class Configuration {

        private Location gladiatorNpc;
        private Location simulatorNpc;
        private Location scrimNpc;
        private Location soupNpc;

        private Location boxingNpc;
        private Location sumoNpc;
        private Location uhcNpc;

        private Location statisticsNpc;

    }

}