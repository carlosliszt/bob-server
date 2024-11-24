/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.command;

import com.minecraft.arcade.pvp.PvP;
import com.minecraft.arcade.pvp.game.list.Arena;
import com.minecraft.arcade.pvp.kit.Kit;
import com.minecraft.arcade.pvp.kit.KitStorage;
import com.minecraft.arcade.pvp.user.User;
import com.minecraft.arcade.pvp.util.Assistance;
import com.minecraft.arcade.pvp.util.selector.Chooser;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.command.command.CommandInfo;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements Assistance, BukkitInterface {

    private final List<String> registeredCommands = new ArrayList<>();
    private final KitStorage kitStorage = PvP.getInstance().getKitStorage();

    public void run(final boolean unregister) {

        final BukkitFrame commandFramework = BukkitGame.getEngine().getBukkitFrame();
        if (unregister) {
            for (String label : registeredCommands) {
                commandFramework.unregisterCommand(label);
                commandFramework.unregisterCommands(label);
            }
            registeredCommands.clear();
        }

        for (int i = 0; i < kitStorage.getGameType().getMaxKits(); i++) {

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

        if (user.getGame().getId() != 1) {
            player.sendMessage("§cComando indisponível.");
            return;
        }

        String[] args = context.getArgs();

        boolean checkMultiKits = user.getKits().length > 1;

        if (args.length == 0) {
            new Chooser(user, Chooser.Sort.MINE_ALL, slot, label).build().open();
            return;
        }

        Arena game = (Arena) user.getGame();

        if (!game.getProtectedUuidSet().contains(player.getUniqueId())) {
            context.info("pvp.command.kit.not_allowed_to_select");
            return;
        }

        Kit currentKit = user.getKit(slot);
        Kit kit = kitStorage.getKit(args[0]);

        if (kit == null) {
            context.info("object.not_found", "Kit");
            return;
        }

        if (checkMultiKits) {
            for (int i = 0; i < user.getKits().length; i++) {
                Kit check = user.getKit(i);

                if (i != slot) {
                    if (kitStorage.isBlocked(kit.getClass(), check.getClass())) {
                        context.info("kit.not_compatible_with", kit.getDisplayName(), check.getDisplayName());
                        return;
                    }
                }
            }
        }

        if (currentKit == kit || kit.isUser(user) && !kit.isNone()) {
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

        context.info("kit.select", kit.getDisplayName());
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 2F, 2F);

        user.setKit(slot, kit);

        game.handleSidebar(user);
    }

    public List<String> complete(Context<CommandSender> context, int slot) {
        return kitStorage.getKits().stream().filter(kit -> startsWith(kit.getName(), context.getArg(context.argsCount() - 1)) && kit.isActive() && getUser(context.getUniqueId()).hasKit(kit, slot)).map(kit -> kit.getName().toLowerCase()).collect(Collectors.toList());
    }
}