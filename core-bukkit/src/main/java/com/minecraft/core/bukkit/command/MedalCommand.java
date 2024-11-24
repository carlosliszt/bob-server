/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Medal;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MedalCommand implements BukkitInterface {

    @Command(name = "medal", usage = "{label} <medal>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        String[] args = context.getArgs();
        Account account = context.getAccount();

        account.getMedalList().loadMedals();

        if (args.length == 0) {
            int max = account.getMedalList().getMedals().size() * 2;

            TextComponent[] textComponents = new TextComponent[max];
            textComponents[0] = new TextComponent("§aSuas medalhas: ");

            int i = max - 1;

            for (Medal medal : account.getMedalList().getMedals()) {
                if (i < max - 1) {
                    textComponents[i] = new TextComponent("§f, ");
                    i -= 1;
                }

                String hoverDisplay = medal.getColor() + medal.getDisplayName() + "\n\n§eClique para selecionar!";

                TextComponent component = createTextComponent(medal.getColor() + (medal == Medal.NONE ? medal.getName() : medal.getIcon()), HoverEvent.Action.SHOW_TEXT, hoverDisplay, ClickEvent.Action.RUN_COMMAND, "/medal " + medal.getName());
                textComponents[i] = component;
                i -= 1;
            }

            context.getSender().sendMessage(textComponents);
        } else {
            Medal medal = Medal.fromString(args[0]);

            if (account.getFlag(Flag.MEDAL)) {
                context.info("flag.locked");
                return;
            }

            if (medal == null) {
                context.info("command.medal.generic_error");
                return;
            }

            if (!account.getMedalList().hasMedal(medal)) {
                context.info("command.medal.generic_error");
                return;
            }

            if (account.getProperty("account_medal").getAs(Medal.class).equals(medal)) {
                context.info("command.medal.medal_already_in_use");
                return;
            }

            account.setProperty("account_medal", medal);
            account.getData(Columns.MEDAL).setData(medal.getUniqueCode());

            context.info("command.medal.medal_change", medal.getColor() + medal.getName());

            async(() -> account.getDataStorage().saveColumn(Columns.MEDAL));
        }
    }

    @Completer(name = "medal")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return context.getAccount().getMedalList().getMedals().stream().map(medal -> medal.getName().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

}
