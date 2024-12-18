/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountStorage;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.proxy.ProxyGame;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface ProxyInterface {

    static TextComponent createTextComponent(String name, HoverEvent.Action hoverAction, String hoverDisplay, ClickEvent.Action clickAction, String clickValue) {
        TextComponent textComponent = new TextComponent(name);
        if (hoverAction != null)
            textComponent.setHoverEvent(new HoverEvent(hoverAction, new TextComponent[]{new TextComponent(hoverDisplay)}));
        if (clickAction != null)
            textComponent.setClickEvent(new ClickEvent(clickAction, clickValue));
        return textComponent;
    }

    default void search(Context<?> sender, String username, Consumer<Account> result) {

        if (!Constants.isValid(username)) {
            if (sender != null) {
                sender.info("target.not_found");
            }
            return;
        }

        Account account = AccountStorage.getAccountByName(username, true);

        if (account == null) {
            UUID uniqueId = Constants.getMojangAPI().getUniqueId(username);

            if (uniqueId == null)
                uniqueId = Constants.getCrackedUniqueId(username);

            account = new Account(uniqueId, username);

            DataStorage dataStorage = account.getDataStorage();
            dataStorage.load(Tables.ACCOUNT);

            boolean exists = (Columns.FIRST_LOGIN.getDefaultValue() != dataStorage.getData(Columns.FIRST_LOGIN).getAsLong());

            if (!exists) {
                if (sender != null) {
                    sender.info("target.not_found");
                }
                return;
            }

            account.setUsername(dataStorage.getData(Columns.USERNAME).getAsString());
            dataStorage.getData(Columns.PREMIUM).setData(!uniqueId.equals(Constants.getCrackedUniqueId(username)));
            dataStorage.saveTable(Tables.ACCOUNT);
        }
        result.accept(account);
    }

    static String post(String text, boolean raw) throws IOException {
        byte[] postData = text.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        String requestURL = "https://hastebin.com/documents";
        URL url = new URL(requestURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Hastebin Java Api");
        conn.setRequestProperty("Authorization", "Bearer bfe80c5161ae6c9913449f826153a4569cfe012fe08cfc68ad41eb3f2781577b86bbe04985ca90c956edbf3d1b06e2930fb15f24397f4d3736282ef0df33a550");
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

    default ProxiedPlayer getPlayer(UUID uuid) {
        Account account = Account.fetch(uuid);
        if (account != null)
            return ProxyServer.getInstance().getPlayer(account.getUniqueId());
        return null;
    }

    default ProxiedPlayer getPlayer(String name) {
        Account account = AccountStorage.getAccountByName(name, true);
        if (account != null)
            return ProxyServer.getInstance().getPlayer(account.getUniqueId());
        return null;
    }

    default List<String> getOnlineNicknames(Context<CommandSender> context) {
        Account sender = context.getAccount();
        return Constants.getAccountStorage().getAccounts().stream().filter(account -> account.getUniqueId() != Constants.CONSOLE_UUID && startsWith(account.getDisplayName(), context.getArgs()[context.argsCount() - 1]) && account.getRank().getCategory().getImportance() <= sender.getRank().getCategory().getImportance()).map(Account::getDisplayName).collect(Collectors.toList());
    }

    default void async(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(ProxyGame.getInstance(), runnable);
    }

    default boolean startsWith(String str, String prefix) {
        if (str != null && prefix != null) {
            return prefix.length() <= str.length() && str.regionMatches(true, 0, prefix, 0, prefix.length());
        } else {
            return str == null && prefix == null;
        }
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

    Pattern PATTERN = Pattern.compile("[a-zA-Z0-9_]{6,16}");

}