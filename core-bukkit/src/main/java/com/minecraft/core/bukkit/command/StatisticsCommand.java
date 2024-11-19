package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StatisticsCommand implements BukkitInterface {

    @Command(name = "stats", usage = "{label} <target>", platform = Platform.PLAYER)
    public final void onExecute(final Context<Player> playerContext) {
        final Account account = playerContext.getAccount();

        final String[] arguments = playerContext.getArgs();

        if (arguments.length == 0) {
            Statistics statistics = new Statistics(playerContext.getSender(), account);
            CompletableFuture.runAsync(statistics::load).thenRun(statistics::open);
        } else {
            final Player target = Bukkit.getPlayer(arguments[0]);

            if (target == null || !playerContext.getSender().canSee(target)) {
                playerContext.info("target.not_found");
                return;
            }

            final Account targetAccount = Account.fetch(target.getUniqueId());

            if (!targetAccount.getPreference(Preference.STATISTICS) && account.getUniqueId() != targetAccount.getUniqueId() && account.getRank().getId() < Rank.TRIAL_MODERATOR.getId()) {
                playerContext.sendMessage("§cEste jogador desativou a visibilidade das estatísticas.");
                return;
            }

            Statistics statistics = new Statistics(playerContext.getSender(), targetAccount);
            CompletableFuture.runAsync(statistics::load).thenRun(statistics::open);
        }
    }


    @RequiredArgsConstructor
    @Getter
    public static class Statistics {

        private final Player sender;
        private final Account target;

        public void load() {
            target.getDataStorage().loadIfUnloaded(Columns.HG_WINS, Columns.HG_KILLS, Columns.HG_DEATHS, Columns.SCRIM_WINS, Columns.SCRIM_KILLS, Columns.SCRIM_DEATHS, Columns.HG_COINS, Columns.PVP_ARENA_KILLS, Columns.PVP_ARENA_DEATHS, Columns.PVP_ARENA_MAX_KILLSTREAK, Columns.PVP_FPS_KILLS, Columns.PVP_FPS_DEATHS, Columns.PVP_FPS_MAX_KILLSTREAK, Columns.PVP_COINS, Columns.DUELS_SOUP_WINS, Columns.DUELS_SOUP_LOSSES, Columns.DUELS_SOUP_GAMES, Columns.DUELS_SOUP_MAX_WINSTREAK, Columns.DUELS_SOUP_RATING, Columns.DUELS_GLADIATOR_WINS, Columns.DUELS_GLADIATOR_LOSSES, Columns.DUELS_GLADIATOR_GAMES, Columns.DUELS_GLADIATOR_MAX_WINSTREAK, Columns.DUELS_GLADIATOR_RATING, Columns.DUELS_GLADIATOR_OLD_RATING, Columns.DUELS_SIMULATOR_WINS, Columns.DUELS_SIMULATOR_LOSSES, Columns.DUELS_SIMULATOR_GAMES, Columns.DUELS_SIMULATOR_MAX_WINSTREAK, Columns.DUELS_SIMULATOR_RATING, Columns.DUELS_UHC_WINS, Columns.DUELS_UHC_LOSSES, Columns.DUELS_UHC_GAMES, Columns.DUELS_UHC_MAX_WINSTREAK, Columns.DUELS_UHC_RATING, Columns.DUELS_SUMO_WINS, Columns.DUELS_SUMO_LOSSES, Columns.DUELS_SUMO_GAMES, Columns.DUELS_SUMO_MAX_WINSTREAK, Columns.DUELS_SUMO_RATING, Columns.DUELS_BOXING_WINS, Columns.DUELS_BOXING_LOSSES, Columns.DUELS_BOXING_GAMES, Columns.DUELS_BOXING_MAX_WINSTREAK, Columns.DUELS_BOXING_RATING, Columns.SCRIM_WINS, Columns.DUELS_SCRIM_LOSSES, Columns.SCRIM_GAMES, Columns.SCRIM_MAX_GAME_KILLS, Columns.DUELS_SCRIM_RATING, Columns.BRIDGE_SOLO_WINS, Columns.BRIDGE_SOLO_LOSSES, Columns.BRIDGE_SOLO_KILLS, Columns.BRIDGE_SOLO_DEATHS, Columns.BRIDGE_SOLO_POINTS, Columns.BRIDGE_SOLO_ROUNDS, Columns.BRIDGE_SOLO_MAX_WINSTREAK, Columns.BRIDGE_DOUBLES_WINS, Columns.BRIDGE_DOUBLES_LOSSES, Columns.BRIDGE_DOUBLES_KILLS, Columns.BRIDGE_DOUBLES_DEATHS, Columns.BRIDGE_DOUBLES_POINTS, Columns.BRIDGE_DOUBLES_ROUNDS, Columns.BRIDGE_DOUBLES_MAX_WINSTREAK, Columns.BRIDGE_COINS, Columns.TIOGERSON_GAMES, Columns.TIOGERSON_MAX_WINSTREAK, Columns.TIOGERSON_LOSSES, Columns.TIOGERSON_WINS, Columns.TIOGERSON_WINSTREAK);
        }

        public void open() {
            Selector.Builder selector = Selector.builder().withSize(27).withName("Estatísticas: " + target.getDisplayName());

            selector.withCustomItem(10, new ItemFactory(Material.MUSHROOM_SOUP).setName("§aHG").setDescription("§7Vitórias HG Mix: §a" + target.getData(Columns.HG_WINS).getAsInteger() + "\n" + "§7Kills HG Mix: §a" + target.getData(Columns.HG_KILLS).getAsInteger() + "\n" + "§7Mortes HG Mix: §a" + target.getData(Columns.HG_DEATHS).getAsInteger() + "\n§7Ratio: §a" + target.getRatio(target.getData(Columns.HG_KILLS).getAsInt(), target.getData(Columns.HG_DEATHS).getAsInt()) + "\n\n" + "§7Vitórias Scrim: §a" + target.getData(Columns.SCRIM_WINS).getAsInteger() + "\n" + "§7Kills Scrim: §a" + target.getData(Columns.SCRIM_KILLS).getAsInteger() + "\n" + "§7Mortes Scrim: §a" + target.getData(Columns.SCRIM_DEATHS).getAsInteger() + "\n§7Ratio: §a" + target.getRatio(target.getData(Columns.SCRIM_KILLS).getAsInt(), target.getData(Columns.SCRIM_DEATHS).getAsInt()) + "\n\n" + "§7Coins: §6" + target.getData(Columns.HG_COINS).getAsInteger()).getStack());
            selector.withCustomItem(11, new InteractableItem(new ItemFactory(Material.DIAMOND_SWORD).setName("§aDuels").setDescription("§eClique para ver mais.").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openDuels();
                    return true;
                }
            }).getItemStack());
            selector.withCustomItem(12, new ItemFactory(Material.IRON_CHESTPLATE).setName("§aPvP").setDescription("§7Kills Arena: §a" + target.getData(Columns.PVP_ARENA_KILLS).getAsInteger() + "\n" + "§7Deaths Arena: §a" + target.getData(Columns.PVP_ARENA_DEATHS).getAsInteger() + "\n" + "§7Maior Killstreak Arena: §a" + target.getData(Columns.PVP_ARENA_MAX_KILLSTREAK).getAsInteger() + "\n\n" + "§7Kills Fps: §a" + target.getData(Columns.PVP_FPS_KILLS).getAsInteger() + "\n" + "§7Deaths Fps: §a" + target.getData(Columns.PVP_FPS_DEATHS).getAsInteger() + "\n" + "§7Maior Killstreak Fps: §a" + target.getData(Columns.PVP_FPS_MAX_KILLSTREAK).getAsInteger() + "\n\n" + "§7Coins: §6" + target.getData(Columns.PVP_COINS).getAsInteger()).getStack());
            selector.withCustomItem(13, new ItemFactory(Material.STICK).setName("§aTio Gerson").setDescription("§7Jogos: §a" + target.getData(Columns.TIOGERSON_GAMES).getAsInteger() + "\n" + "§7Vitórias: §a" + target.getData(Columns.TIOGERSON_WINS).getAsInteger() + "\n" + "§7Derrotas: §a" + target.getData(Columns.TIOGERSON_LOSSES).getAsInteger() + "\n" + "§7Maior Winstreak: §a" + target.getData(Columns.TIOGERSON_MAX_WINSTREAK).getAsInteger() + "\n" + "§7Winstreak: §a" + target.getData(Columns.TIOGERSON_WINSTREAK).getAsInteger()).getStack());
            selector.build().open(sender);
        }

        public void openDuels() {
            Selector.Builder selector = Selector.builder().withSize(36).withName("Estatísticas: " + target.getDisplayName() + " (Duels)");

            selector.withCustomItem(10, new ItemFactory(Material.MUSHROOM_SOUP).setName("§aSoup").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_SOUP_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_SOUP_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_SOUP_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_SOUP_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_SOUP_MAX_WINSTREAK).getAsInteger()).getStack());
            selector.withCustomItem(11, new ItemFactory(Material.IRON_FENCE).setName("§aGladiator").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_GLADIATOR_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_GLADIATOR_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_GLADIATOR_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_GLADIATOR_RATING).getAsInteger() + "\n§7Rating Old: §a" + target.getData(Columns.DUELS_GLADIATOR_OLD_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_GLADIATOR_MAX_WINSTREAK).getAsInteger()).getStack());
            selector.withCustomItem(12, new ItemFactory(Material.WEB).setName("§aSimulator").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_SIMULATOR_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_SIMULATOR_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_SIMULATOR_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_SIMULATOR_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_SIMULATOR_MAX_WINSTREAK).getAsInteger()).getStack());
            selector.withCustomItem(13, new ItemFactory(Material.GOLDEN_APPLE).setName("§aUHC").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_UHC_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_UHC_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_UHC_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_UHC_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_UHC_MAX_WINSTREAK).getAsInteger()).getStack());
            selector.withCustomItem(14, new ItemFactory(Material.LEASH).setName("§aSumo").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_SUMO_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_SUMO_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_SUMO_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_SUMO_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_SUMO_MAX_WINSTREAK).getAsInteger()).getStack());
            selector.withCustomItem(15, new ItemFactory(Material.DIAMOND_SWORD).setName("§aBoxing").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_BOXING_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_BOXING_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_BOXING_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_BOXING_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_BOXING_MAX_WINSTREAK).getAsInteger()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());
            selector.withCustomItem(16, new ItemFactory(Material.INK_SACK).setDurability(3).setName("§aScrim").setDescription("§7Wins: §a" + target.getData(Columns.DUELS_SCRIM_WINS).getAsInteger() + "\n§7Losses: §a" + target.getData(Columns.DUELS_SCRIM_LOSSES).getAsInteger() + "\n§7Games: §a" + target.getData(Columns.DUELS_SCRIM_GAMES).getAsInteger() + "\n§7Rating: §a" + target.getData(Columns.DUELS_SCRIM_RATING).getAsInteger() + "\n§7Maior Winstreak: §a" + target.getData(Columns.DUELS_SCRIM_MAX_WINSTREAK).getAsInteger()).getStack());
            selector.withCustomItem(19, new ItemFactory(Material.STAINED_CLAY).setDurability(11).setName("§aThe Bridge").setDescription("§7Wins: §a" + (target.getData(Columns.BRIDGE_SOLO_WINS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_WINS).getAsInt()) + "\n§7Losses: §a" + (target.getData(Columns.BRIDGE_SOLO_LOSSES).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_LOSSES).getAsInt()) + "\n§7Kills: §a" + (target.getData(Columns.BRIDGE_SOLO_KILLS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_KILLS).getAsInt()) + "\n§7Deaths: §a" + (target.getData(Columns.BRIDGE_SOLO_DEATHS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_DEATHS).getAsInt()) + "\n§7Points: §a" + (target.getData(Columns.BRIDGE_SOLO_POINTS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_POINTS).getAsInt()) + "\n§7Rounds: §a" + (target.getData(Columns.BRIDGE_SOLO_ROUNDS).getAsInt() + target.getData(Columns.BRIDGE_DOUBLES_ROUNDS).getAsInt()) + "\n\n" + "§7Coins: §6" + target.getData(Columns.BRIDGE_COINS).getAsInteger()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());

            selector.withCustomItem(31, new InteractableItem(new ItemFactory(Material.ARROW).setName("§aVoltar").setDescription("§7Para Estatísticas: " + target.getDisplayName()).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    open();
                    return true;
                }
            }).getItemStack());

            selector.build().open(sender);
        }

    }

    @Completer(name = "stats")
    public final List<String> handleComplete(final Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }
}