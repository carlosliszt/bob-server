/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VanishCommand implements BukkitInterface {

    Vanish vanish = Vanish.getInstance();

    @Command(name = "vanish", aliases = {"v"}, platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<Player> context) {
        if (context.argsCount() == 0) {
            boolean vanished = vanish.isVanished(context.getUniqueId());
            if (vanished)
                vanish.setVanished(context.getSender(), null, false);
            else
                vanish.setVanished(context.getSender(), context.getAccount().getRank(), false);
            log(context.getAccount(), context.getAccount().getDisplayName() + (!vanished ? " entrou no modo vanish" : " saiu do modo vanish"));
        } else {
            if (context.getArg(0).equalsIgnoreCase("v") || context.getArg(0).equalsIgnoreCase("visible")) {
                context.getSender().performCommand("visible");
            }
        }

    }

    @Command(name = "visible", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommandVisible(Context<Player> context) {
        if (!vanish.visible(context.getUniqueId()))
            vanish.setVanished(context.getSender(), Rank.MEMBER, false);
        else
            vanish.setVanished(context.getSender(), context.getAccount().getRank(), false);
        log(context.getAccount(), context.getAccount().getDisplayName() + (!vanish.visible(context.getUniqueId()) ? " entrou no modo vanish" : " saiu do modo vanish"));
    }

    @Command(name = "vanishlevel", aliases = {"vl"}, platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommandVanishLevel(Context<Player> context, String level) {
        boolean vanished = vanish.isVanished(context.getUniqueId());

        Rank.Category category;
        try {
            category = Rank.Category.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            context.sendMessage("§cCategoria inválida, tente: " + Arrays.stream(Rank.Category.values()).filter(r -> r.getImportance() <= context.getAccount().getRank().getCategory().getImportance()).map(Rank.Category::name).collect(Collectors.joining(", ")) + ".");
            return;
        }

        Rank rank = Rank.getRanksByCategory(category).stream().filter(r -> r.getCategory().getImportance() == category.getImportance()).collect(Collectors.toList()).get(0);
        if (rank.getCategory().getImportance() > context.getAccount().getRank().getCategory().getImportance()) {
            context.sendMessage("§cVocê não pode se esconder de jogadores com rank superior ao seu.");
            return;
        }

        if (vanished) {
            boolean vanishedToRank = vanish.isVanishedToRankExact(context.getUniqueId(), rank);
            if (!vanishedToRank) {
                vanish.setVanished(context.getSender(), rank, false);
                log(context.getAccount(), context.getAccount().getDisplayName() + " entrou no modo vanish para " + rank.getCategory().getDisplay() + ".");
            } else {
                context.sendMessage("§cVocê já está no modo vanish para " + rank.getCategory().getDisplay() + ".");
            }
        } else {
            vanish.setVanished(context.getSender(), rank, false);
            log(context.getAccount(), context.getAccount().getDisplayName() + " entrou no modo vanish para " + rank.getCategory().getDisplay() + ".");
        }
    }

    @Completer(name = "vanish")
    public List<String> handleComplete(Context<Player> context) {
        ArrayList<String> list = new ArrayList<>();
        if (context.argsCount() == 1) {
            list.add("v");
            list.add("visible");
            return list;
        }
        return Collections.emptyList();
    }

    @Completer(name = "vanishlevel")
    public List<String> handleVanishLevelComplete(Context<Player> context) {
        ArrayList<String> list = new ArrayList<>();
        for (Rank.Category cat : Rank.Category.values()) {
            if (context.getAccount().getRank().getCategory().getImportance() >= cat.getImportance()) {
                list.add(cat.name().toLowerCase());
            }
        }
        return list;
    }

}