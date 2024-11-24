/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.Nenhum;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.GameStorage;
import com.minecraft.hungergames.util.game.GameType;
import com.minecraft.hungergames.util.game.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInitialSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LmCommand implements Assistance, BukkitInterface {

    @Command(name = "lm", platform = Platform.PLAYER, rank = Rank.EVENT_MOD, usage = "lm <mode> or /lm <type> <gametype>")
    public void handleCommand(Context<Player> context, String name) {

        if (hasStarted()) {
            context.info("hg.game.already_started");
            return;
        }

        Class<?> classGame = GameStorage.getGame(name);

        if (classGame == null && !name.equalsIgnoreCase("type")) {
            context.info("object.not_found", "Preset");
            return;
        } else if (name.equalsIgnoreCase("type")) {

            if (context.argsCount() < 2) {
                context.sendMessage("§cUso do /lm:");
                context.sendMessage("§c* /lm type <gametype>");
                return;
            }

            String type = context.getArg(1);

            GameType gameType = GameType.fromString(type.toUpperCase());

            if (gameType == null) {
                context.info("object.not_found", "Type");
                return;
            }

            Game game = getGame();
            GameType current = game.getType();

            if (gameType == current) {
                context.sendMessage("§cO servidor já está rodando este modo.");
                return;
            }

            context.info("§6[Game] §eType alterado para " + gameType);

            getPlugin().getUserStorage().getUsers().forEach(users -> {
                Kit[] kits = new Kit[gameType.getMaxKits()];
                Arrays.fill(kits, getPlugin().getKitStorage().getDefaultKit());
                users.setKits(kits);
            });

            context.info("§6[Game] §eDescarregando modo " + getGame().getClass().getSimpleName().toLowerCase());
            game.unload();

            for (GameStage stages : GameStage.values()) {
                stages.getPendingRegister().clear();
                stages.getPendingUnregister().clear();
            }

            Game newGame;

            try {
                context.info("§6[Game] §eCarregando modo " + game.getClass().getSimpleName().toLowerCase());
                newGame = game.getClass().getConstructor(HungerGames.class).newInstance(HungerGames.getInstance());
                newGame.setType(gameType);
                HungerGames.getInstance().setGame(newGame);
                newGame.load();
            } catch (Exception e) {
                context.sendMessage("§4§lFATAL " + context.getAccount().getLanguage().translate("unexpected_error"));
                Bukkit.shutdown();
                e.printStackTrace();
                return;
            }

            reload(newGame);
            return;
        }

        if (!classGame.isAnnotationPresent(Loadable.class)) {
            context.sendMessage("§cEsse modo não pode ser carregado.");
            return;
        }

        Game currentGame = getGame();

        if (currentGame.getClass() == classGame) {
            context.sendMessage("§cO servidor já está rodando este modo.");
            return;
        }

        context.info("§6[Game] §eDescarregando modo " + currentGame.getClass().getSimpleName().toLowerCase());
        currentGame.unload();

        Game newGame = null;

        for (GameStage stages : GameStage.values()) {
            stages.getPendingRegister().clear();
            stages.getPendingUnregister().clear();
        }

        try {
            context.info("§6[Game] §eCarregando modo " + name.toLowerCase());
            newGame = (Game) classGame.getConstructor(HungerGames.class).newInstance(HungerGames.getInstance());
            newGame.setType(currentGame.getType());
            HungerGames.getInstance().setGame(newGame);
            newGame.load();
        } catch (Exception e) {
            context.sendMessage("§4§lFATAL " + context.getAccount().getLanguage().translate("unexpected_error"));
            Bukkit.shutdown();
            e.printStackTrace();
        }
        reload(newGame);
    }

    public void reload(final Game game) {
        getPlugin().getKitEmployer().run(true, game);
        getPlugin().getUserStorage().getUsers().forEach(c -> {
            PlayerInitialSpawnEvent spawnEvent = new PlayerInitialSpawnEvent(c.getPlayer(), c.getPlayer().getLocation());
            Bukkit.getPluginManager().callEvent(spawnEvent);
            ((CraftPlayer) c.getPlayer()).getHandle().playerConnection.teleport(spawnEvent.getSpawnLocation());
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(c.getPlayer(), null));
            c.getPlayer().sendMessage("§eModo alterado para §b" + game.getName() + "§e!");
        });
    }

    @Completer(name = "lm")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            String arg0 = context.getArg(0);
            return GameStorage.getGames().stream().filter(c -> startsWith(c.getSimpleName(), arg0)).map(c -> c.getSimpleName().toLowerCase()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
