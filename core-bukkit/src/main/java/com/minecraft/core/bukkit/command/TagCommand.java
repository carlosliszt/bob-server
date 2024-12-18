/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TagCommand implements BukkitInterface {

    @Command(name = "tag", usage = "{label} <tag>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        String[] args = context.getArgs();
        Account account = context.getAccount();

        account.getTagList().loadTags();

        if (args.length == 0) {
            int max = account.getTagList().getTags().size() * 2;

            PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

            TextComponent[] textComponents = new TextComponent[max];
            textComponents[0] = new TextComponent("§aSuas tags: ");

            int i = max - 1;

            for (Tag tag : account.getTagList().getTags()) {
                if (i < max - 1) {
                    textComponents[i] = new TextComponent("§f, ");
                    i -= 1;
                }

                String hoverDisplay = "§fExemplo: " + (tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag).replace("#", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+")) + account.getDisplayName() + "\n\n§eClique para selecionar!";

                TextComponent component = createTextComponent(tag.getColor() + tag.getName().replace("+3", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()).replace("+2", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()).replace("+1", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()).replace("+", tag == Tag.PARTNER_PLUS ? "+" : account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()), HoverEvent.Action.SHOW_TEXT, hoverDisplay, ClickEvent.Action.RUN_COMMAND, "/tag " + tag.getName());
                textComponents[i] = component;
                i -= 1;
            }

            context.getSender().sendMessage(textComponents);
        } else {
            Tag tag = Tag.fromUsages(args[0]);

            if (account.getFlag(Flag.TAG)) {
                context.info("flag.locked");
                return;
            }

            if (tag == null) {
                context.info("command.tag.generic_error");
                return;
            }

            if (!account.getTagList().hasTag(tag)) {
                context.info("command.tag.generic_error");
                return;
            }

            if (account.getProperty("account_tag").getAs(Tag.class).equals(tag)) {
                context.info("command.tag.tag_already_in_use");
                return;
            }

            account.setProperty("account_tag", tag);
            account.getData(Columns.TAG).setData(tag.getUniqueCode());

            context.info("command.tag.tag_change", tag.getColor() + tag.getName().replace("+3", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()).replace("+2", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()).replace("+1", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()).replace("+", tag == Tag.PARTNER_PLUS ? "+" : account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+" + tag.getColor()));

            PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, tag, account.getProperty("account_prefix_type").getAs(PrefixType.class));
            Bukkit.getPluginManager().callEvent(event);

            async(() -> account.getDataStorage().saveColumn(Columns.TAG));
        }
    }

    @Completer(name = "tag")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return context.getAccount().getTagList().getTags().stream().map(tag -> tag.getName().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

}
