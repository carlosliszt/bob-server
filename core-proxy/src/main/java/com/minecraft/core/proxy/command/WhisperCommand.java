/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.List;

public class WhisperCommand implements ProxyInterface {

    @Command(name = "tell", aliases = {"whisper", "mp", "w", "msg"}, usage = "{label} <target> <msg>", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context, ProxiedPlayer target, String[] message) {
        ProxiedPlayer sender = context.getSender();

        if (target == null) {
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

        //TODO: change that, temporary fix until i can be arsed to make an actual fix
        /*
        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(context.getUniqueId(), "chat.cooldown");

        if (!accountSender.hasPermission(Rank.PARTNER_PLUS)) {
            if (cooldown != null && !cooldown.expired()) {
                context.info("wait_to_chat", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining()));
                return;
            } else {
                CooldownProvider.getGenericInstance().addCooldown(context.getUniqueId(), "chat.cooldown", 3, false);
            }
        } */

        Account accountTarget = Account.fetch(target.getUniqueId());

        if (!accountTarget.getPreference(Preference.TELL) && !accountSender.hasPermission(Rank.TRIAL_MODERATOR)) {
            context.info("command.tell.cant_send_message");
            target.sendMessage("§8" + sender.getName() + " está tentando te enviar uma mensagem privada.");
            return;
        }

        if(accountTarget.getData(Columns.FRIEND_STATUS).getAsString().contains("VANISH")) {
            context.info("target.not_found");
        }

        if(accountTarget.isBlocked(accountSender.getUniqueId())) {
            context.info("command.tell.cant_send_message");
            return;
        }

        final String msg = String.join(" ", message);

        if (accountSender.getRank().getId() < Rank.ADMINISTRATOR.getId())
            ProxyGame.getInstance().getWordCensor().filter(msg);

        accountSender.setProperty("whisper_linked_player", target);
        accountTarget.setProperty("whisper_linked_player", sender);

        target.sendMessage("§8[§7" + accountSender.getProperty("account_tag").getAs(Tag.class).getFormattedColor() + sender.getName() + " §f» " + accountTarget.getProperty("account_tag").getAs(Tag.class).getFormattedColor()+  target.getName() + "§8] §e" + msg);
        sender.sendMessage("§8[" + accountSender.getProperty("account_tag").getAs(Tag.class).getFormattedColor() + sender.getName() + " §f» §7" + accountTarget.getProperty("account_tag").getAs(Tag.class).getFormattedColor()+  target.getName() + "§8] §e" + msg);
    }

    @Command(name = "r", aliases = {"reply"}, usage = "{label} <msg>", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context, String[] message) {
        ProxiedPlayer sender = context.getSender();

        Account accountSender = context.getAccount();

        if (!accountSender.hasProperty("whisper_linked_player")) {
            context.info("command.reply.no_target");
            return;
        }

        ProxiedPlayer target = accountSender.getProperty("whisper_linked_player").getAs(ProxiedPlayer.class);
        Account accountTarget = Account.fetch(target.getUniqueId());

        if (accountTarget == null || target != null) {
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
            ProxyGame.getInstance().getWordCensor().filter(msg);

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

    public boolean checkPunish(Context<ProxiedPlayer> context) {

        Account account = context.getAccount();

        if (account.isPunished(PunishType.MUTE, PunishCategory.COMMUNITY)) {
            Punish punish = account.getPunish(PunishType.MUTE, PunishCategory.COMMUNITY);

            if (account.getLanguage() == Language.PORTUGUESE) {
                context.sendMessage("§cA sua conta está mutada por " + punish.getReason() + (punish.isPermanent() ? "." : " expira em " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED) + "." + (punish.isInexcusable() ? " §c§l(NÃO PODE COMPRAR UNMUTE)" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " §c§l(NÃO PODE COMPRAR UNMUTE)" : ""))));
            } else {
                context.sendMessage("§cYour account is muted for" + punish.getReason() + (punish.isPermanent() ? "." : " expires in " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED) + "." + (punish.isInexcusable() ? " §c§l(CAN'T BUY UNMUTE)" : (account.count(punish.getType(), PunishCategory.COMMUNITY) >= 5 ? " §c§l(CAN'T BUY UNMUTE)" : ""))));
            }
            return true;
        }
        return false;
    }

}
