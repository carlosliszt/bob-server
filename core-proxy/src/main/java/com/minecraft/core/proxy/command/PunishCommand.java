/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.event.PunishAssignEvent;
import com.minecraft.core.proxy.staff.Shortcut;
import com.minecraft.core.proxy.staff.ShortcutRepository;
import com.minecraft.core.proxy.staff.Staffer;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.core.util.geodata.DataResolver;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PunishCommand implements ProxyInterface {

    @Command(name = "p", aliases = {"punish"}, rank = Rank.HELPER, usage = "punish <type> <category> <time> <target> <reason>")
    public void punishCommand(Context<CommandSender> context, PunishType type, PunishCategory category, String stringTime, String target) {

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        if (type == null) {
            context.info("object.not_found", "Type");
            return;
        }

        if (type != PunishType.MUTE && context.getAccount().getRank().getId() <= Rank.HELPER.getId()) {
            context.info(MessageType.NO_PERMISSION.getMessageKey());
            return;
        }

        if (category == null) {
            context.info("object.not_found", "Category");
            return;
        }

        if (context.isPlayer() && !context.getAccount().hasPermission(category.getRank())) {
            context.info("object.not_found", "Category");
            return;
        }

        if (!category.isApplicable(type)) {
            context.info("command.punish.incompatible_punish", category.getName().toLowerCase(), type.name().toLowerCase());
            return;
        }

        if (context.argsCount() < 5 && !context.getAccount().getProperty("isAdmin").getAsBoolean()) {
            context.info(MessageType.INCORRECT_USAGE.getMessageKey(), "/punish <type> <category> <time> <target> [reason]");
            return;
        }

        String reason = createArgs(4, context.getArgs(), "...", false);
        AtomicLong time = new AtomicLong(-1);

        try {
            time.set(StringTimeUtils.parseDateDiff(stringTime, true));
        } catch (Exception ex) {
            context.info("invalid_time", "y,m,d,min,s");
            return;
        }

        async(() -> search(context, target, account -> {

            account.loadPunishments();

            if (type != PunishType.MUTE && account.isPunished(type) || type == PunishType.MUTE && account.isPunished(type, category)) {
                context.info("command.punish.already_punished", type.name().toLowerCase());
                return;
            }

            account.loadRanks();

            if (context.getAccount().getRank().getId() <= account.getRank().getId()) {
                context.info("command.punish.cant_ban_same_rank");
                return;
            }

            context.info("command.punish.processing");

            account.getDataStorage().loadColumns(true, Columns.ADDRESS);

            Punish punish = new Punish();
            punish.setApplier(context.getSender().getName());
            punish.setReason(reason);
            punish.setActive(true);
            punish.setTime(time.get());
            punish.setAddress(account.getData(Columns.ADDRESS).getAsString());
            punish.setCode(Constants.KEY(6, false).toLowerCase());
            punish.setType(type);
            punish.setCategory(category);
            punish.setApplyDate(System.currentTimeMillis());

            punish.assign(account);

            BungeeCord.getInstance().getPluginManager().callEvent(new PunishAssignEvent(account, punish));

            Account finalAccount = account;
            Constants.getAccountStorage().getAccounts().stream().filter(staff -> staff.hasPermission(Rank.PARTNER_PLUS) && staff.getPreference(Preference.STAFFCHAT)).forEach(acc -> {

                ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(acc.getUniqueId());

                if (proxiedPlayer == null)
                    return;

                if (punish.getType() == PunishType.BAN)
                    proxiedPlayer.sendMessage("§e[STAFF]§r §5§lMODS§e: §eJogador §6" + finalAccount.getUsername() + " §efoi banido por " + punish.getCategory().getDisplay(Language.PORTUGUESE).toLowerCase() + ".");

            });

            context.info("command.punish.punished_succesful", type.name().toLowerCase(), category.getName().toLowerCase(), account.getUsername(), reason);

            if (punish.getType() == PunishType.BAN) {
                DataResolver.getInstance().getData(punish.getAddress()).setBanned(true);
            }

            if (!type.getColumns().isEmpty()) {
                type.getColumns().forEach(columns -> context.getAccount().addInt(1, columns));
                context.getAccount().getDataStorage().saveTable(Tables.STAFF);
            }
        }));
    }

    @Command(name = "unpunish", rank = Rank.ASSISTANT_MOD, usage = "unpunish <target> <code|type> [force]", aliases = {"pardon"})
    public void unbanCommand(Context<CommandSender> context, String target, String code) {

        if (context.getAccount().getFlag(Flag.UNPUNISH)) {
            context.info("flag.locked");
            return;
        }

        async(() -> search(context, target, account -> {

            account.loadPunishments();

            Punish punish;

            if (PunishType.fromString(code) != null)
                punish = account.getPunish(PunishType.fromString(code));
            else
                punish = account.getPunish(code);

            if (punish == null || !punish.isActive()) {
                context.info("command.unpunish.not_punished", code);
                return;
            }

            boolean force = context.argsCount() > 2 && context.getArg(2).equalsIgnoreCase("-force");

            if (!force && account.count(PunishType.BAN) > 3) {
                context.info("command.unpunish.failed_to_unpunish");
                return;
            }

            int i = account.unpunish(punish, context.getAccount().getUsername(), force);
            if (i > 0) {
                context.info("command.unpunish.unpunished_succesful", punish.getCode().toLowerCase(), punish.getType().name().toLowerCase(), account.getUsername());
                if (punish.getType() == PunishType.BAN) {
                    DataResolver.getInstance().getData(punish.getAddress()).setBanned(false);
                    account.getData(Columns.BANNED).setData(false);
                    account.getDataStorage().saveColumn(Columns.BANNED);
                } else if (punish.getType() == PunishType.MUTE) {
                    account.getData(Columns.MUTED).setData(false);
                    account.getDataStorage().saveColumn(Columns.MUTED);
                }
            } else
                context.info("command.unpunish.failed_to_unpunish");
        }));
    }

    @Command(name = "fastban", rank = Rank.SECONDARY_MOD, usage = "fb <shortcut>", aliases = {"fb"}, platform = Platform.PLAYER)
    public void fbCommand(Context<ProxiedPlayer> context) {
        ShortcutRepository shortcutRepository = ProxyGame.getInstance().getShortcutRepository();
        Staffer staffer = Staffer.fetch(context.getSender().getUniqueId());

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        if (context.argsCount() < 1) {
            context.info(MessageType.INCORRECT_USAGE.getMessageKey(), "/fb <shortcut>");
            for (Shortcut shortcut : shortcutRepository.getShortcuts()) {
                if (shortcut.getPunishType() == PunishType.BAN) {
                    TextComponent text = getTextComponent(shortcut, staffer); // creating a text message with hover and click events.
                    context.getSender().sendMessage(text);
                }
            }
            return;
        }

        if (staffer.getCurrent() == null) {
            context.info("target.not_found");
            return;
        }

        String shortcut = context.getArg(0);

        if (!shortcutRepository.hasShortcut(shortcut)) {
            context.info("object.not_found", "Shortcut");
            return;
        }

        String fullCommand = shortcutRepository.getShortcut(shortcut).getFullCommand();
        BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), fullCommand.replace("{0}", staffer.getCurrent()));

        staffer.setCurrent(null);

    }

    private static @NotNull TextComponent getTextComponent(Shortcut shortcut, Staffer staffer) {
        TextComponent text = new TextComponent("§c" + shortcut.getShortcut() + ": " + shortcut.getName());
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new TextComponent[] {new TextComponent("§c" + shortcut.getFullCommand().replace("{0}", (staffer.getCurrent() == null ? "nenhum" : staffer.getCurrent())))}));
        if(staffer.getCurrent() != null) {
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fb " + shortcut.getShortcut()));
        }
        return text;
    }

    @Command(name = "cban", rank = Rank.PARTNER_PLUS, usage = "cban <target> <reason>", aliases = {"cheatban", "cbum"})
    public void cbanCommand(Context<CommandSender> context, String target) {

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        if (context.argsCount() < 2 && !context.getAccount().getProperty("isAdmin").getAsBoolean()) {
            context.info(MessageType.INCORRECT_USAGE.getMessageKey(), "/cban <target> <reason>");
            return;
        }

        String reason = createArgs(1, context.getArgs(), "...", false);
        BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "punish ban cheating n " + target + " " + reason);
    }

    @Command(name = "mute", rank = Rank.HELPER, usage = "mute <target> <shortcut> <proof>", aliases = {"mamute"})
    public void muteCommand(Context<CommandSender> context, String target, String shortcut) {

        ShortcutRepository repository = ProxyGame.getInstance().getShortcutRepository();

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        if (context.argsCount() < 2 && !context.getAccount().getProperty("isAdmin").getAsBoolean()) {
            context.sendMessage("§c/mute (target) (shortcut) [proof]");
            for (Shortcut shortcut1 : ProxyGame.getInstance().getShortcutRepository().getShortcuts()) {
                if (shortcut1.getPunishType() == PunishType.MUTE)
                    context.sendMessage("§c" + shortcut1.getShortcut() + " - " + shortcut1.getName());
            }
            return;
        }

        if (context.argsCount() >= 3) {
            String proof = createArgs(2, context.getArgs(), "...", false);
            if (repository.hasShortcut(shortcut)) {
                Shortcut s = repository.getShortcut(shortcut);
                BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), s.getFullCommand().replace("{0}", target).replace("{1}", "(" + proof + ")"));
            } else {
                context.info("object.not_found", "Shortcut");
            }
        } else {
            if (repository.hasShortcut(shortcut)) {
                Shortcut s = repository.getShortcut(shortcut);
                BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), s.getFullCommand().replace("{0}", target).replace(" {1}", ""));
            } else {
                context.info("object.not_found", "Shortcut");
            }
        }


    }

    @Command(name = "badbuild", rank = Rank.PARTNER_PLUS, usage = "badbuild <target>", aliases = {"bb"})
    public void badBuildCommand(Context<CommandSender> context, String target) {

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "punish ban badbuild 1d " + target + " Construção indevida");
    }

    @Command(name = "extremism", rank = Rank.PARTNER_PLUS, usage = "extremism <target>", aliases = {"e"})
    public void extremismCommand(Context<CommandSender> context, String target) {

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "punish ban extremism n " + target + " Extremismo");
    }

    @Command(name = "badnick", rank = Rank.PARTNER_PLUS, usage = "bn <target>", aliases = {"bn"})
    public void badNickCommand(Context<CommandSender> context, String target) {

        if (context.getAccount().getFlag(Flag.PUNISH)) {
            context.info("flag.locked");
            return;
        }

        search(context, target, result -> {
            if (result != null) {
                String timeString = result.getData(Columns.PREMIUM).getAsBoolean() ? "30d" : "n";
                BungeeCord.getInstance().getPluginManager().dispatchCommand(context.getSender(), "punish ban badnick " + timeString + " " + target + " nickname");
            } else {
                context.info("object.not_found", "Player");
            }
        });

    }

    @Completer(name = "p")
    public List<String> handlePunishComplete(Context<CommandSender> context) {
        String[] args = context.getArgs();
        if (args.length == 1)
            return Stream.of(PunishType.values()).filter(c -> startsWith(c.name(), args[0])).map(c -> c.name().toLowerCase()).collect(Collectors.toList());
        if (args.length == 2)
            return Stream.of(PunishCategory.values()).filter(c -> startsWith(c.getName(), args[1]) && context.getAccount().hasPermission(c.getRank())).map(c -> c.getName().toLowerCase()).collect(Collectors.toList());
        else if (args.length == 3)
            return Stream.of("n", "5h", "3d", "7d", "1mo", "3mo", "6mo", "1y").filter(c -> startsWith(c, args[2])).collect(Collectors.toList());
        else if (args.length == 4)
            return getOnlineNicknames(context);
        else {
            PunishCategory punishCategory = PunishCategory.fromString(args[1]);

            if (punishCategory == null)
                return Collections.emptyList();
            return punishCategory.getSuggestions().stream().filter(c -> startsWith(c, context.getArg(context.argsCount() - 1))).collect(Collectors.toList());
        }
    }

    @Completer(name = "cban")
    public List<String> cbanComplete(Context<CommandSender> context) {
        String[] args = context.getArgs();
        if (args.length == 1)
            return getOnlineNicknames(context);
        return PunishCategory.CHEATING.getSuggestions().stream().filter(c -> startsWith(c, context.getArg(context.argsCount() - 1))).collect(Collectors.toList());
    }

    @Completer(name = "badbuild")
    public List<String> badBuildComplete(Context<CommandSender> context) {
        return getOnlineNicknames(context);
    }

    @Completer(name = "fastban")
    public List<String> fastBanComplete(Context<ProxiedPlayer> context) {
        return ProxyGame.getInstance().getShortcutRepository().getShortcuts().stream().filter(s -> s.getPunishType() == PunishType.BAN).map(Shortcut::getShortcut).collect(Collectors.toList());
    }

    @Completer(name = "mute")
    public List<String> muteComplete(Context<CommandSender> context) {

        if (context.getArgs().length == 1) {
            return getOnlineNicknames(context);
        } else if (context.getArgs().length == 2) {
            return ProxyGame.getInstance().getShortcutRepository().getShortcuts().stream().filter(s -> s.getPunishType() == PunishType.MUTE).map(Shortcut::getShortcut).collect(Collectors.toList());
        } else {
            return Collections.singletonList("insira sua prova");
        }

    }

    @Completer(name = "extremism")
    public List<String> extremismComplete(Context<CommandSender> context) {
        return getOnlineNicknames(context);
    }

    @Completer(name = "badnick")
    public List<String> badNickComplete(Context<CommandSender> context) {
        return getOnlineNicknames(context);
    }

    @Completer(name = "unpunish")
    public List<String> unpunishCompleter(Context<CommandSender> context) {
        String[] args = context.getArgs();
        if (args.length == 1)
            return getOnlineNicknames(context);
        else if (args.length == 2)
            return Stream.of(PunishType.values()).filter(c -> startsWith(c.name(), args[1])).map(c -> c.name().toLowerCase()).collect(Collectors.toList());
        return Collections.singletonList("-force");
    }

}
