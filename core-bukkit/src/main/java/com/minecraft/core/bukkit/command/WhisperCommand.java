/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class WhisperCommand implements BukkitInterface {

    @Command(name = "tell", aliases = {"whisper", "mp", "w", "msg"}, usage = "{label} <target> <msg>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context, Player target, String[] message) {
        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        if (sender == target) {
            context.info("command.tell.whisper_yourself");
            return;
        }

        Account accountSender = context.getAccount();

        if (!accountSender.getPreference(Preference.TELL) && !accountSender.hasPermission(Rank.TRIAL_MODERATOR)) {
            context.info("command.tell.unable_send_message");
            return;
        }

        if (checkPunish(context))
            return;

        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(context.getUniqueId(), "chat.cooldown");

        if (!accountSender.hasPermission(Rank.PARTNER_PLUS)) {
            if (cooldown != null && !cooldown.expired()) {
                context.info("wait_to_chat", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining()));
                return;
            } else {
                CooldownProvider.getGenericInstance().addCooldown(context.getUniqueId(), "chat.cooldown", 3, false);
            }
        }

        Account accountTarget = Account.fetch(target.getUniqueId());

        if (!accountTarget.getPreference(Preference.TELL) && !accountSender.hasPermission(Rank.TRIAL_MODERATOR)) {
            context.info("command.tell.cant_send_message");
            target.sendMessage("§8" + sender.getName() + " está tentando te enviar uma mensagem privada.");
            return;
        }

        final String msg = String.join(" ", message);

        if (accountSender.getRank().getId() < Rank.ADMINISTRATOR.getId())
            BukkitGame.getEngine().getWordCensor().filter(msg);

        accountSender.setProperty("whisper_linked_player", target);
        accountTarget.setProperty("whisper_linked_player", sender);

        target.sendMessage("§8[§7" + sender.getName() + " §f» §7Você§8] §e" + msg);
        sender.sendMessage("§8[§7Você §f» §7" + target.getName() + "§8] §e" + msg);
    }

    @Command(name = "r", aliases = {"reply"}, usage = "{label} <msg>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context, String[] message) {
        Player sender = context.getSender();

        Account accountSender = context.getAccount();

        if (!accountSender.hasProperty("whisper_linked_player")) {
            context.info("command.reply.no_target");
            return;
        }

        Player target = accountSender.getProperty("whisper_linked_player").getAs(Player.class);
        Account accountTarget = Account.fetch(target.getUniqueId());

        if (accountTarget == null || !target.isOnline()) {
            context.info("target.not_found");
            accountSender.removeProperty("whisper_linked_player");
            return;
        }

        if (checkPunish(context))
            return;

        if (!accountTarget.getPreference(Preference.TELL) && !accountSender.hasPermission(Rank.TRIAL_MODERATOR)) {
            context.info("command.tell.cant_send_message");
            return;
        }

        final String msg = String.join(" ", message);

        if (accountSender.getRank().getId() < Rank.ADMINISTRATOR.getId())
            BukkitGame.getEngine().getWordCensor().filter(msg);

        accountSender.setProperty("whisper_linked_player", target);
        accountTarget.setProperty("whisper_linked_player", sender);

        target.sendMessage("§8[§7" + sender.getName() + " §f» §7Você§8] §e" + msg);
        sender.sendMessage("§8[§7Você §f» §7" + target.getName() + "§8] §e" + msg);
    }

    @Completer(name = "tell")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

    public boolean checkPunish(Context<Player> context) {

        Account account = context.getAccount();

        if (account.isPunished(PunishType.MUTE, PunishCategory.COMMUNITY)) {
            Punish punish = account.getPunish(PunishType.MUTE, PunishCategory.COMMUNITY);

            context.sendMessage("§c§m                                                            ");
            if (account.getLanguage() == Language.PORTUGUESE) {
                context.sendMessage("§cVocê foi" + (punish.isPermanent() ? " permanentemente" : " temporariamente") + " silenciado por " + punish.getReason());
                if (!punish.isPermanent())
                    context.sendMessage("§7Seu silenciamento expirará em§c " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED));
                context.sendMessage("");
                context.sendMessage("§7Saiba mais em §e" + Constants.SERVER_WEBSITE);
            } else {
                context.sendMessage("§cYou've been" + (punish.isPermanent() ? " permanently" : " temporarily") + " muted for " + punish.getReason());
                if (!punish.isPermanent())
                    context.sendMessage("§7Your mute will expire in§c " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED));
                context.sendMessage("");
                context.sendMessage("§7Find out more on §e" + Constants.SERVER_WEBSITE);
            }
            context.sendMessage("§7ID: §f#" + punish.getCode());
            context.sendMessage("§c§m                                                            ");
            return true;
        }
        return false;
    }
}