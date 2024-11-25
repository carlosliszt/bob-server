/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.*;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.database.mojang.MojangAPI;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.*;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.core.util.communication.AccountRankUpdateData;
import com.minecraft.core.util.geodata.AddressData;
import com.minecraft.core.util.geodata.DataResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountCommand implements ProxyInterface {

    @Command(name = "acc", aliases = {"account"})
    public void handleCommand(Context<CommandSender> context) {

        String[] args = context.getArgs();

        Account localAccount = context.getAccount();

        if (args.length == 0)
            Argument.INFO.getExecutor().execute(localAccount, context);
        else {
            if (!localAccount.hasPermission(Argument.INFO.getRank())) {
                context.info("command.account.argument.info.insufficient_permission");
                return;
            }

            async(() -> search(context, args[0], account -> {
                if (args.length == 1)
                    Argument.INFO.getExecutor().execute(account, context);
                else {
                    Argument argument = Argument.get(args[1]);

                    if (argument == null) {
                        context.info("no_function", args[1].toLowerCase());
                        return;
                    }

                    if (!localAccount.hasPermission(argument.getRank())) {
                        context.info("no_function", args[1].toLowerCase());
                        return;
                    }
                    argument.getExecutor().execute(account, context);
                }
            }));
        }
    }

    @Completer(name = "acc")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.getAccount().hasPermission(Rank.PARTNER_PLUS)) {
            String[] args = context.getArgs();
            if (args.length == 1)
                return getOnlineNicknames(context);
            else if (args.length == 2)
                return Arrays.stream(Argument.values()).filter(arg -> arg.getArg() != null && context.getAccount().hasPermission(arg.getRank()) && startsWith(arg.getArg(), args[1])).map(Argument::getArg).sorted().collect(Collectors.toList());
            else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("rank"))
                    return Arrays.stream(Rank.values()).filter(rank -> rank != Rank.MEMBER && startsWith(rank.getDisplayName(), args[2]) && context.getAccount().hasPermission(rank)).map(r -> r.getDisplayName().toLowerCase()).collect(Collectors.toList());
                else if (args[1].equalsIgnoreCase("tag"))
                    return Arrays.stream(Tag.values())
                            .filter(tag -> tag != Tag.MEMBER && context.getAccount().getTagList().hasTag(tag))
                            .map(tag -> tag.name().toLowerCase())
                            .collect(Collectors.toList());
                else if (args[1].equalsIgnoreCase("medal"))
                    return Arrays.stream(Medal.values()).filter(medal -> medal != Medal.NONE && context.getAccount().hasPermission(medal.getRank()) && startsWith(medal.getName(), args[2])).map(medal -> medal.name().toLowerCase()).collect(Collectors.toList());
                else if (args[1].equalsIgnoreCase("clantag"))
                    return Arrays.stream(Clantag.values()).filter(clantag -> clantag != Clantag.DEFAULT && context.getAccount().hasPermission(clantag.getRank()) && startsWith(clantag.getName(), args[2])).map(clantag -> clantag.name().toLowerCase()).collect(Collectors.toList());
                else if (args[1].equalsIgnoreCase("pluscolor"))
                    return Arrays.stream(PlusColor.values()).filter(plusColor -> plusColor != PlusColor.GOLDEN && startsWith(plusColor.getName(), args[2])).map(plusColor -> plusColor.name().toLowerCase()).collect(Collectors.toList());
                else if (args[1].equalsIgnoreCase("flag"))
                    return Arrays.stream(Flag.values()).filter(flag -> context.getAccount().hasPermission(flag.getRank()) && startsWith(flag.getName(), args[2])).map(flag -> flag.getName().toLowerCase()).collect(Collectors.toList());
            } else if (args.length == 4) {
                if (args[1].equalsIgnoreCase("rank"))
                    return Stream.of("n", "5h", "3d", "7d", "1mo", "3mo", "6mo", "1y").filter(c -> startsWith(c, args[3])).collect(Collectors.toList());
                if (args[1].equalsIgnoreCase("flag"))
                    return Stream.of("true", "false").filter(c -> startsWith(c, args[3])).collect(Collectors.toList());

            }
        }
        return Collections.emptyList();
    }

    private interface Executor {
        void execute(Account account, Context<CommandSender> context);
    }

    @Getter
    @AllArgsConstructor
    public enum Argument {

        RANK("rank", Rank.ADMINISTRATOR, (account, context) -> {

            String[] args = context.getArgs();
            String author = (context.isPlayer() ? context.getSender().getName() : "[SERVER]");

            if (args.length >= 3) {

                Rank rank = Rank.fromString(args[2]);

                if (rank == null || rank == Rank.MEMBER) {
                    context.info("object.not_found", "Rank");
                    return;
                }

                if (context.isPlayer() && !context.getAccount().hasPermission(rank)) {
                    context.info(MessageType.NO_PERMISSION.getMessageKey());
                    return;
                }

                account.loadRanks();

                if (args.length < 4) {
                    if (account.hasRank(rank)) {
                        RankData rankData = account.getRankData(rank);

                        if (rankData.isPermanent()) {
                            account.removeRank(rank);
                            context.info("command.account.argument.rank.rank_remove", rank.getName(), account.getUsername());

                            AccountRankUpdateData data = new AccountRankUpdateData(account.getUniqueId(), rank, 0, 0, 0, "", AccountRankUpdateData.Action.REMOVE);
                            Constants.getRedis().publish(Redis.RANK_UPDATE_CHANNEL, Constants.GSON.toJson(data));
                        } else {
                            account.removeRank(rank);
                            account.giveRank(rank, -1, author);
                            context.info("command.account.argument.rank.rank_replace", rank.getName(), account.getUsername());

                            if (rank == Rank.ULTRA_PLUS) {
                                account.addInt(20, Columns.ULTRA_PLUS_MONTHS);
                            }

                            AccountRankUpdateData data = new AccountRankUpdateData(account.getUniqueId(), rank, System.currentTimeMillis(), System.currentTimeMillis(), -1, author, AccountRankUpdateData.Action.REPLACE);
                            Constants.getRedis().publish(Redis.RANK_UPDATE_CHANNEL, Constants.GSON.toJson(data));
                        }
                    } else {
                        account.giveRank(rank, -1, author);
                        context.info("command.account.argument.rank.rank_add", rank.getName(), account.getUsername());

                        if (rank == Rank.ULTRA_PLUS) {
                            account.addInt(20, Columns.ULTRA_PLUS_MONTHS);
                        }

                        AccountRankUpdateData data = new AccountRankUpdateData(account.getUniqueId(), rank, System.currentTimeMillis(), -1, -1, author, AccountRankUpdateData.Action.ADD);
                        Constants.getRedis().publish(Redis.RANK_UPDATE_CHANNEL, Constants.GSON.toJson(data));
                    }
                } else {

                    long expiration;

                    try {
                        expiration = StringTimeUtils.parseDateDiff(args[3], true);
                    } catch (Exception e) {
                        context.info("invalid_time", "y,m,d,min,s");
                        return;
                    }

                    if (account.hasRank(rank)) {
                        RankData rankData = account.getRankData(rank);

                        if (rankData.isPermanent()) {
                            context.info("command.account.argument.rank.player_already_have_rank");
                        } else {

                            expiration = expiration + (rankData.getExpiration() - rankData.getAddedAt());

                            account.removeRank(rank);
                            account.giveRank(rank, expiration, author, rankData.getAddedAt(), System.currentTimeMillis());
                            context.info("command.account.argument.rank.rank_add", rank.getName(), account.getUsername());

                            AccountRankUpdateData data = new AccountRankUpdateData(account.getUniqueId(), rank, rankData.getAddedAt(), System.currentTimeMillis(), expiration, author, AccountRankUpdateData.Action.REPLACE);
                            Constants.getRedis().publish(Redis.RANK_UPDATE_CHANNEL, Constants.GSON.toJson(data));

                            if (rank == Rank.ULTRA_PLUS) {
                                long months = StringTimeUtils.getMonthsFromTimeString(args[3]);
                                account.addInt((int) months, Columns.ULTRA_PLUS_MONTHS);
                                context.info("command.account.argument.rank.ultra_plus_updated", rank.getName(), months, account.getUsername());
                            }

                        }
                    } else {
                        context.info("command.account.argument.rank.rank_add", rank.getName(), account.getUsername());
                        account.giveRank(rank, expiration, author);

                        if (rank == Rank.ULTRA_PLUS) {
                            long months = StringTimeUtils.getMonthsFromTimeString(args[3]);
                            account.addInt((int) months, Columns.ULTRA_PLUS_MONTHS);
                            context.info("command.account.argument.rank.ultra_plus_updated", rank.getName(), months, account.getUsername());
                        }

                        AccountRankUpdateData data = new AccountRankUpdateData(account.getUniqueId(), rank, System.currentTimeMillis(), -1, expiration, author, AccountRankUpdateData.Action.ADD);
                        Constants.getRedis().publish(Redis.RANK_UPDATE_CHANNEL, Constants.GSON.toJson(data));

                    }
                }


                account.getTagList().loadTags();
                account.getData(Columns.TAG).setData(account.getTagList().getHighestTag().getUniqueCode());
                account.getDataStorage().saveTable(Tables.ACCOUNT, Tables.OTHER);

            } else {
                context.sendMessage("§cUso do /account:");
                context.sendMessage("§c* /account <user> rank <rank> [time]");
            }
        }),

        PLUSCOLOR("pluscolor", Rank.ASSISTANT_MOD, (account, context) -> {

            String[] args = context.getArgs();
            String author = (context.isPlayer() ? context.getSender().getName() : "[SERVER]");

            if (args.length >= 3) {

                PlusColor plusColor;

                try {
                    plusColor = PlusColor.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    context.info("object.not_found", "PlusColor");
                    return;
                }

                if (plusColor == PlusColor.GOLDEN) {
                    context.info("object.not_found", "PlusColor");
                    return;
                }

                account.loadPlusColors();

                if (args.length < 4) {
                    if (account.hasPlusColor(plusColor)) {
                        PlusColorData plusColorData = account.getPlusColorData(plusColor);

                        if (plusColorData.isPermanent()) {
                            account.removePlusColor(plusColor);
                            context.info("command.account.argument.pluscolor.pluscolor_remove", plusColor.getName(), account.getUsername());
                        } else {
                            account.removePlusColor(plusColor);
                            account.givePlusColors(plusColor, -1, author);
                            context.info("command.account.argument.pluscolor.pluscolor_replace", plusColor.getName(), account.getUsername());
                        }
                    } else {
                        account.givePlusColors(plusColor, -1, author);
                        context.info("command.account.argument.pluscolor.pluscolor_add", plusColor.getName(), account.getUsername());
                    }
                } else {

                    long expiration;

                    try {
                        expiration = StringTimeUtils.parseDateDiff(args[3], true);
                    } catch (Exception e) {
                        context.info("invalid_time", "y,m,d,min,s");
                        return;
                    }

                    if (account.hasPlusColor(plusColor)) {
                        PlusColorData plusColorData = account.getPlusColorData(plusColor);

                        if (plusColorData.isPermanent()) {
                            context.info("command.account.argument.pluscolor.player_already_have_pluscolor");
                        } else {

                            expiration = expiration + (plusColorData.getExpiration() - plusColorData.getAddedAt());

                            account.removePlusColor(plusColor);
                            account.givePlusColors(plusColor, expiration, author);
                            context.info("command.account.argument.pluscolor.pluscolor_add", plusColor.getName(), account.getUsername());
                        }
                    } else {
                        context.info("command.account.argument.pluscolor.pluscolor_add", plusColor.getName(), account.getUsername());
                        account.givePlusColors(plusColor, expiration, author);
                    }
                }

                account.getPlusColorList().loadPlusColor();
                account.getData(Columns.PLUSCOLOR).setData(account.getPlusColorList().getHighestPlusColor().getUniqueCode());
            }
        }),

        PLUSCOLORS("pluscolors", Rank.PRIMARY_MOD, (account, context) -> {
            account.loadPlusColors();

            if (account.getPlusColors().size() == 0) {
                context.sendMessage("§cNenhuma cor de plus encontrada.");
                return;
            }

            Account localAccount = context.getAccount();
            boolean showAddedBy = localAccount.hasPermission(Rank.ADMINISTRATOR);

            account.getPlusColors().stream().sorted((a, b) -> Long.compare(b.getAddedAt(), a.getAddedAt())).forEach(plusColorData -> {
                context.sendMessage("§aCor de plus: §f" + plusColorData.getPlusColor().getName());
                context.sendMessage("  §7Adicionada em: " + localAccount.getLanguage().getDateFormat().format(plusColorData.getAddedAt()));
                if (showAddedBy)
                    context.sendMessage("  §7Adicionada por: " + plusColorData.getAddedBy());
                if (!plusColorData.isPermanent())
                    context.sendMessage("  §7Expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, plusColorData.getExpiration()));
            });
        }),

        TAG("tag", Rank.ASSISTANT_MOD, (account, context) -> {

            String[] args = context.getArgs();
            String author = (context.isPlayer() ? context.getSender().getName() : "[SERVER]");

            if (args.length >= 3) {

                Tag tag;

                try {
                    tag = Tag.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    context.info("object.not_found", "Tag");
                    return;
                }

                if (tag == Tag.MEMBER) {
                    context.info("object.not_found", "Tag");
                    return;
                }

                account.loadTags();

                if (args.length < 4) {
                    if (account.hasTag(tag)) {
                        TagData tagData = account.getTagData(tag);

                        if (tagData.isPermanent()) {
                            account.removeTag(tag);
                            context.info("command.account.argument.tag.tag_remove", tag.getName(), account.getUsername());
                        } else {
                            account.removeTag(tag);
                            account.giveTag(tag, -1, author);
                            context.info("command.account.argument.tag.tag_replace", tag.getName(), account.getUsername());
                        }
                    } else {
                        account.giveTag(tag, -1, author);
                        context.info("command.account.argument.tag.tag_add", tag.getName(), account.getUsername());
                    }
                } else {

                    long expiration;

                    try {
                        expiration = StringTimeUtils.parseDateDiff(args[3], true);
                    } catch (Exception e) {
                        context.info("invalid_time", "y,m,d,min,s");
                        return;
                    }

                    if (account.hasTag(tag)) {
                        TagData tagData = account.getTagData(tag);

                        if (tagData.isPermanent()) {
                            context.info("command.account.argument.tag.player_already_have_tag");
                        } else {

                            expiration = expiration + (tagData.getExpiration() - tagData.getAddedAt());

                            account.removeTag(tag);
                            account.giveTag(tag, expiration, author);
                            context.info("command.account.argument.tag.tag_add", tag.getName(), account.getUsername());
                        }
                    } else {
                        context.info("command.account.argument.tag.tag_add", tag.getName(), account.getUsername());
                        account.giveTag(tag, expiration, author);
                    }
                }

                account.getTagList().loadTags();
                account.getData(Columns.TAG).setData(account.getTagList().getHighestTag().getUniqueCode());
                account.getDataStorage().saveTable(Tables.ACCOUNT, Tables.OTHER);

            } else {
                context.sendMessage("§cUso do /account:");
                context.sendMessage("§c* /account <user> tag <tag> [time]");
            }
        }),

        MEDAL("medal", Rank.ASSISTANT_MOD, (account, context) -> {

            String[] args = context.getArgs();
            String author = (context.isPlayer() ? context.getSender().getName() : "[SERVER]");

            if (args.length >= 3) {

                Medal medal;

                try {
                    medal = Medal.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    context.info("object.not_found", "Medal");
                    return;
                }

                if (medal == Medal.NONE) {
                    context.info("object.not_found", "Medal");
                    return;
                }

                account.loadMedals();

                if (args.length < 4) {
                    if (account.hasMedal(medal)) {
                        MedalData medalData = account.getMedalData(medal);

                        if (medalData.isPermanent()) {
                            account.removeMedal(medal);
                            context.info("command.account.argument.medal.medal_remove", medal.getName(), account.getUsername());
                        } else {
                            account.removeMedal(medal);
                            account.giveMedal(medal, -1, author);
                            context.info("command.account.argument.medal.medal_replace", medal.getName(), account.getUsername());
                        }
                    } else {
                        account.giveMedal(medal, -1, author);
                        context.info("command.account.argument.medal.medal_add", medal.getName(), account.getUsername());
                    }
                } else {

                    long expiration;

                    try {
                        expiration = StringTimeUtils.parseDateDiff(args[3], true);
                    } catch (Exception e) {
                        context.info("invalid_time", "y,m,d,min,s");
                        return;
                    }

                    if (account.hasMedal(medal)) {
                        MedalData medalData = account.getMedalData(medal);

                        if (medalData.isPermanent()) {
                            context.info("command.account.argument.medal.player_already_have_medal");
                        } else {

                            expiration = expiration + (medalData.getExpiration() - medalData.getAddedAt());

                            account.removeMedal(medal);
                            account.giveMedal(medal, expiration, author, medalData.getAddedAt(), System.currentTimeMillis());
                            context.info("command.account.argument.medal.medal_add", medal.getName(), account.getUsername());
                        }
                    } else {
                        context.info("command.account.argument.medal.medal_add", medal.getName(), account.getUsername());
                        account.giveMedal(medal, expiration, author);
                    }
                }

                account.getMedalList().loadMedals();
                account.getData(Columns.MEDAL).setData(account.getMedalList().getHighestMedal().getUniqueCode());
                account.getDataStorage().saveTable(Tables.ACCOUNT, Tables.OTHER);

            } else {
                context.sendMessage("§cUso do /account:");
                context.sendMessage("§c* /account <user> medal <medal> [time]");
            }
        }),

        RESETNICK("resetnick", Rank.ADMINISTRATOR, (account, context) -> {
            account.getData(Columns.NICK).setData("...");
            Constants.getRedis().publish(Redis.NICK_DISGUISE_CHANNEL, account.getUniqueId() + ":" + account.getUsername());
            account.getDataStorage().saveColumnsFromSameTable(Columns.NICK);
            context.sendMessage("§aNickname de " + account.getUsername() + " foi resetado.");
        }),

        NICKS("nicks", Rank.ADMINISTRATOR, (account, context) -> {
            account.loadNicks();

            context.sendMessage("§aVerificando nicks de " + account.getUsername() + "...");

            if (account.getNicks().isEmpty()) {
                context.sendMessage("§cNenhum nick encontrado.");
                return;
            }

            Account localAccount = context.getAccount();

            account.getNicks().stream().sorted((a, b) -> Long.compare(b.getDateChanged(), a.getDateChanged())).forEach(rankData -> {
                TextComponent t2 = createTextComponent(" §aNick: §f" + rankData.getNickname(), null, null, null, null);
                context.getSender().sendMessage(t2);
                context.sendMessage("  §7Escolhido em: " + localAccount.getLanguage().getDateFormat().format(rankData.getDateChanged()));
                context.sendMessage("  §7Expira em: " + localAccount.getLanguage().getDateFormat().format(rankData.getExpiry()));
            });

        }),

        SETNICK("setnick", Rank.DEVELOPER_ADMIN, (account, context) -> {
            String[] args = context.getArgs();
            String author = (context.isPlayer() ? context.getSender().getName() : "[SERVER]");

            if (args.length >= 3) {
                String nickname = args[2];

                if (nickname.length() > 16) {
                    context.sendMessage(" §cNick muito longo.");
                    return;
                }

                account.getData(Columns.NICK).setData(nickname);
                Constants.getRedis().publish(Redis.NICK_DISGUISE_CHANNEL, account.getUniqueId() + ":" + nickname);
                account.getDataStorage().saveColumnsFromSameTable(Columns.NICK);

                account.getDataStorage().saveTable(Tables.ACCOUNT, Tables.OTHER);

                context.sendMessage("§aNickname de " + account.getUsername() + " foi alterado para " + nickname + ".");
            } else {
                context.sendMessage("§cUso do /account:");
                context.sendMessage("§c* /account <user> setnick <nickname>");
                context.sendMessage("§c§lCUIDADO!§c Esse nick pode ser qualquer coisa -w-");
            }

        }),

        CLANTAG("clantag", Rank.ADMINISTRATOR, (account, context) -> {

            String[] args = context.getArgs();
            String author = (context.isPlayer() ? context.getSender().getName() : "[SERVER]");

            if (args.length >= 3) {

                Clantag clantag;

                try {
                    clantag = Clantag.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    context.info("object.not_found", "ClanTag");
                    return;
                }

                if (clantag == null || clantag == Clantag.DEFAULT) {
                    context.info("object.not_found", "ClanTag");
                    return;
                }

                account.loadClanTags();

                if (args.length < 4) {
                    if (account.hasClanTag(clantag)) {
                        ClanTagData clanTagData = account.getClanTaData(clantag);

                        if (clanTagData.isPermanent()) {
                            account.removeClanTag(clantag);
                            context.info("command.account.argument.clantag.clantag_remove", clantag.getName(), account.getUsername());
                        } else {
                            account.removeClanTag(clantag);
                            account.giveClanTag(clantag, -1, author);
                            context.info("command.account.argument.clantag.clantag_replace", clantag.getName(), account.getUsername());
                        }
                    } else {
                        account.giveClanTag(clantag, -1, author);
                        context.info("command.account.argument.clantag.clantag_add", clantag.getName(), account.getUsername());
                    }
                } else {

                    long expiration;

                    try {
                        expiration = StringTimeUtils.parseDateDiff(args[3], true);
                    } catch (Exception e) {
                        context.info("invalid_time", "y,m,d,min,s");
                        return;
                    }

                    if (account.hasClanTag(clantag)) {
                        ClanTagData clanTagData = account.getClanTaData(clantag);

                        if (clanTagData.isPermanent()) {
                            context.info("command.account.argument.clantag.player_already_have_clantag");
                        } else {

                            expiration = expiration + (clanTagData.getExpiration() - clanTagData.getAddedAt());

                            account.removeClanTag(clantag);
                            account.giveClanTag(clantag, expiration, author, clanTagData.getAddedAt(), System.currentTimeMillis());
                            context.info("command.account.argument.clantag.clantag_add", clantag.getName(), account.getUsername());
                        }
                    } else {
                        context.info("command.account.argument.clantag.clantag_add", clantag.getName(), account.getUsername());
                        account.giveClanTag(clantag, expiration, author);
                    }
                }

                account.getClanTagList().loadClanTags();
                account.getData(Columns.CLANTAG).setData(account.getClanTagList().getHighestClanTag().getUniqueCode());
                account.getDataStorage().saveTable(Tables.ACCOUNT, Tables.OTHER);

            } else {
                context.sendMessage("§cUso do /account:");
                context.sendMessage("§c* /account <user> clantag <clantag> [time]");
            }
        }),

        CLANTAGS("clantags", Rank.PRIMARY_MOD, (account, context) -> {
            account.loadClanTags();

            if (account.getClanTags().size() == 0) {
                context.sendMessage("§cNenhuma clan tag encontrada.");
                return;
            }

            Account localAccount = context.getAccount();
            boolean showAddedBy = localAccount.hasPermission(Rank.ADMINISTRATOR);

            account.getClanTags().stream().sorted((a, b) -> Long.compare(b.getAddedAt(), a.getAddedAt())).forEach(clanTagData -> {
                context.sendMessage("§aClantag: §f" + clanTagData.getClantag().getName());
                context.sendMessage("  §7Adicionada em: " + localAccount.getLanguage().getDateFormat().format(clanTagData.getAddedAt()));
                if (showAddedBy)
                    context.sendMessage("  §7Adicionada por: " + clanTagData.getAddedBy());
                if (!clanTagData.isPermanent())
                    context.sendMessage("  §7Expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, clanTagData.getExpiration()));
            });
        }),

        RANKS("ranks", Rank.TRIAL_MODERATOR, (account, context) -> {

            account.loadRanks();

            if (account.getRanks().size() == 0) {
                context.sendMessage("§cNenhum rank encontrado.");
                return;
            }

            Account localAccount = context.getAccount();
            boolean showAddedBy = localAccount.hasPermission(Rank.ADMINISTRATOR);

            account.getRanks().stream().sorted((a, b) -> Long.compare(b.getAddedAt(), a.getAddedAt())).forEach(rankData -> {
                TextComponent t1 = createTextComponent("§8[§c-§8]", null, null, ClickEvent.Action.RUN_COMMAND, "/account " + account.getUsername() + " rank " + rankData.getRank().getDisplayName());
                TextComponent t2 = createTextComponent(" §aRank: §f" + rankData.getRank().getName(), null, null, null, null);
                context.getSender().sendMessage(t1, t2);
                context.sendMessage("  §7Adicionado em: " + localAccount.getLanguage().getDateFormat().format(rankData.getAddedAt()));
                context.sendMessage("  §7Atualizado em: " + localAccount.getLanguage().getDateFormat().format(rankData.getUpdatedAt()));
                if (showAddedBy)
                    context.sendMessage("  §7Adicionado por: " + rankData.getAddedBy());
                if (!rankData.isPermanent())
                    context.sendMessage("  §7Expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, rankData.getExpiration()));
            });
        }),

        TAGS("tags", Rank.PRIMARY_MOD, (account, context) -> {
            account.loadTags();

            if (account.getTags().size() == 0) {
                context.sendMessage("§cNenhuma tag encontrada.");
                return;
            }

            Account localAccount = context.getAccount();
            boolean showAddedBy = localAccount.hasPermission(Rank.ADMINISTRATOR);

            account.getTags().stream().sorted((a, b) -> Long.compare(b.getAddedAt(), a.getAddedAt())).forEach(tagData -> {
                context.sendMessage("§aTag: §f" + tagData.getTag().getName());
                context.sendMessage("  §7Adicionada em: " + localAccount.getLanguage().getDateFormat().format(tagData.getAddedAt()));
                if (showAddedBy)
                    context.sendMessage("  §7Adicionada por: " + tagData.getAddedBy());
                if (!tagData.isPermanent())
                    context.sendMessage("  §7Expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, tagData.getExpiration()));
            });
        }),

        MEDALS("medals", Rank.PRIMARY_MOD, (account, context) -> {
            account.loadMedals();

            if (account.getMedals().size() == 0) {
                context.sendMessage("§cNenhuma medalha encontrada.");
                return;
            }

            Account localAccount = context.getAccount();
            boolean showAddedBy = localAccount.hasPermission(Rank.ADMINISTRATOR);

            account.getMedals().stream().sorted((a, b) -> Long.compare(b.getAddedAt(), a.getAddedAt())).forEach(medalData -> {
                context.sendMessage("§aMedal: §f" + medalData.getMedal().getName());
                context.sendMessage("  §7Adicionada em: " + localAccount.getLanguage().getDateFormat().format(medalData.getAddedAt()));
                if (showAddedBy)
                    context.sendMessage("  §7Adicionada por: " + medalData.getAddedBy());
                if (!medalData.isPermanent())
                    context.sendMessage("  §7Expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, medalData.getExpiration()));
            });
        }),

        NICKNAMES("nicknames", Rank.ADMINISTRATOR, (account, context) -> {
            context.sendMessage("§aCarregando...");

            if (!account.getData(Columns.PREMIUM).getAsBoolean()) {
                context.sendMessage("§cEste usuário não é registrado no Minecraft.");
                return;
            }

            List<MojangAPI.Name> names = Constants.getMojangAPI().requestNames(account.getUniqueId());

            names.forEach(name -> {
                context.sendMessage("§aNickname: §f" + name.getName());
                if (name.hasDate())
                    context.sendMessage(" §7Alterado em: " + context.getAccount().getLanguage().getDateFormat().format(name.getChangedToAt()));
                else
                    context.sendMessage(" §7(Primeiro nickname da conta)");
            });
        }),

        BANS("bans", Rank.TRIAL_MODERATOR, (account, context) ->

        {

            account.loadPunishments();

            Account localAccount = context.getAccount();

            List<Punish> punishments = account.getPunishments().stream().filter(c -> c.getType() == PunishType.BAN).collect(Collectors.toList());

            if (!punishments.isEmpty()) {
                context.sendMessage("§aBans: ");

                for (Punish punish : punishments) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("§7[").append("#").append(punish.getCode()).append(" - ").append(localAccount.getLanguage().getDateFormat().format(new Date(punish.getApplyDate()))).append("] §eMotivo §b").append((localAccount.hasPermission(Rank.ASSISTANT_MOD) ? punish.getReason() + "§b/" : "")).append(punish.getCategory().getDisplay(localAccount.getLanguage()));
                    if (!punish.isPermanent() && !punish.isExpired())
                        stringBuilder.append(" §eExpira em §b").append(DateUtils.formatDifference(punish.getTime(), localAccount.getLanguage(), DateUtils.Style.NORMAL));
                    TextComponent t1 = createTextComponent((punish.isActive() ? "§8[§c-§8] " : ""), null, null, ClickEvent.Action.RUN_COMMAND, "/unpunish " + account.getUsername() + ' ' + punish.getCode() + " -force");
                    TextComponent t2 = createTextComponent(stringBuilder.toString(), null, null, null, null);
                    TextComponent t3 = createTextComponent(localAccount.hasPermission(Rank.ASSISTANT_MOD) ? " §eAdicionado por §6" + punish.getApplier() + (!punish.isActive() ? " §eRemovido em §b" + localAccount.getLanguage().getDateFormat().format(new Date(punish.getUnpunishDate())) + " §epor §6" + punish.getUnpunisher() : "") : "", null, null, null, null);
                    context.getSender().sendMessage(t1, t2, t3);
                }
            } else {
                context.info("command.account.no_punishments", "ban");
            }
        }),

        MUTES("mutes", Rank.TRIAL_MODERATOR, (account, context) ->

        {

            account.loadPunishments();

            Account localAccount = context.getAccount();

            List<Punish> punishments = account.getPunishments().stream().filter(c -> c.getType() == PunishType.MUTE && c.getCategory() == PunishCategory.COMMUNITY).collect(Collectors.toList());

            if (!punishments.isEmpty()) {
                context.sendMessage("§aMutes: ");

                for (Punish punish : punishments) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("§7[").append("#").append(punish.getCode()).append(" - ").append(localAccount.getLanguage().getDateFormat().format(new Date(punish.getApplyDate()))).append("] §eMotivo §b").append((localAccount.hasPermission(Rank.ASSISTANT_MOD) ? punish.getReason() + "§b/" : "")).append(punish.getCategory().getDisplay(localAccount.getLanguage()));
                    if (!punish.isPermanent() && !punish.isExpired())
                        stringBuilder.append(" §eExpira em §b").append(DateUtils.formatDifference(punish.getTime(), localAccount.getLanguage(), DateUtils.Style.NORMAL));
                    TextComponent t1 = createTextComponent((punish.isActive() ? "§8[§c-§8] " : ""), null, null, ClickEvent.Action.RUN_COMMAND, "/unpunish " + account.getUsername() + ' ' + punish.getCode() + " -force");
                    TextComponent t2 = createTextComponent(stringBuilder.toString(), null, null, null, null);
                    TextComponent t3 = createTextComponent(localAccount.hasPermission(Rank.ASSISTANT_MOD) ? " §eAdicionado por §6" + punish.getApplier() + (!punish.isActive() ? " §eRemovido em §b" + localAccount.getLanguage().getDateFormat().format(new Date(punish.getUnpunishDate())) + " §epor §6" + punish.getUnpunisher() : "") : "", null, null, null, null);
                    context.getSender().sendMessage(t1, t2, t3);
                }
            } else {
                context.info("command.account.no_punishments", "mute");
            }
        }),

        PUNISHMENTS("punishments", Rank.ADMINISTRATOR, (account, context) -> {

            account.loadRanks();

            if (!account.getRank().isStaffer()) {
                context.sendMessage("§cEste jogador não faz parte da equipe.");
                return;
            }

            account.getDataStorage().loadIfUnloaded(Tables.STAFF.getColumns());

            for (final PunishType punishType : PunishType.getValues()) {
                if (punishType.getColumns().isEmpty())
                    continue;
                context.sendMessage("§aPunições de " + punishType.getName());
                context.sendMessage(" §7Semanal: " + account.getData(punishType.getColumns().get(2)).getAsInteger());
                context.sendMessage(" §7Mensal: " + account.getData(punishType.getColumns().get(1)).getAsInteger());
                context.sendMessage(" §7Total: " + account.getData(punishType.getColumns().get(0)).getAsInteger());
            }
        }),

        RESETPUNISHMENTS("resetPunishments", Rank.ADMINISTRATOR, (account, context) -> {

            account.loadRanks();

            if (!account.getRank().isStaffer()) {
                context.sendMessage("§cEste jogador não faz parte da equipe.");
                return;
            }

            account.getDataStorage().loadIfUnloaded(Tables.STAFF.getColumns());

            for (final PunishType punishType : PunishType.getValues()) {
                punishType.getColumns().forEach(columns -> account.getData(columns).setData(0));
            }

            account.getDataStorage().saveTable(Tables.STAFF);

            context.sendMessage("§cAs punições de " + account.getUsername() + " foram redefinidas.");
        }),

        EVENTBLACKLISTS("eventblacklists", Rank.TRIAL_MODERATOR, (account, context) ->

        {

            account.loadPunishments();

            Account localAccount = context.getAccount();

            List<Punish> punishments = account.getPunishments().stream().filter(c -> c.getType() == PunishType.EVENT).collect(Collectors.toList());

            if (!punishments.isEmpty()) {
                context.sendMessage("§aEvent blacklists: ");

                for (Punish punish : punishments) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("§7[").append("#").append(punish.getCode()).append(" - ").append(localAccount.getLanguage().getDateFormat().format(new Date(punish.getApplyDate()))).append("] §eMotivo §b").append((localAccount.hasPermission(Rank.ASSISTANT_MOD) ? punish.getReason() + "§b/" : "")).append(punish.getCategory().getDisplay(localAccount.getLanguage()));
                    if (!punish.isPermanent() && !punish.isExpired())
                        stringBuilder.append(" §eExpira em §b").append(DateUtils.formatDifference(punish.getTime(), localAccount.getLanguage(), DateUtils.Style.NORMAL));
                    TextComponent t1 = createTextComponent((punish.isActive() ? "§8[§c-§8] " : ""), null, null, ClickEvent.Action.RUN_COMMAND, "/unpunish " + account.getUsername() + ' ' + punish.getCode() + " -force");
                    TextComponent t2 = createTextComponent(stringBuilder.toString(), null, null, null, null);
                    TextComponent t3 = createTextComponent(localAccount.hasPermission(Rank.ASSISTANT_MOD) ? " §eAdicionado por §6" + punish.getApplier() + (!punish.isActive() ? " §eRemovido em §b" + localAccount.getLanguage().getDateFormat().format(new Date(punish.getUnpunishDate())) + " §epor §6" + punish.getUnpunisher() : "") : "", null, null, null, null);
                    context.getSender().sendMessage(t1, t2, t3);
                }
            } else {
                context.info("command.account.no_punishments", "event");
            }
        }),

        REPORTMUTES("reportmutes", Rank.ADMINISTRATOR, (account, context) ->

        {

            account.loadPunishments();

            Account localAccount = context.getAccount();

            List<Punish> punishments = account.getPunishments().stream().filter(c -> c.getType() == PunishType.MUTE && c.getCategory() == PunishCategory.REPORT).collect(Collectors.toList());

            if (!punishments.isEmpty()) {
                context.sendMessage("§eReportmutes: ");
                for (Punish punish : punishments) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("§7[").append("#").append(punish.getCode()).append(" - ").append(localAccount.getLanguage().getDateFormat().format(new Date(punish.getApplyDate()))).append("] §eMotivo §b").append((localAccount.hasPermission(Rank.ASSISTANT_MOD) ? punish.getReason() + "§b/" : "")).append(punish.getCategory().getDisplay(localAccount.getLanguage()));
                    if (!punish.isPermanent() && !punish.isExpired())
                        stringBuilder.append(" §eExpira em §b").append(DateUtils.formatDifference(punish.getTime(), localAccount.getLanguage(), DateUtils.Style.NORMAL));
                    TextComponent t1 = createTextComponent((punish.isActive() ? "§8[§c-§8] " : ""), null, null, ClickEvent.Action.RUN_COMMAND, "/unpunish " + account.getUsername() + ' ' + punish.getCode() + " -force");
                    TextComponent t2 = createTextComponent(stringBuilder.toString(), null, null, null, null);
                    TextComponent t3 = createTextComponent(localAccount.hasPermission(Rank.ASSISTANT_MOD) ? " §eAdicionado por §6" + punish.getApplier() + (!punish.isActive() ? " §eRemovido em §b" + localAccount.getLanguage().getDateFormat().format(new Date(punish.getUnpunishDate())) + " §epor §6" + punish.getUnpunisher() : "") : "", null, null, null, null);
                    context.getSender().sendMessage(t1, t2, t3);
                }
            } else {
                context.info("command.account.no_punishments", "mute report");
            }
        }),

        FLAGS("flags", Rank.ADMINISTRATOR, (account, context) ->

        {

            for (Flag flag : Flag.values()) {
                context.sendMessage("§b" + flag.getName() + " §e= §6" + account.getFlag(flag));
            }
        }),

        PERMISSION("permission", Rank.ADMINISTRATOR, (account, context) ->

        {


        }),

        FLAG("flag", Rank.ADMINISTRATOR, (account, context) ->

        {
            String[] args = context.getArgs();

            if (args.length == 3) {
                Flag flag = Flag.from(args[2]);

                if (flag == null) {
                    context.info("object.not_found", "Flag");
                    return;
                }

                account.setFlags(account.getData(Columns.FLAGS).getAsInt());
                context.info("command.account.argument.flag.value", flag.getName(), account.getFlag(flag));
            } else if (args.length == 4) {

                Flag flag = Flag.from(args[2]);

                if (flag == null || !context.getAccount().hasPermission(flag.getRank())) {
                    context.info("object.not_found", "Flag");
                    return;
                }

                account.setFlags(account.getData(Columns.FLAGS).getAsInt());

                boolean bool;
                try {
                    bool = Boolean.parseBoolean(args[3].toLowerCase());
                } catch (Exception e) {
                    context.sendMessage("§cUso do /account:");
                    context.sendMessage("§c* /account <user> flag <flag> <true/false>");
                    return;
                }

                if (account.getFlag(flag) == bool) {
                    context.info("command.account.argument.flag.same_value", flag.getName(), bool);
                    return;
                }

                account.setFlag(flag, bool);

                account.getData(Columns.FLAGS).setData(account.getFlags());
                account.getDataStorage().saveColumn(Columns.FLAGS);

                Constants.getRedis().publish(Redis.FLAG_UPDATE_CHANNEL, account.getUniqueId() + ":" + account.getFlags());

                context.info("command.account.argument.flag.success", flag.getName().toLowerCase(), bool);
            } else {
                context.sendMessage("§cUso do /account:");
                context.sendMessage("§c* /account <user> flag <flag> <true/false>");
            }
        }),

        TRUST("trust", Rank.ASSISTANT_MOD, (account, context) ->

        {

            account.getDataStorage().loadIfUnloaded(Columns.ADDRESS);

            if (account.getData(Columns.ADDRESS).getAsString().equals(Columns.ADDRESS.getDefaultValue())) {
                context.sendMessage("§cNenhum endereço IP encontrado.");
                return;
            }

            AddressData addressData = account.getProperty("account_address_data", DataResolver.getInstance().getData(account.getData(Columns.ADDRESS).getAsString())).getAs(AddressData.class);
            addressData.completelyTrust();
            context.info("command.account.trust.success", account.getDisplayName());
        }),

        IPADDRESS("ipaddress", Rank.ADMINISTRATOR, (account, context) ->

        {

            account.loadRanks();

            if (context.getAccount().getRank().getId() <= account.getRank().getId()) {
                context.sendMessage("§cNenhum endereço IP encontrado.");
                return;
            }

            Columns column = Columns.ADDRESS;
            account.getDataStorage().loadIfUnloaded(column);

            if (account.getData(column).getAsString().equals(column.getDefaultValue())) {
                context.sendMessage("§cNenhum endereço IP encontrado.");
                return;
            }

            String address = account.getData(column).getAsString();

            TextComponent textComponent = createTextComponent("§aEndereço IP encontrado: " + address, null, null, ClickEvent.Action.SUGGEST_COMMAND, address);
            context.getSender().sendMessage(textComponent);
        }),

        PASSWORD("password", Rank.ADMINISTRATOR, (account, context) ->

        {

            if (account.getData(Columns.PREMIUM).getAsBoolean()) {
                context.sendMessage("§cEste usuário é registrado no Minecraft.");
                return;
            }

            Columns column = Columns.PASSWORD;
            account.getDataStorage().loadIfUnloaded(column);

            if (account.getData(column).getAsString().equals(column.getDefaultValue())) {
                context.sendMessage("§cNenhuma senha encontrada.");
                return;
            }

            String password = account.getData(column).getAsString();

            TextComponent textComponent = createTextComponent("§aSenha encontrada: " + password, null, null, ClickEvent.Action.SUGGEST_COMMAND, password);
            context.getSender().sendMessage(textComponent);
        }),

        RESETPASS("resetpass", Rank.ADMINISTRATOR, (account, context) ->

        {

            if (account.getData(Columns.PREMIUM).getAsBoolean()) {
                context.sendMessage("§cEste usuário é registrado no Minecraft.");
                return;
            }

            account.getData(Columns.PASSWORD).setData(Columns.PASSWORD.getDefaultValue());
            account.getData(Columns.SESSION_EXPIRES_AT).setData(Columns.SESSION_EXPIRES_AT.getDefaultValue());
            account.getData(Columns.SESSION_ADDRESS).setData(Columns.SESSION_ADDRESS.getDefaultValue());

            account.getDataStorage().saveTable(Tables.AUTH);

            context.sendMessage("§aVocê resetou a senha de " + account.getUsername() + ".");
        }),

        LOGS("logs", Rank.ADMINISTRATOR, (account, context) ->

        {

            context.sendMessage("§aCarregando...");

            account.loadRanks();

            if (context.getAccount().getRank().getId() != Rank.BOB.getId() && account.getRank().getId() == Rank.BOB.getId()) {
                context.sendMessage("§cNenhuma log encontrada.");
                return;
            }

            List<LogData> logDataList = new ArrayList<>();

            try {
                PreparedStatement statement = Constants.getMySQL().getConnection().prepareStatement("SELECT * FROM `logs` WHERE `unique_id` = ?");
                statement.setString(1, account.getUniqueId().toString());

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    logDataList.add(new LogData(account.getUniqueId(), resultSet.getString("nickname"), resultSet.getString("server"), resultSet.getString("content"), LogData.Type.valueOf(resultSet.getString("type")), resultSet.getTimestamp("created_at").toLocalDateTime()));
                }

                if (logDataList.size() == 0) {
                    context.sendMessage("§cNenhuma log encontrada.");
                    return;
                }

                StringBuilder stringBuilder = new StringBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                for (LogData logData : logDataList) {
                    stringBuilder.append("\n[").append(logData.getType().name().toLowerCase()).append("/").append(logData.getServer()).append("/").append(formatter.format(logData.getCreatedAt())).append("] ").append(logData.getNickname()).append(": ").append(logData.getContent());
                }

                String url = ProxyInterface.post(stringBuilder.toString(), true);

                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§6" + logDataList.size() + " §elogs foram carregadas. Para acessar, clique §b§lAQUI"));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                context.getSender().sendMessage(textComponent);
            } catch (Exception e) {
                e.printStackTrace();
                context.sendMessage("§cNenhuma log encontrada.");
            }
        }),

        INFO(null, Rank.PARTNER_PLUS, (account, context) -> {

            Account localAccount = context.getAccount();

            boolean isPremium = account.getData(Columns.PREMIUM).getAsBoolean();

            account.getDataStorage().loadIfUnloaded(Columns.NICK);

            if (!isPremium)
                account.getDataStorage().loadIfUnloaded(Columns.REGISTERED_AT, Columns.SESSION_EXPIRES_AT, Columns.PASSWORD_LAST_UPDATE);

            if (!account.getData(Columns.NICK).getAsString().equals("...") && !account.hasProperty("nickname"))
                account.setProperty("nickname", account.getData(Columns.NICK).getAsString());

            account.loadRanks();
            account.loadPunishments();

            AddressData addressData = account.getProperty("account_address_data", DataResolver.getInstance().getData(account.getData(Columns.ADDRESS).getAsString())).getAs(AddressData.class);

            context.sendMessage("§aUsuário: §f" + account.getUsername());
            context.sendMessage("§aTipo: §f" + (isPremium ? "Premium" : "Cracked"));

            if (account.hasCustomName() && localAccount.getRank().getId() >= account.getRank().getId())
                context.sendMessage("§aNickname: §f" + account.getDisplayName());

            if (account.hasClan()) {
                Clan clan = account.getClan();

                if (clan.isMember(account.getUniqueId()))
                    context.sendMessage("§aClan: " + ChatColor.valueOf(clan.getColor()) + "[" + account.getClan().getTag().toUpperCase() + "]");
            }


            account.getRanks().stream().sorted((a, b) -> Long.compare((int) b.getAddedAt(), (int) a.getAddedAt())).forEach(rankData -> {
                context.sendMessage("§aRank: §f" + rankData.getRank().getName());
                context.sendMessage("  §7Adicionado em: " + localAccount.getLanguage().getDateFormat().format(rankData.getAddedAt()));
                context.sendMessage("  §7Atualizado em: " + localAccount.getLanguage().getDateFormat().format(rankData.getUpdatedAt()));
                if (localAccount.getProperty("isAdmin").getAsBoolean())
                    context.sendMessage("  §7Adicionado por: " + rankData.getAddedBy());
                if (!rankData.isPermanent())
                    context.sendMessage("  §7Expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, rankData.getExpiration()));
            });

            long firstLogin = account.getData(Columns.FIRST_LOGIN).getAsLong(), lastLogin = account.getData(Columns.LAST_LOGIN).getAsLong();

            if (firstLogin != 0L) {
                context.sendMessage("§aPrimeiro login: §f" + localAccount.getLanguage().getDateFormat().format(firstLogin));
                context.sendMessage("§aÚltimo login: §f" + localAccount.getLanguage().getDateFormat().format(lastLogin));
            }

            if (!addressData.getAddress().equals("...")) {
                addressData.print(context);
            }

            if (!isPremium) {
                context.sendMessage("§aAutenticação: ");

                long registeredAt = account.getData(Columns.REGISTERED_AT).getAsLong();
                long sessionExpiration = account.getData(Columns.SESSION_EXPIRES_AT).getAsLong();
                long updatedAt = account.getData(Columns.PASSWORD_LAST_UPDATE).getAsLong();

                if (registeredAt != 0L) {
                    context.sendMessage(" §7Registrado em: " + localAccount.getLanguage().getDateFormat().format(registeredAt));
                    context.sendMessage(" §7Atualizado em: " + localAccount.getLanguage().getDateFormat().format(updatedAt));
                }
                if (System.currentTimeMillis() < sessionExpiration)
                    context.sendMessage(" §7Sessão expira em: " + StringTimeUtils.formatDifference(StringTimeUtils.Type.NORMAL, sessionExpiration));
            }

            List<Punish> activePunishments = new ArrayList<>(account.getActivePunishments());

            if (!activePunishments.isEmpty()) {
                Language lang = localAccount.getLanguage();
                context.sendMessage("§aPunições ativas: ");

                for (Punish punish : activePunishments) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("  §a").append(punish.getType().getName()).append(":\n");
                    stringBuilder.append("§7[").append(lang.getDateFormat().format(new Date(punish.getApplyDate()))).append("] §eMotivo §b");

                    if (localAccount.hasPermission(Rank.ASSISTANT_MOD))
                        stringBuilder.append(punish.getReason()).append("§b/");

                    stringBuilder.append(punish.getCategory().getDisplay(lang));
                    if (!punish.isPermanent() && !punish.isExpired())
                        stringBuilder.append(" §eExpira em §b").append(DateUtils.formatDifference(punish.getTime(), lang, DateUtils.Style.NORMAL));
                    context.sendMessage(stringBuilder.toString());
                }
            }
        });

        private static TextComponent createTextComponent(String name, HoverEvent.Action hoverAction, String hoverDisplay, ClickEvent.Action clickAction, String clickValue) {
            TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(name));
            if (hoverAction != null)
                textComponent.setHoverEvent(new HoverEvent(hoverAction, new TextComponent[]{new TextComponent(hoverDisplay)}));
            if (clickAction != null)
                textComponent.setClickEvent(new ClickEvent(clickAction, clickValue));
            return textComponent;
        }

        private final String arg;
        private final Rank rank;
        private final Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(argument -> argument.getArg() != null && argument.getArg().equalsIgnoreCase(key)).findFirst().orElse(null);
        }
    }
}