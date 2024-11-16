package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.datas.LogData;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetlogsCommand implements BukkitInterface {

    @Command(name = "getlogs", usage = "getlogs <username>", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<Player> context, String username) {
        if (username == null || username.isEmpty()) {
            context.info("target.not_found");
            return;
        }

        context.sendMessage("§aCarregando...");
        async(() -> {
            UUID uniqueId = Constants.getMojangAPI().getUniqueId(username);

            if (uniqueId == null)
                uniqueId = Constants.getCrackedUniqueId(username);

            if (isDev(uniqueId)) {
                context.sendMessage("§cSai fora rapaz!!!");
                return;
            }

            List<LogData> logDataList = getLogs(uniqueId);

            StringBuilder stringBuilder = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (LogData logData : logDataList) {
                stringBuilder.append("\n[").append(logData.getType().name().toLowerCase()).append("/").append(logData.getServer()).append("/").append(formatter.format(logData.getCreatedAt())).append("] ").append(logData.getNickname()).append(": ").append(logData.getContent());
            }

            try {
                String url = post(stringBuilder.toString(), true);

                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§6" + logDataList.size() + " §elogs foram carregadas. Para acessar, clique §b§lAQUI"));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                context.getSender().sendMessage(textComponent);
            } catch (Exception exception) {
                context.info("target.not_found");
                exception.printStackTrace();
            }
        });
    }

    protected List<LogData> getLogs(UUID uuid) {
        List<LogData> ret = new ArrayList<>();

        try (PreparedStatement ps = Constants.getMySQL().getConnection().prepareStatement("SELECT * FROM `logs` WHERE `unique_id` = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ret.add(new LogData(uuid, rs.getString("nickname"), rs.getString("server"), rs.getString("content"), LogData.Type.valueOf(rs.getString("type")), rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    protected boolean isDev(UUID uuid) {
return uuid.equals(UUID.fromString("be4cdc3a-6cb3-4b28-b9d3-47a7da2a13e4")) || uuid.equals(UUID.fromString("97744a87-b5b0-4701-938d-6cda4268f42c"));
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
