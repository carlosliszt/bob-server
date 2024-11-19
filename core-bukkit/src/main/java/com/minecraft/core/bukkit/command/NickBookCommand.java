package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.book.Book;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.core.util.communication.NicknameUpdateData;
import com.minecraft.core.util.skin.Skin;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class NickBookCommand {

    private static final String[] prefix = {"", "y", "_", "u", "i", "__", "e", "z", "x", "Iam", "chorapro"};
    private static final String[] suffix = {"uwu", "", "owo", "__", "_", "2012", "BR", "bw", "34", "1337", "XD"};
    private static final String[] middle = {"Mariaum", "mariaum", "maria1", "coelhoh", "coelho",
            "neymar", "alessia", "drone","fest", "festinha","alan",  "aleeessia", "alexa", "faasty", "Neymar", "lucas", "naruto", "Naruto", "Sasuke", "Matheuszinho", "matheuszinho", "matheus", "bizarro", "ricardinho", "biajoaninha", "bob", "wal"};


    @Command(name = "nickbook", rank = Rank.ULTRA_PLUS, platform = Platform.PLAYER)
    public void onNickBookCommand(Context<Player> ctx) {
        String[] args = ctx.getArgs();
        Account account = ctx.getAccount();
        String nickname = null;

            if(args.length == 1) {
                if(args[0].equalsIgnoreCase("start")) {
                    Book book = new Book("Nick", "hateinblue");
                    Book.PageBuilder p1 = new Book.PageBuilder(book);
                    p1.add("       §l§nNICK: SKIN").build();
                    p1.add("\n\n").build();
                    p1.add("Escolha a skin para seu disfarce.").build();
                    p1.add("\n\n").build();
                    p1.add("➤ Skin Aleatória").hoverEvent(Book.HoverAction.Show_Text, "Skin Aleatória").clickEvent(Book.ClickAction.Run_Command, "/nickbook randomskin").build();
                    p1.add("\n➤ Sua Skin").hoverEvent(Book.HoverAction.Show_Text, "Sua Skin").clickEvent(Book.ClickAction.Run_Command, "/nickbook myskin").build();
                    p1.build();
                    Book.open(ctx.getSender(), book.build(), false);
                } else if(args[0].equalsIgnoreCase("randomskin")) {
                    Book book = new Book("Nick", "hateinblue");
                    Book.PageBuilder p1 = new Book.PageBuilder(book);
                    p1.add("       §l§nNICK: NOME").build();
                    p1.add("\n\n").build();
                    p1.add("Escolha o nome para seu disfarce.").build();
                    p1.add("\n\n").build();
                    p1.add("➤ Aleatório").hoverEvent(Book.HoverAction.Show_Text, "Usar um nome aleatório").clickEvent(Book.ClickAction.Run_Command, "/nickbook randomskin randomnick").build();
                    if(!account.getData(Columns.LAST_NICK).getAsString().equals("...")) {
                        p1.add("\n➤ Anterior: §l" + account.getData(Columns.LAST_NICK).getAsString()).hoverEvent(Book.HoverAction.Show_Text, "Usar o nome anterior: §l" + account.getData(Columns.LAST_NICK).getAsString()).clickEvent(Book.ClickAction.Run_Command, "/nickbook randomskin last").build();
                    }
                    p1.build();
                    Book.open(ctx.getSender(), book.build(), false);

                } else if(args[0].equalsIgnoreCase("myskin")) {
                    Book book = new Book("Nick", "hateinblue");
                    Book.PageBuilder p1 = new Book.PageBuilder(book);
                    p1.add("       §l§nNICK: NOME").build();
                    p1.add("\n\n").build();
                    p1.add("Escolha o nome para seu disfarce.").build();
                    p1.add("\n\n").build();
                    p1.add("➤ Aleatório").hoverEvent(Book.HoverAction.Show_Text, "Usar um nome aleatório").clickEvent(Book.ClickAction.Run_Command, "/nickbook myskin randomnick").build();
                    if(!account.getData(Columns.LAST_NICK).getAsString().equals("...")) {
                        p1.add("\n➤ Anterior: §l" + account.getData(Columns.LAST_NICK).getAsString()).hoverEvent(Book.HoverAction.Show_Text, "Usar o nome anterior: §l" + account.getData(Columns.LAST_NICK).getAsString()).clickEvent(Book.ClickAction.Run_Command, "/nickbook myskin last").build();
                    }
                    p1.build();
                    Book.open(ctx.getSender(), book.build(), false);
                }
            }

            if(args.length == 2) {
                if(args[1].equalsIgnoreCase("randomnick")) {
                    nickname = generateNick();

                    Book book = new Book("Nick", "hateinblue");
                    Book.PageBuilder p1 = new Book.PageBuilder(book);
                    p1.add("       §l§nNICK: NOME").build();
                    p1.add("\n\n").build();
                    p1.add("Nome aleatório gerado!").build();
                    p1.add("\n\n").build();
                    p1.add("§lNOME: §r" + nickname).build();
                    p1.add("\n\n").build();
                    p1.add("          §2§lUSAR").hoverEvent(Book.HoverAction.Show_Text, "§aClique para usar o nome: " + nickname).clickEvent(Book.ClickAction.Run_Command, "/nickbook " + args[0] + " " + args[1] + " accept:" + nickname).build();
                    p1.add("\n  §4§lESCOLHER OUTRO").hoverEvent(Book.HoverAction.Show_Text, "§cClique para escolher um novo disfarce.").clickEvent(Book.ClickAction.Run_Command, "/nickbook " + args[0] + " " + args[1]).build();

                    p1.build();
                    Book.open(ctx.getSender(), book.build(), false);

                } else if(args[1].equalsIgnoreCase("last")) {
                    nickname = account.getData(Columns.LAST_NICK).getAsString();

                    accept("last", ctx, nickname, args[0].equalsIgnoreCase("randomskin"), true);

                }
            }

            if(args.length == 3) {
                if(args[2].startsWith("accept")) {
                    nickname = args[2].split(":")[1];
                    if(nickname != null) {
                        accept("random", ctx, nickname, args[0].equalsIgnoreCase("randomskin"), true);
                    }
                }
            }
    }

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

        Property property;
        if(randomizeSkin) {
            Skin randomSkin = Skin.getValues()[new Random().nextInt(Skin.getValues().length)];
            property = new Property("textures", randomSkin.getCustomProperty().getValue(), randomSkin.getCustomProperty().getSignature());
        } else {
            property = new Property("textures", account.getSkinData().getValue(), account.getSkinData().getSignature());
        }

        Executor.sync(() -> {

            List<Player> playerList = randomizeSkin ? Bukkit.getOnlinePlayers().stream().filter(p -> p != sender && sender.canSee(p)).collect(Collectors.toList()) : null;
            account.setProperty("account_tag", Tag.MEMBER);
            account.setDisplayName(nickname);

            PlayerDisguise.disguise(sender, nickname, property, true);
            PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, Tag.MEMBER, account.getProperty("account_prefix_type").getAs(PrefixType.class));
            Bukkit.getPluginManager().callEvent(event);
            context.info("command.nick.nickname_change", nickname);
            CooldownProvider.getGenericInstance().addCooldown(sender.getUniqueId(), "command.nick", 15, false);
            account.getData(Columns.NICK).setData(nickname);
            account.getData(Columns.TAG).setData(Tag.MEMBER.getUniqueCode());
            account.getData(Columns.LAST_NICK).setData(nickname);

            if (property != null) {

                SkinData skinData = account.getSkinData();

                skinData.setName("command.nick.disguise");
                skinData.setValue(property.getValue());
                skinData.setSignature(property.getSignature());
                skinData.setSource(SkinData.Source.CUSTOM);
                skinData.setUpdatedAt(System.currentTimeMillis());
                account.getData(Columns.SKIN).setData(skinData.toJson());
            }

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


    public static String generateNick() {
        StringBuilder s = new StringBuilder();
        Random r = new Random();

        String p = prefix[r.nextInt(prefix.length)];
        String m = middle[r.nextInt(middle.length)];
        String ss = suffix[r.nextInt(suffix.length)];

        s.append(p).append(m).append(ss);

        return s.substring(0, Math.min(s.length(), 16));
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
