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
        boolean vanished = vanish.isVanished(context.getUniqueId());
        if (context.getArgs().length == 0) {
            if (vanished)
                vanish.setVanished(context.getSender(), null, false);
            else
                vanish.setVanished(context.getSender(), context.getAccount().getRank(), false);
            log(context.getAccount(), context.getAccount().getDisplayName() + (!vanished ? " entrou no modo vanish" : " saiu do modo vanish"));
        } else {
            String[] args = context.getArgs();

            if (args[0].equalsIgnoreCase("v") || args[0].equalsIgnoreCase("visible")) {

                if (vanished) {
                    if (vanish.isVanishedToCategory(context.getUniqueId(), Rank.Category.NONE)) {
                        vanish.setVanished(context.getSender(), context.getAccount().getRank(), false);
                        log(context.getAccount(), context.getAccount().getDisplayName() + " não está mais visível.");
                    } else {
                        vanish.setVanished(context.getSender(), Rank.MEMBER, false);
                        log(context.getAccount(), context.getAccount().getDisplayName() + " agora está visível.");
                    }
                } else {
                    vanish.setVanished(context.getSender(), context.getAccount().getRank(), false);
                }

            } else {

                Rank.Category category;
                try {
                    category = Rank.Category.valueOf(args[0].toUpperCase());
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
        }


    }

    @Completer(name = "vanish")
    public List<String> handleComplete(Context<Player> context) {
        ArrayList<String> list = new ArrayList<>();
        if (context.argsCount() == 1) {
            list.add("v");
            list.add("visible");
            for (Rank.Category cat : Rank.Category.values()) {
                if (context.getAccount().getRank().getCategory().getImportance() >= cat.getImportance()) {
                    list.add(cat.name());
                }
            }

            return list;
        }
        return Collections.emptyList();
    }

}