/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.selector.Type;
import com.minecraft.hungergames.util.selector.object.Chooser;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitEmployer implements Assistance, BukkitInterface {

    private final List<String> registeredCommands = new ArrayList<>();

    public void run(final boolean unregister, final Game game) {

        final BukkitFrame commandFramework = BukkitGame.getEngine().getBukkitFrame();

        if (unregister) {
            for (String label : registeredCommands) {
                commandFramework.unregisterCommand(label);
                commandFramework.unregisterCommands(label);
            }
            registeredCommands.clear();
        }

        for (int i = 0; i < game.getType().getMaxKits(); i++) {

            final int slot = i;

            String label = "kit";

            if (slot > 0)
                label = label + (slot + 1);

            if (commandFramework.getCommandMap().containsKey(label)) {
                System.out.println(label + " already exists, skipping");
                continue;
            }

            final String finalLabel = label;

            commandFramework.registerCommand(CommandInfo.builder().holder(getClass()).name(label).platform(Platform.PLAYER).build(), context -> {
                execute(context, slot, finalLabel);
                return true;
            });

            commandFramework.registerCompleter(label, context -> complete(context, slot));
            registeredCommands.add(label);
        }
    }

    public void execute(Context<CommandSender> context, int slot, String label) {

        Player player = (Player) context.getSender();
        User user = User.fetch(player.getUniqueId());
        String[] args = context.getArgs();

        boolean checkMultiKits = user.getKits().length > 1;

        if (args.length == 0) {
            if (isLateLimit()) {
                new Chooser(user, Chooser.Sort.MINE_ALL, slot, label).build().open();
            } else {
                context.info("hg.game.already_started");
            }
            return;
        }

        if (!user.isAlive()) {
            context.info("hg.game.not_alive");
            return;
        }

        if (!isLateLimit()) {
            context.info("hg.game.already_started");
            return;
        }

        if (hasStarted() && !user.getAccount().hasPermission(Rank.VIP)) {
            context.info("hg.game.already_started");
            return;
        }

        Kit currentKit = user.getKit(slot);
        Kit kit = getKit(args[0]);

        if (kit == null) {
            context.info("object.not_found", "Kit");
            return;
        }

        if (checkMultiKits) {
            for (int i = 0; i < user.getKits().length; i++) {
                Kit check = user.getKit(i);

                if (i != slot) {
                    if (getPlugin().getKitStorage().isBlocked(kit.getClass(), check.getClass())) {
                        context.info("kit.not_compatible_with", kit.getDisplayName(), check.getDisplayName());
                        return;
                    }
                }
            }
        }

        if (hasStarted() && !currentKit.isNone()) {
            context.info("hg.game.already_started");
            return;
        }

        if (currentKit == kit || kit.isUser(player) && !kit.isMultipleChoices()) {
            context.info("kit.already_selected");
            return;
        }

        if (!kit.isActive()) {
            context.info("kit.not_active");
            return;
        }


        if (!kit.isNone() && !user.hasKit(kit, slot)) {
            context.info("kit.do_not_have", Constants.SERVER_STORE);
            return;
        }

        user.setKit(slot, kit);
        context.info("kit.select", kit.getDisplayName());
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 2F, 2F);

        if (hasStarted())
            kit.grant(player);

        getGame().handleSidebar(user);
    }

    public List<String> complete(Context<CommandSender> context, int slot) {
        return getPlugin().getKitStorage().getKits().stream().filter(kit -> startsWith(kit.getName(), context.getArg(context.argsCount() - 1)) && kit.isActive() && getUser(context.getUniqueId()).hasKit(kit, slot)).map(kit -> kit.getName().toLowerCase()).collect(Collectors.toList());
    }
}
