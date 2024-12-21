/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.translation.Language;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public interface BukkitInterface {

    default TextComponent createTextComponent(String name, HoverEvent.Action hoverAction, String hoverDisplay, ClickEvent.Action clickAction, String clickValue) {
        TextComponent textComponent = new TextComponent(name);
        textComponent.setHoverEvent(new HoverEvent(hoverAction, new TextComponent[]{new TextComponent(hoverDisplay)}));
        textComponent.setClickEvent(new ClickEvent(clickAction, clickValue));
        return textComponent;
    }

    default List<String> getOnlineNicknames(Context<CommandSender> context) {
        return Constants.getAccountStorage().getAccounts().stream().filter(account -> account.getUniqueId() != Constants.CONSOLE_UUID && startsWith(account.getDisplayName(), context.getArgs()[context.argsCount() - 1]) && canSee(context, account.getUniqueId())).map(Account::getDisplayName).collect(Collectors.toList());
    }

    default boolean canSee(Context<CommandSender> context, UUID check) {
        Player target = Bukkit.getPlayer(check);
        return !context.isPlayer() || target != null && ((Player) context.getSender()).canSee(target);
    }

    static boolean nameInUse(String name) throws SQLException {
        String SQL = "SELECT accounts.index FROM `accounts` accounts LEFT JOIN `other` other ON accounts.unique_id = other.unique_id WHERE accounts.username='" + name + "' OR other.nick='" + name + "'";
        PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(SQL);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean result = resultSet.next();
        preparedStatement.close();
        resultSet.close();
        return result;
    }

    default void sync(Runnable runnable) {
        MinecraftServer.getServer().postToMainThread(runnable);
    }

    default void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitGame.getEngine(), runnable);
    }

    default void run(Runnable runnable, long l) {
        Bukkit.getScheduler().runTaskLater(BukkitGame.getEngine(), runnable, l);
    }

    default boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

     static String generateNick() {
        StringBuilder s = new StringBuilder();
        Random r = new Random();

        List<String> prefixes;
        try (InputStream inputStream = BukkitInterface.class.getResourceAsStream("/nick_prefixes.txt")) {
            if (inputStream == null) {
                System.err.println("Prefix file not found: nick_prefixes.txt");
                return null;
            }
            prefixes = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))).lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<String> suffixes;
        try (InputStream inputStream = BukkitInterface.class.getResourceAsStream("/nick_suffixes.txt")) {
            if (inputStream == null) {
                System.err.println("Suffix file not found: nick_suffixes.txt");
                return null;
            }
            suffixes = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))).lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<String> middles;
        try (InputStream inputStream = BukkitInterface.class.getResourceAsStream("/nick_middles.txt")) {
            if (inputStream == null) {
                System.err.println("Middle file not found: nick_middles.txt");
                return null;
            }
            middles = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))).lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String p = prefixes.get(r.nextInt(prefixes.size()));
        String sfx = suffixes.get(r.nextInt(suffixes.size()));
        String m = middles.get(r.nextInt(middles.size()));

        s.append(p);
        if(r.nextBoolean())
            s.append(m.substring(0, 1).toUpperCase());
        else
            s.append(m.substring(0, 1).toLowerCase());
        s.append(m.substring(1));
        s.append(sfx);

        return s.substring(0, Math.min(s.length(), 16));
    }

    List<String> booleanOptions = Arrays.asList("true", "false", "on", "off");

    default boolean isBoolean(String str) {
        return booleanOptions.contains(str.toLowerCase());
    }

    default boolean startsWith(String str, String prefix) {
        if (str != null && prefix != null) {
            return prefix.length() <= str.length() && str.regionMatches(true, 0, prefix, 0, prefix.length());
        } else {
            return str == null && prefix == null;
        }
    }

    default <T extends Collection<? super String>> T copyPartialMatches(String token, Iterable<String> originals, T collection) {
        for (String string : originals) {
            if (multiEqualsIgnoreCase(string, token)) {
                collection.add(string);
            }
        }
        return collection;
    }

    default int absolute(int i) {
        return Math.abs(i);
    }

    default int absolute(Double i) {
        return Math.abs(i.intValue());
    }

    default boolean multiEqualsIgnoreCase(String input, String... values) {
        for (String value : values) {
            if (input.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    int[] NUMBERS = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

    default String numural(int number) {
        for (int i = 0; i < NUMBERS.length; i++) {
            if (number >= NUMBERS[i]) {
                return SYMBOLS[i] + numural(number - NUMBERS[i]);
            }
        }
        return "";
    }

    default int unnumural(String number) {
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (number.startsWith(SYMBOLS[i])) {
                return NUMBERS[i] + unnumural(number.replaceFirst(SYMBOLS[i], ""));
            }
        }
        return 0;
    }

    default String createArgs(final int index, final String[] args, final String defaultArgs, final boolean color) {
        final StringBuilder sb = new StringBuilder();
        for (int i = index; i < args.length; ++i) {
            sb.append(args[i]).append((i + 1 >= args.length) ? "" : " ");
        }
        if (sb.length() == 0) {
            sb.append(defaultArgs);
        }
        return color ? ChatColor.translateAlternateColorCodes('&', sb.toString()) : sb.toString();
    }

    default String format(int i) {
        int minutes = i / 60;
        int hours = minutes / 60;
        int seconds = i % 60;
        minutes = (minutes - 60 * hours);
        return (hours > 0 ? hours + ":" : "") + (minutes < 10 && minutes != 0 && hours > 0 ? "0" : minutes == 0 && hours > 0 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    default void log(Account account, String message) {
        List<Account> receivers = new ArrayList<>(Constants.getAccountStorage().getAccounts());
        receivers.removeIf(accounts -> account.getUniqueId().equals(accounts.getUniqueId()) || accounts.getRank().getId() < Rank.PRIMARY_MOD.getId() || !accounts.getPreference(Preference.STAFFLOG));

        Player player = Bukkit.getPlayer(account.getUniqueId());

        receivers.forEach(receiver -> {
            Player administrator = Bukkit.getPlayer(receiver.getUniqueId());
            if (administrator == null)
                return;
            if (player != null && !administrator.getWorld().getUID().equals(player.getWorld().getUID()))
                return;
            if (account.getRank().getId() > Account.fetch(administrator.getUniqueId()).getRank().getId())
                return;
            administrator.sendMessage("§7§o[" + message + "]");
        });

        receivers.clear();
    }

    default int randomize(int min, int max) {
        int random_int = nextInt(min, max);
        return Constants.RANDOM.nextBoolean() ? random_int : -random_int;
    }

    default int nextInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    default int randomize(int x) {
        if (x <= 0)
            return 0;
        return randomize(1, x);
    }

    default void broadcast(String key, Object... values) {
        String BR = Language.PORTUGUESE.translate(key, values), US = Language.ENGLISH.translate(key, values);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(player.getUniqueId());

            if (account == null)
                continue;

            player.sendMessage(account.getLanguage() == Language.ENGLISH ? US : BR);
        }
    }

    default void refreshTablist(Account account) {
        PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class));
        Bukkit.getPluginManager().callEvent(event);
    }

    default String post(String text, boolean raw) throws IOException {
        byte[] postData = text.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        String requestURL = "https://hastebin.com/documents";
        URL url = new URL(requestURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer bfe80c5161ae6c9913449f826153a4569cfe012fe08cfc68ad41eb3f2781577b86bbe04985ca90c956edbf3d1b06e2930fb15f24397f4d3736282ef0df33a550");
        conn.setRequestProperty("User-Agent", "Hastebin Java Api");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);

        String response = null;
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.contains("\"key\"")) {
            response = response.substring(response.indexOf(":") + 2, response.length() - 2);

            String postURL = raw ? "https://hastebin.com/raw/" : "https://hastebin.com/";
            response = postURL + response;
        }

        return response;
    }

}