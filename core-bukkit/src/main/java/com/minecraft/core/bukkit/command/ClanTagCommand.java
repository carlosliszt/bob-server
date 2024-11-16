/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Clantag;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClanTagCommand implements BukkitInterface {

    @Command(name = "clantag", usage = "{label} <clantag>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        String[] args = context.getArgs();
        Account account = context.getAccount();

        Clan clan = Constants.getClanService().fetch(account.getData(Columns.CLAN).getAsInt());

        if (clan == null || !account.hasClan()) {
            context.sendMessage("§cVocê precisa fazer parte de um clan para alterar sua clan tag.");
            return;
        }

        account.getClanTagList().loadClanTags();

        if (args.length == 0) {
            int max = account.getClanTagList().getClanTags().size() * 2;

            TextComponent[] textComponents = new TextComponent[max];
            textComponents[0] = new TextComponent("§aSuas Clan Tags: ");

            int i = max - 1;

            final Tag tag = account.getProperty("account_tag").getAs(Tag.class);
            final PrefixType prefixType = account.getProperty("account_prefix_type").getAs(PrefixType.class);

            for (Clantag clantag : account.getClanTagList().getClanTags()) {
                if (i < max - 1) {
                    textComponents[i] = new TextComponent("§f, ");
                    i -= 1;
                }

                String hoverDisplay = "§fExemplo: " + (tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag)) + account.getDisplayName() + " " + (clantag == Clantag.DEFAULT ? ChatColor.valueOf(clan.getColor()) : clantag.getColor()) + "[" + clan.getTag().toUpperCase() + "]" + "\n\n§eClique para selecionar!";

                TextComponent component = createTextComponent((clantag == Clantag.DEFAULT ? ChatColor.valueOf(clan.getColor()) : clantag.getColor()) + clantag.getName(), HoverEvent.Action.SHOW_TEXT, hoverDisplay, ClickEvent.Action.RUN_COMMAND, "/clantag " + clantag.getName());
                textComponents[i] = component;
                i -= 1;
            }

            context.getSender().sendMessage(textComponents);
        } else {
            Clantag clantag = Clantag.fromName(args[0]);

            if (clantag == null) {
                context.info("command.clantag.generic_error");
                return;
            }

            if (!account.getClanTagList().hasTag(clantag)) {
                context.info("command.clantag.generic_error");
                return;
            }

            if (account.getProperty("account_clan_tag").getAs(Clantag.class).equals(clantag)) {
                context.info("command.clantag.clantag_already_in_use");
                return;
            }

            account.setProperty("account_clan_tag", clantag);
            account.getData(Columns.CLANTAG).setData(clantag.getUniqueCode());

            context.info("command.clantag.clantag_change", clantag.getColor() + clantag.getName());

            PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class));
            Bukkit.getPluginManager().callEvent(event);

            async(() -> account.getDataStorage().saveColumn(Columns.CLANTAG));
        }
    }

    @Completer(name = "clantag")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return context.getAccount().getClanTagList().getClanTags().stream().map(clantag -> clantag.getName().toLowerCase()).filter(s -> startsWith(s, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

}
