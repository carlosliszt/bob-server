package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.BukkitSerialization;
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

public class ExportInventoryCommand implements BukkitInterface {

    @Command(name = "exportinventory", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<Player> context) {
        context.sendMessage("§aCarregando...");
        async(() -> {
            try {
                String url = post(BukkitSerialization.toBase64(context.getSender().getInventory()), true);

                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText("§aInventário exportado."));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                context.getSender().sendMessage(textComponent);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
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

            String postURL = raw ? "https://hastebin.com/share/" : "https://hastebin.com/";
            response = postURL + response;
        }

        return response;
    }

}