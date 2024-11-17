package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PckgstatCommand implements BukkitInterface {

    @Command(name = "tagstat", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<Player> context, String tag) {
        if (tag == null || tag.isEmpty()) {
            context.sendMessage("§cTag não encontrada.");
            return;
        }

        Tag tag1 = Tag.fromUsages(tag);

        if (tag1 == null) {
            context.sendMessage("§cTag não encontrada.");
            return;
        }

        context.sendMessage("§aCarregando...");
        async(() -> {
            final String SQL = "SELECT username FROM `accounts` WHERE `tags` REGEXP '" + tag1.getUniqueCode() + "'";

            try {
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(SQL);
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder stringBuilder = new StringBuilder();

                int size = 0;
                while (resultSet.next()) {
                    stringBuilder.append(resultSet.getString("username")).append("\n");
                    size++;
                }

                String url = post(stringBuilder.toString(), true);

                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§a" + size + " usuários possuem essa tag."));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                context.getSender().sendMessage(textComponent);

                preparedStatement.close();
                resultSet.close();
            } catch (Exception exception) {
                context.sendMessage("§cHouve um problema com o serviço de busca.");
            }
        });
    }

    @Command(name = "rankstat", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleRankCommand(Context<Player> context, String rank) {
        if (rank == null || rank.isEmpty()) {
            context.sendMessage("§cTag não encontrada.");
            return;
        }

        Rank rank1 = Rank.fromString(rank);

        if (rank1 == null) {
            context.sendMessage("§cTag não encontrada.");
            return;
        }

        context.sendMessage("§aCarregando...");
        async(() -> {
            final String SQL = "SELECT username FROM `accounts` WHERE `ranks` REGEXP '" + rank1.getUniqueCode() + "'";

            try {
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(SQL);
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder stringBuilder = new StringBuilder();

                int size = 0;
                while (resultSet.next()) {
                    stringBuilder.append(resultSet.getString("username")).append("\n");
                    size++;
                }

                String url = post(stringBuilder.toString(), true);

                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§a" + size + " usuários possuem esse rank."));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                context.getSender().sendMessage(textComponent);

                preparedStatement.close();
                resultSet.close();
            } catch (Exception exception) {
                context.sendMessage("§cHouve um problema com o serviço de busca.");
            }
        });
    }

    @Command(name = "medalstat", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleMedalCommand(Context<Player> context, String medal) {
        if (medal == null || medal.isEmpty()) {
            context.sendMessage("§cMedalha não encontrada.");
            return;
        }

        Medal medal1 = Medal.fromString(medal);

        if (medal1 == null) {
            context.sendMessage("§cMedalha não encontrada.");
            return;
        }

        context.sendMessage("§aCarregando...");
        async(() -> {
            final String SQL = "SELECT username FROM `accounts` WHERE `medals` REGEXP '" + medal1.getUniqueCode() + "'";

            try {
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(SQL);
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder stringBuilder = new StringBuilder();

                int size = 0;
                while (resultSet.next()) {
                    stringBuilder.append(resultSet.getString("username")).append("\n");
                    size++;
                }

                String url = post(stringBuilder.toString(), true);

                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§a" + size + " usuários possuem essa medalha"));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                context.getSender().sendMessage(textComponent);

                preparedStatement.close();
                resultSet.close();
            } catch (Exception exception) {
                context.sendMessage("§cHouve um problema com o serviço de busca.");
            }
        });
    }

    @Completer(name = "tagstat")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return Arrays.stream(Tag.getValues()).map(tag -> tag.getName().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Completer(name = "rankstat")
    public List<String> handleRankComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return Arrays.stream(Rank.getValues()).map(rank -> rank.getName().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Completer(name = "medalstat")
    public List<String> handleMedalComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return Arrays.stream(Medal.getValues()).map(rank -> rank.getName().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    public String post(String text, boolean raw) throws IOException {
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

}