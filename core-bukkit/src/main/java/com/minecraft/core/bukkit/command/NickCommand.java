/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.util.ranking.RankingFactory;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class NickCommand implements BukkitInterface {

    private static void accept(Context<Player> context, String nickname, boolean randomizeSkin, boolean checkMojang) {
        Player sender = context.getSender();
        Account account = context.getAccount();

        if (!context.getAccount().hasPermission(Rank.PRIMARY_MOD) && CooldownProvider.getGenericInstance().hasCooldown(sender, "command.nick")) {
            Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(context.getUniqueId(), "command.nick");
            if (cooldown != null && !cooldown.expired()) {
                context.info("wait_generic", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining()));
                return;
            }
        }

        if (!Constants.isValid(nickname)) {
            context.info("invalid_name", "[a-zA-Z0-9_]{3,16}");
            return;
        }

        if (Bukkit.getPlayerExact(nickname) != null) {
            context.info("command.nick.name_already_in_use", "Online");
            return;
        }

        if (checkMojang) {
            if (Constants.getMojangAPI().getUniqueId(nickname) != null) {
                context.info("command.nick.name_already_in_use", "Mojang");
                return;
            }

            try {
                if (BukkitInterface.nameInUse(nickname)) {
                    context.info("command.nick.name_already_in_use", "Database");
                    return;
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                context.info("unexpected_error");
                return;
            }
        }

        Executor.sync(() -> {

            List<Player> playerList = randomizeSkin ? Bukkit.getOnlinePlayers().stream().filter(p -> p != sender && sender.canSee(p)).collect(Collectors.toList()) : null;
            Property property = playerList == null || playerList.isEmpty() ? null : get(playerList.get(Constants.RANDOM.nextInt(playerList.size())));
            account.setProperty("account_tag", Tag.MEMBER);
            account.setDisplayName(nickname);

            if (property != null) {

                SkinData skinData = account.getSkinData();

                skinData.setName("command.nick.disguise");
                skinData.setValue(property.getValue());
                skinData.setSignature(property.getSignature());
                skinData.setSource(SkinData.Source.CUSTOM);
                skinData.setUpdatedAt(System.currentTimeMillis());
                account.getData(Columns.SKIN).setData(skinData.toJson());
            }

            PlayerDisguise.disguise(sender, nickname, property, true);
            PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, Tag.MEMBER, account.getProperty("account_prefix_type").getAs(PrefixType.class));
            Bukkit.getPluginManager().callEvent(event);
            context.info("command.nick.nickname_change", nickname);
            CooldownProvider.getGenericInstance().addCooldown(sender.getUniqueId(), "command.nick", 15, false);
            account.getData(Columns.NICK).setData(nickname);
            account.getData(Columns.TAG).setData(Tag.MEMBER.getUniqueCode());
            Executor.async(() -> {
                try (Jedis redis = Constants.getRedis().getResource()) {
                    redis.publish(Redis.NICK_DISGUISE_CHANNEL, account.getUniqueId() + ":" + nickname);
                    if (property != null)
                        redis.publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + property.getValue() + ":" + property.getSignature());
                }
                account.getDataStorage().saveColumnsFromSameTable(Columns.NICK, Columns.TAG, Columns.SKIN);
            });
        });
    }

    private static Property get(Player playerBukkit) {
        EntityPlayer playerNMS = ((CraftPlayer) playerBukkit).getHandle();
        GameProfile profile = playerNMS.getProfile();
        Property property = profile.getProperties().get("textures").iterator().next();
        return new Property("textures", property.getValue(), property.getSignature());
    }

    @Command(name = "nick", platform = Platform.PLAYER, rank = Rank.ELITE)
    public void handleCommand(Context<Player> context) {
        String[] args = context.getArgs();

        Player sender = context.getSender();
        Account account = context.getAccount();

        if (args.length == 0) {
            sender.sendMessage("§cUso do /nick:");
            sender.sendMessage("§c* /nick <nickname>");
            sender.sendMessage("§c* /nick random");
            sender.sendMessage("§c* /nick reset");
            sender.sendMessage("§c* /nick skin <skin>");
            return;
        }

        Argument argument = Argument.get(args[0]);

        if (account.hasPermission(argument.getRank()))
            argument.getExecutor().execute(account, context);
        else
            context.info(MessageType.NO_PERMISSION.getMessageKey());
    }

    @Completer(name = "nick")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            Account account = context.getAccount();
            return Arrays.stream(Argument.values()).filter(argument -> argument.getArg() != null && startsWith(argument.getArg(), context.getArg(0)) && account.hasPermission(argument.getRank())).map(Argument::getArg).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Getter
    @AllArgsConstructor
    enum Argument {

        RANDOM("random", Rank.ELITE, (account, context) -> {

            if (account.getFlag(Flag.NICK)) {
                context.info("flag.locked");
                return;
            }

            final int maxLength = 13;
            final String sql = "SELECT `username` FROM `accounts` WHERE `premium`='true' AND`banned`='true' AND `username` IS NOT NULL AND LENGTH(`username`) >= 3 AND LENGTH(`username`) <= " + maxLength + " AND (username != \"...\") ORDER BY RAND() LIMIT 1";
            Executor.async(() -> {
                try {
                    PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(sql);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (!resultSet.next()) {
                        context.info("unexpected_error");
                        preparedStatement.close();
                        resultSet.close();
                        return;
                    }

                    String nickname = resultSet.getString(1);
                    Executor.async(() -> accept(context, nickname, true, false));
                    preparedStatement.close();
                    resultSet.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    context.info("unexpected_error");
                }
            });
        }),

        RESET("reset", Rank.ELITE, (account, context) -> {

            if (account.getFlag(Flag.NICK)) {
                context.info("flag.locked");
                return;
            }

            if (!account.hasCustomName()) {
                context.info("command.nick.no_custom_name");
                return;
            }

            if (BukkitGame.getEngine().getRankingFactory() != null)
                account.setRanking(Ranking.fromId(account.getData(BukkitGame.getEngine().getRankingFactory().getTarget().getRanking()).getAsInt()));

            Tag tag = account.getTagList().getHighestTag();
            account.setProperty("account_tag", tag);
            PlayerDisguise.changeNickname(context.getSender(), account.getUsername());

            Bukkit.getPluginManager().callEvent(new PlayerUpdateTablistEvent(account, tag, account.getProperty("account_prefix_type").getAs(PrefixType.class)));

            account.removeProperty("nickname");
            account.getData(Columns.NICK).setData("...");
            account.getData(Columns.TAG).setData(tag.getUniqueCode());

            RankingFactory rankingFactory = BukkitGame.getEngine().getRankingFactory();

            if (rankingFactory != null) {
                account.setRanking(Ranking.fromId(account.getData(rankingFactory.getTarget().getRanking()).getAsInt()));
                rankingFactory.verify(account);
            }

            Executor.async(() -> {
                Constants.getRedis().publish(Redis.NICK_DISGUISE_CHANNEL, account.getUniqueId() + ":" + account.getUsername());
                account.getDataStorage().saveColumnsFromSameTable(Columns.NICK, Columns.TAG);
            });

            context.info("command.nick.nickname_remove");
        }),

        LIST("list", Rank.STREAMER_PLUS, (account, context) -> {

            List<Account> accountList = new ArrayList<>(Constants.getAccountStorage().getAccounts());
            accountList.removeIf(accounts -> {

                if (!accounts.hasCustomName())
                    return true;

                if (account.getRank().getId() < accounts.getRank().getId())
                    return true;

                Player player = Bukkit.getPlayer(accounts.getUniqueId());

                if (player == null)
                    return true;

                return !context.getSender().canSee(player);
            });

            if (accountList.isEmpty()) {
                context.info("command.nick.list.empty");
                return;
            }

            context.info("command.nick.list.title");
            accountList.forEach(accounts -> context.sendMessage("§7- §6" + accounts.getDisplayName() + "§e=§b" + accounts.getUsername()));
        }),

        SKIN("skin", Rank.SHORTS, (account, context) -> {

            if (account.getFlag(Flag.SKIN)) {
                context.info("flag.locked");
                return;
            }

            String[] args = context.getArgs();

            if (args.length != 2) {
                context.info("command.usage", "/nick skin <skin>");
                return;
            }

            String skin = args[1];

            if (!Constants.isValid(skin)) {
                context.info("object.not_found", "Skin");
                return;
            }

            Executor.async(() -> {
                UUID uniqueId = Constants.getMojangAPI().getUniqueId(skin);

                if (uniqueId == null) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                Property property = Constants.getMojangAPI().getProperty(uniqueId);

                if (property == null) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                SkinData skinData = account.getSkinData();

                skinData.setName(skin);
                skinData.setValue(property.getValue());
                skinData.setSignature(property.getSignature());
                skinData.setSource(SkinData.Source.CUSTOM);
                skinData.setUpdatedAt(System.currentTimeMillis());

                account.getData(Columns.SKIN).setData(skinData.toJson());
                account.getDataStorage().saveColumn(Columns.SKIN);

                Executor.sync(() -> {
                    PlayerDisguise.changeSkin(context.getSender(), property);
                    context.info("command.nick.skin_change", skin);
                    if (account.getVersion() < 47)
                        context.info("command.nick.skin_change.legacy_version.warning");
                });

                Constants.getRedis().publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + property.getValue() + ":" + property.getSignature());
            });
        }),

        DEFAULT(null, Rank.ELITE, (account, context) -> {

            if (account.getFlag(Flag.NICK) || account.getFlag(Flag.NICK_CHOOSE)) {
                context.info("flag.locked");
                return;
            }

            Executor.async(() -> accept(context, context.getArg(0), false, true));
        });

        private final String arg;
        private final Rank rank;
        private final Executor executor;

        private static Argument get(String key) {
            return Arrays.stream(values()).filter(argument -> argument.getArg() != null && argument.getArg().equalsIgnoreCase(key)).findFirst().orElse(DEFAULT);
        }
    }

    private interface Executor {
        static void sync(Runnable runnable) {
            Bukkit.getScheduler().runTask(BukkitGame.getEngine(), runnable);
        }

        static void async(Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(BukkitGame.getEngine(), runnable);
        }

        void execute(Account account, Context<Player> context);

    }
}