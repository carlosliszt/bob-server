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
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PrefixtypeCommand implements BukkitInterface {

    @Command(name = "prefixtype", usage = "{label} <prefixtype>", platform = Platform.PLAYER, rank = Rank.ELITE)
    public void handleCommand(Context<Player> context, String type) {

        Account account = context.getAccount();

        if (!account.hasPermission(Rank.STREAMER_PLUS) && account.getRank().getId() != Rank.ELITE.getId()) {
            context.info("game.spectatorlist.no_permission", Rank.ELITE.getName(), Constants.SERVER_WEBSITE);
            return;
        }

        PrefixType prefixType = PrefixType.fromString(type);

        if (prefixType == null) {
            context.info("object.not_found", "Type");
            return;
        }

        if (!account.hasPermission(prefixType.getRank())) {
            context.info("command.prefixtype.no_permission");
            return;
        }

        if (account.getProperty("account_prefix_type").getAs(PrefixType.class).equals(prefixType)) {
            context.info("command.prefixtype.already_in_use");
            return;
        }

        account.setProperty("account_prefix_type", prefixType);
        account.getData(Columns.PREFIXTYPE).setData(prefixType.getUniqueCode());

        context.info("command.prefixtype.type_change", prefixType.name().toLowerCase());

        PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), prefixType);
        Bukkit.getPluginManager().callEvent(event);

        async(() -> account.getDataStorage().saveColumn(Columns.PREFIXTYPE));
    }

    protected final List<PrefixType> prefixTypeList = Arrays.asList(PrefixType.values());

    @Completer(name = "prefixtype")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return prefixTypeList.stream().filter(a -> context.getAccount().hasPermission(a.getRank())).map(pt -> pt.name().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

}