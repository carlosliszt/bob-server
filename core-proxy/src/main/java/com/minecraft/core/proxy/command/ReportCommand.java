/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountStorage;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.account.fields.Property;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReportCommand implements ProxyInterface {

    @Command(name = "report", aliases = {"rp", "denunciar", "reportar"}, usage = "{label} <target> <reason>", platform = Platform.PLAYER)
    public void handleCommand(Context<ProxiedPlayer> context, String arg) {

        Account account = context.getAccount();

        Argument argument = Argument.get(arg);

        if (!account.hasPermission(argument.getRank())) {
            context.info(MessageType.NO_PERMISSION.getMessageKey());
            return;
        }

        argument.getExecutor().execute(context);
    }

    @Completer(name = "report")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            Account account = context.getAccount();
            List<String> response = Arrays.stream(Argument.values()).filter(argument -> argument.getKey() != null && startsWith(argument.getKey(), context.getArg(0)) && account.hasPermission(argument.getRank())).map(Argument::getKey).collect(Collectors.toList());
            response.addAll(getOnlineNicknames(context));
            return response;
        }
        return Collections.emptyList();
    }

    @AllArgsConstructor
    @Getter
    public enum Argument {

        DEFAULT(null, Rank.MEMBER, context -> {

            String[] args = context.getArgs();
            String stringTarget = args[0];

            if (args.length == 1) {
                context.info(MessageType.INCORRECT_USAGE.getMessageKey(), "/report <target> <reason>");
                return;
            }

            Account accountVictim = context.getAccount();

            ProxiedPlayer proxiedPlayer = context.getSender();

            if (accountVictim.isPunished(PunishType.MUTE, PunishCategory.REPORT)) {

                Punish punish = accountVictim.getPunish(PunishType.MUTE, PunishCategory.REPORT);

                if (accountVictim.getLanguage() == Language.PORTUGUESE) {
                    proxiedPlayer.sendMessage(new TextComponent("§cA sua conta está proibida de reportar por " + punish.getReason() + (punish.isPermanent() ? "" : " expira em " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED)) + "."));
                } else {
                    proxiedPlayer.sendMessage(new TextComponent("§cYour account is banned from reporting for " + punish.getReason() + (punish.isPermanent() ? "" : " expires in " + DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.SIMPLIFIED)) + "."));
                }
                return;
            }

            long cooldown = getCooldown().containsKey(context.getUniqueId()) ? getCooldown().get(context.getUniqueId()) : 0;
            if (cooldown != 0 && cooldown > System.currentTimeMillis()) {
                context.info("wait_generic", (cooldown - System.currentTimeMillis()) / 100); //FIXME: Fix that shit.
                return;
            }

            ProxiedPlayer target = getPlayer(stringTarget);

            if (target == null) {
                context.info("target.not_found");
                return;
            }

            getCooldown().put(context.getUniqueId(), System.currentTimeMillis() + 16000);

            Account accountTarget = Account.fetch(target.getUniqueId());

            if (accountTarget.getRank().getId() >= Rank.TRIAL_MODERATOR.getId() && !accountVictim.hasPermission(accountTarget.getRank())) {
                context.info("target.not_found");
                return;
            }

            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            context.info("command.report.submit.confirmation", accountTarget.getDisplayName(), message);

            Constants.getAccountStorage().getAccounts().stream().filter(account -> account.getUniqueId() != Constants.CONSOLE_UUID && account.hasPermission(Rank.TRIAL_MODERATOR)).forEach(account -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(account.getUniqueId());

                if (player == null)
                    return;

                if (account.getProperty("reports.server_lock", false).getAsBoolean() && target.getServer().getInfo() != player.getServer().getInfo())
                    return;

                if (!account.getPreference(Preference.REPORTS))
                    return;

                TextComponent component = new TextComponent(TextComponent.fromLegacyText(account.getLanguage().translate("command.report.submit.info.click_to_teleport")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + accountTarget.getDisplayName()));

                Language language = account.getLanguage();

                player.sendMessage(TextComponent.fromLegacyText(" "));
                player.sendMessage(TextComponent.fromLegacyText(language.translate("command.report.submit.info.target", target.getDisplayName())));
                player.sendMessage(TextComponent.fromLegacyText(language.translate("command.report.submit.info.reason", message)));
                player.sendMessage(TextComponent.fromLegacyText(language.translate("command.report.submit.info.server", target.getServer().getInfo().getName())));
                player.sendMessage(TextComponent.fromLegacyText(language.translate("command.report.submit.info.reporter", accountVictim.getDisplayName())));
                player.sendMessage(TextComponent.fromLegacyText(" "));

                player.sendMessage(component);


            });

            EmbedBuilder reportEmbed = new EmbedBuilder();
            reportEmbed.setColor(Color.RED);
            reportEmbed.setAuthor("REPORT");
            reportEmbed.setThumbnail("https://mineskin.eu/helm/" + target.getUniqueId() + "/256");
            reportEmbed.setTitle(target.getDisplayName());
            reportEmbed.addField(new MessageEmbed.Field("Motivo", message, false));
            reportEmbed.addField(new MessageEmbed.Field("Servidor", target.getServer().getInfo().getName(), false));
            reportEmbed.addField(new MessageEmbed.Field("Vítima", accountVictim.getDisplayName(), false));

            TextChannel txt = ProxyGame.getInstance().getDiscord().getJDA().getTextChannelById("1310230221278023750");

            if (txt != null)
                txt.sendMessageEmbeds(reportEmbed.build()).queue();

        }),

        TOGGLE("toggle", Rank.TRIAL_MODERATOR, context -> {

            Account account = context.getAccount();

            account.setPreference(Preference.REPORTS, !account.getPreference(Preference.REPORTS));
            account.getData(Columns.PREFERENCES).setData(account.getPreferences());
            Executor.async(() -> account.getDataStorage().saveColumn(Columns.PREFERENCES));

            boolean enabled = account.getPreference(Preference.REPORTS);

            context.info(enabled ? "command.report.toggle.enable" : "command.report.toggle.disable");
        }),

        STATUS("status", Rank.TRIAL_MODERATOR, context -> {

            Account account = context.getAccount();

            boolean enabled = account.getPreference(Preference.REPORTS);
            boolean serverlocked = account.getProperty("reports.server_lock", false).getAsBoolean();

            context.sendMessage("§bAtivado:§6 %s", (enabled ? "Sim" : "Não"));
            context.sendMessage("§bServer lock:§6 %s", (serverlocked ? "Sim" : "Não"));
        }),

        SERVERLOCK("serverlock", Rank.TRIAL_MODERATOR, context -> {

            Account account = context.getAccount();

            boolean enabled;
            Property property = account.getProperty("reports.server_lock", false);
            property.setValue(enabled = !property.getAsBoolean());
            context.info(enabled ? "command.report.server_lock.enable" : "command.report.server_lock.disable");
        });

        private final String key;
        private final Rank rank;
        private final Executor executor;

        @Getter
        private static final HashMap<UUID, Long> cooldown = new HashMap<>();

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(argument -> argument.getKey() != null && argument.getKey().equalsIgnoreCase(key)).findFirst().orElse(DEFAULT);
        }

        private static ProxiedPlayer getPlayer(String name) {
            Account account = AccountStorage.getAccountByName(name, false);

            if (account != null)
                return ProxyServer.getInstance().getPlayer(account.getUniqueId());
            return null;
        }

        protected interface Executor {
            void execute(Context<ProxiedPlayer> context);

            static void async(Runnable runnable) {
                ProxyServer.getInstance().getScheduler().runAsync(ProxyGame.getInstance(), runnable);
            }
        }
    }

}