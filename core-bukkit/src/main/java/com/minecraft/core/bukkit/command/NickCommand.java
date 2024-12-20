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
import com.minecraft.core.bukkit.util.book.Book;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.message.MessageType;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.core.util.communication.NicknameUpdateData;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.core.util.skin.Skin;
import com.minecraft.core.util.skin.util.CustomProperty;
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

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class NickCommand implements BukkitInterface {

    private static final String[] prefix = {"", "y", "_", "u", "i", "__", "e", "z", "x", "Iam", "chorapro"};
    private static final String[] suffix = {"uwu", "", "owo", "__", "_", "2012", "BR", "34", "bw", "1337", "XD"};
    private static final String[] middle = {"Mariaum", "mariaum", "maria1", "coelhoh", "coelho",
            "neymar", "alessia", "drone", "aleeessia", "alexa", "fest", "festinha", "alan", "faasty", "Neymar", "lucas", "naruto", "Naruto", "Sasuke", "Matheuszinho", "matheuszinho", "matheus", "bizarro", "ricardinho", "biajoaninha", "bob", "wal"};

    private static void accept(String subcommand, Context<Player> context, String nickname, boolean randomizeSkin, boolean checkMojang) {
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
            account.getData(Columns.LAST_NICK).setData(nickname);

            Executor.async(() -> {
                if (!subcommand.equalsIgnoreCase("last")) {
                    try {
                        account.loadNicks();
                        long currentTime = System.currentTimeMillis();
                        long expiryTime = StringTimeUtils.parseDateDiff("1d", true);
                        account.addNick(nickname, currentTime, expiryTime);

                        account.getData(Columns.NICK_OBJECTS).setChanged(true);
                        account.getDataStorage().saveColumn(Columns.NICK_OBJECTS);

                        account.getDataStorage().saveTable(Tables.ACCOUNT, Tables.OTHER);

                        NicknameUpdateData data = new NicknameUpdateData(account.getUniqueId(), nickname, System.currentTimeMillis(), StringTimeUtils.parseDateDiff("1d", true));
                        try (Jedis redis = Constants.getRedis().getResource()) {
                            redis.publish(Redis.NICK_ADD_CHANNEL, Constants.GSON.toJson(data));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try (Jedis redis = Constants.getRedis().getResource()) {
                    redis.publish(Redis.NICK_DISGUISE_CHANNEL, account.getUniqueId() + ":" + nickname);
                    if (property != null) {
                        redis.publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + property.getValue() + ":" + property.getSignature());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    account.getDataStorage().saveColumnsFromSameTable(Columns.NICK, Columns.LAST_NICK, Columns.TAG, Columns.SKIN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });
    }

    private static Property get(Player playerBukkit) {
        EntityPlayer playerNMS = ((CraftPlayer) playerBukkit).getHandle();
        GameProfile profile = playerNMS.getProfile();
        Property property = profile.getProperties().get("textures").iterator().next();
        return new Property("textures", property.getValue(), property.getSignature());
    }

    @Command(name = "nick", platform = Platform.PLAYER, rank = Rank.ULTRA_PLUS)
    public void handleCommand(Context<Player> context) {
        String[] args = context.getArgs();

        Player sender = context.getSender();
        Account account = context.getAccount();

        boolean hasReachedLimited = account.getRank() == Rank.ULTRA_PLUS && account.getUnexpiredNicks().size() >= 3;

        if (args.length == 0) {
            Book book = new Book("Nick", "hateinblue");
            Book.PageBuilder p1 = new Book.PageBuilder(book);
            p1.add("            §l§nNICK").build();
            p1.add("\n\n").build();
            p1.add("Escolha um disfarce para usar no servidor.").build();
            p1.add("\n\n").build();
            p1.add("§lATUAL§r: " + (context.getAccount().getData(Columns.NICK).getAsString().equals("...") ? "(Nenhum)" : context.getAccount().getData(Columns.NICK).getAsString())).build();
            p1.add("\n§lLIMITE: §r" + (context.getAccount().getRank().ordinal() <= Rank.PARTNER.ordinal() ? "§oIlimitado" : (hasReachedLimited ? "§7§m(3/3)" : "(" + account.getUnexpiredNicks().size() + "/3)"))).build();
            p1.add("\n\n").build();
            p1.add("        §2§lESCOLHER").hoverEvent(Book.HoverAction.Show_Text, "§aClique para escolher um disfarce.").clickEvent(Book.ClickAction.Run_Command, "/nickbook start").build();

            if (context.getAccount().getData(Columns.NICK).getAsString() != "...")
                p1.add("\n        §4§lREMOVER").hoverEvent(Book.HoverAction.Show_Text, "§cClique para remover o disfarce.").clickEvent(Book.ClickAction.Run_Command, "/nick reset").build();
            p1.build();
            Book.open(context.getSender(), book.build(), false);
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

        LAST("last", Rank.ULTRA_PLUS, (account, context) -> {
            accept("last", context, account.getData(Columns.LAST_NICK).getAsString(), true, false);
        }),

        RANDOM("random", Rank.ULTRA_PLUS, (account, context) -> {

            if (account.getFlag(Flag.NICK)) {
                context.info("flag.locked");
                return;
            }

            boolean hasReachedLimited = account.getRank() == Rank.ULTRA_PLUS && account.getUnexpiredNicks().size() >= 3;

            if(hasReachedLimited) {
                context.sendMessage("§cVocê já usou todos os seus disfarces hoje.");
                return;
            }

            Executor.async(() -> {
                String nickname = generateNick();
                Executor.async(() -> accept("random", context, nickname, true, false));

            });
        }),

        RESET("reset", Rank.ULTRA_PLUS, (account, context) -> {

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

            if (account.getSkinData().getSource() == SkinData.Source.ACCOUNT) {
                return;
            }

            Executor.async(() -> {

                Property property;

                if (Constants.getCrackedUniqueId(account.getUsername()).equals(account.getUniqueId())) {
                    CustomProperty customProperty = Skin.getRandomSkin().getCustomProperty();
                    property = new Property(customProperty.getName(), customProperty.getValue(), customProperty.getSignature());
                } else {
                    property = Constants.getMojangAPI().getProperty(account.getUniqueId());
                }

                if (property == null) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                SkinData skinData = account.getSkinData();

                skinData.setName(account.getUsername());
                skinData.setValue(property.getValue());
                skinData.setSignature(property.getSignature());
                skinData.setSource(SkinData.Source.ACCOUNT);
                skinData.setUpdatedAt(System.currentTimeMillis());

                account.getData(Columns.SKIN).setData(skinData.toJson());
                account.getDataStorage().saveColumn(Columns.SKIN);

                Executor.sync(() -> {
                    PlayerDisguise.changeSkin(context.getSender(), property);
                });
                Constants.getRedis().publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + property.getValue() + ":" + property.getSignature());
            });

        }),

        LIST("list", Rank.PARTNER_PLUS, (account, context) -> {

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

        SKIN("skin", Rank.PARTNER, (account, context) -> {

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

        DEFAULT(null, Rank.PARTNER, (account, context) -> {

            if (account.getFlag(Flag.NICK) || account.getFlag(Flag.NICK_CHOOSE)) {
                context.info("flag.locked");
                return;
            }

            Executor.async(() -> accept("none", context, context.getArg(0), false, true));
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

    public static String generateNick() {
        StringBuilder s = new StringBuilder();
        Random r = new Random();

        String p = prefix[r.nextInt(prefix.length)];
        String m = middle[r.nextInt(middle.length)];
        String ss = suffix[r.nextInt(suffix.length)];

        s.append(p).append(m).append(ss);

        return s.substring(0, Math.min(s.length(), 16));
    }
}