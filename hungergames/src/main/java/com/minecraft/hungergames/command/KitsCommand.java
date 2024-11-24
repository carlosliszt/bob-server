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
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class KitsCommand implements Assistance, BukkitInterface {

    @Command(name = "kits", rank = Rank.TRIAL_MODERATOR, platform = Platform.BOTH, usage = "{label} <enable/disable> <kits...>")
    public void handleCommand(Context<CommandSender> context, String action, String kitRaw) {

        if (action.equalsIgnoreCase("enable")) {

            Collection<Kit> kits = getKits(',', kitRaw);
            kits.removeIf(c -> c.isActive() || c.isNone());

            int i = 0;

            for (Kit kit : kits) {
                i++;
                kit.setActive(true, true);
            }

            context.info("command.kits.enable_toggle", i);
        } else if (action.equalsIgnoreCase("disable")) {

            Collection<Kit> kits = getKits(',', kitRaw);
            kits.removeIf(kit -> !kit.isActive() || kit.isNone());

            int i = 0;

            for (Kit kit : kits) {
                i++;
                kit.setActive(false, true);
            }

            context.info("command.kits.disable_toggle", i);

        }
    }

    @Completer(name = "kits")
    public List<String> tabComplete(Context<Player> context) {
        String[] args = context.getArgs();

        List<String> list;
        if (args.length == 1) {
            list = Arrays.asList("disable", "enable");
            list.removeIf(c -> !startsWith(c, args[0]));
        } else {

            list = new ArrayList<>();

            if (startsWith("all", args[context.argsCount() - 1]))
                list.add("all");

            if (args[0].equalsIgnoreCase("enable")) {
                getPlugin().getKitStorage().getKits().stream().filter(kit -> !kit.isNone() && !kit.isActive() && startsWith(kit.getName(), args[context.argsCount() - 1])).map(kit -> kit.getName().toLowerCase()).forEach(list::add);
            } else if (args[0].equalsIgnoreCase("disable")) {
                getPlugin().getKitStorage().getKits().stream().filter(kit -> !kit.isNone() && kit.isActive() && startsWith(kit.getName(), args[context.argsCount() - 1])).map(kit -> kit.getName().toLowerCase()).forEach(list::add);
            }
        }
        return list;
    }
}
