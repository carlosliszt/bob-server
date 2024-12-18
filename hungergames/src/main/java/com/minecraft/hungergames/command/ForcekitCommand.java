/*
 * Copyright (C) BobMC, All Rights Reserved
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
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ForcekitCommand implements Assistance, BukkitInterface {

    @Command(name = "forcekit", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS, usage = "{label} <target...> <kit>")
    public void killCommand(Context<Player> context, String rawUsers, String rawKit) {

        Kit kit = getPlugin().getKitStorage().getKit(rawKit);

        if (kit == null) {
            context.info("object.not_found", "Kit");
            return;
        }

        if (!kit.isActive()) {
            context.info("kit.not_active");
            return;
        }

        int slot = 0;
        int supportedKits = HungerGames.getInstance().getGame().getType().getMaxKits();

        if (context.argsCount() == 3 && isInteger(context.getArg(2))) {

            int provided = Integer.parseInt(context.getArg(2));

            if (provided < 0 || provided >= supportedKits) {
                context.sendMessage("§cSlot fora do limite.");
                return;
            }

            slot = provided;

        } else if (context.argsCount() == 3 && context.getArg(2).equalsIgnoreCase("*")) {

            if (supportedKits > 1 && !kit.isMultipleChoices()) {
                context.sendMessage("§cO kit " + kit.getDisplayName() + " não suporta múltipla escolhas.");
                return;
            }

            slot = -1;
        }

        AtomicInteger affected = new AtomicInteger();

        int finalSlot = slot;

        getUsers(",", rawUsers).forEach(user -> {

            if (finalSlot == -1) {
                for (int i = 0; i < user.getKits().length; i++) {
                    user.getKit(i).removeItems(user.getPlayer());
                    user.setKit(i, kit);

                    if (hasStarted())
                        kit.grant(user.getPlayer());
                }
                affected.getAndIncrement();
                getGame().handleSidebar(user);
                return;
            }

            user.getKit(finalSlot).removeItems(user.getPlayer());
            user.setKit(finalSlot, kit);

            if (hasStarted())
                kit.grant(user.getPlayer());

            affected.getAndIncrement();
            getGame().handleSidebar(user);
        });

        context.info("command.forcekit.affected_users", kit.getDisplayName(), affected.get(), (slot == -1 ? "*" : slot));
    }

    @Completer(name = "forcekit")
    public List<String> handleComplete(Context<CommandSender> context) {

        if (context.argsCount() == 1) {
            List<String> response = new ArrayList<>();

            if (startsWith("all", context.getArg(0)))
                response.add("all");

            response.addAll(getOnlineNicknames(context));
            return response;
        } else if (context.argsCount() == 2)
            return getPlugin().getKitStorage().getKits().stream().filter(kit -> kit.isActive() && startsWith(kit.getName(), context.getArg(1))).map(kit -> kit.getName().toLowerCase()).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
