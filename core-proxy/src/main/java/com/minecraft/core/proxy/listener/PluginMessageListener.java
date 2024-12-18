/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.account.fields.Property;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.event.PunishAssignEvent;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.proxy.util.player.PlayerPingHistory;
import com.minecraft.core.proxy.util.server.ServerAPI;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.packet.ServerPayload;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.core.util.anticheat.AntiCheatAlert;
import com.minecraft.core.util.anticheat.information.Information;
import com.minecraft.core.util.geodata.DataResolver;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.checkerframework.checker.units.qual.C;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;

public class PluginMessageListener implements Listener, ProxyInterface {

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {

        if (!(event.getReceiver() instanceof ProxiedPlayer))
            return;

        switch (event.getTag()) {
            case "Redirection": {

                ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();

                if (ServerAPI.getInstance().hasPendingConnection(player))
                    return;

                DataInputStream stream = new DataInputStream(new ByteArrayInputStream(event.getData()));

                async(() -> {
                    try {

                        ServerRedirect serverRedirect = Constants.GSON.fromJson(stream.readUTF(), ServerRedirect.class);

                        ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(serverRedirect.getUniqueId());
                        Server server = serverRedirect.getRoute().getServer();

                        if (proxiedPlayer == null || server == null || server.isDead() || proxiedPlayer.getServer().getInfo().getName().equals(server.getName())) {
                            return;
                        }

                        Account account = Account.fetch(proxiedPlayer.getUniqueId());

                        if (account == null) {
                            proxiedPlayer.disconnect(TextComponent.fromLegacyText("§cNão foi possível processar sua conexão."));
                            return;
                        }

                        ServerPayload payload = server.getBreath();

                        if (!account.hasPermission(Rank.VIP) && payload.getOnlinePlayers() >= payload.getMaxPlayers()) {
                            proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("server_is_full", Constants.SERVER_STORE)));
                            return;
                        }

                        BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§aConnecting §f" + proxiedPlayer.getName() + "§a to §f" + server.getName()));

                        String route = serverRedirect.getRoute().getInternalRoute();

                        if (route != null && !route.isEmpty()) {
                            proxiedPlayer.sendMessage(TextComponent.fromLegacyText(account.getLanguage().translate("arcade.room.found")));

                            try (Jedis redis = Constants.getRedis().getResource()) {
                                redis.setex("route:" + proxiedPlayer.getUniqueId(), 5, route);
                            }
                        }

                        proxiedPlayer.connect(BungeeCord.getInstance().getServerInfo(server.getName()));
                        server.getBreath().overrideOnlineCount(server.getBreath().getOnlinePlayers() + 1);
                    } catch (IOException e) {
                        player.sendMessage(TextComponent.fromLegacyText("§cNenhuma sala encontrada."));
                        e.printStackTrace();
                    }
                });
                break;
            }
            case "AntiCheat": {
                DataInputStream stream = new DataInputStream(new ByteArrayInputStream(event.getData()));
                try {
                    AntiCheatAlert alert = Constants.GSON.fromJson(stream.readUTF(), AntiCheatAlert.class);
                    notify(alert);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "Auth": {
                ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                Account account = Account.fetch(player.getUniqueId());
                account.setProperty("captcha_successful", true);
                break;
            }
        }
    }

    private void notify(AntiCheatAlert alert) {

        UUID alertUniqueId = UUID.nameUUIDFromBytes((alert.getDisplayName().toLowerCase()).getBytes(StandardCharsets.UTF_8));
        Account account = Account.fetch(alert.getTarget());
        String opening = "§c%sº aviso. §a%s Report:§f %s %s"; //String opening = "§c%sº aviso. §eReport §a%s §7falhou no teste de §a%s §8%s";
        StringBuilder infoBuilder = new StringBuilder("\n§eInfo {");
        StringBuilder reason = new StringBuilder(alert.getDisplayName() + " ");

        EmbedBuilder acb = new EmbedBuilder();

        acb.addField(new MessageEmbed.Field("Servidor", BungeeCord.getInstance().getPlayer(account.getUniqueId()).getServer().getInfo().getName(), false));

        Iterator<Information> informationIterator = alert.getInformations().iterator();

        if (!informationIterator.hasNext()) {
            PlayerPingHistory pingHistory = account.getProperty("pings", new PlayerPingHistory()).getAs(PlayerPingHistory.class);
            reason.append("ping=").append(pingHistory.getMinimum()).append("/").append(pingHistory.getAverage()).append("/").append(pingHistory.getMaximum());
            infoBuilder.append("§6ping§e=§b").append(pingHistory.getMinimum()).append("/").append(pingHistory.getAverage()).append("/").append(pingHistory.getMaximum()).append("§e}");
            acb.addField(new MessageEmbed.Field("Ping", pingHistory.getMinimum() + "/" + pingHistory.getAverage() + "/" + pingHistory.getMaximum(), false));
        }

        while (informationIterator.hasNext()) {
            Information information = informationIterator.next();
            reason.append(information.getDisplayName()).append("=").append(information.getValue());
            infoBuilder.append("§6" + information.getDisplayName()).append("§e=").append("§b" + information.getValue());
            acb.addField(new MessageEmbed.Field(information.getDisplayName(), information.getValue(), false));

            if (informationIterator.hasNext()) {
                reason.append(", ");
                infoBuilder.append("§e, ");
            } else {
                PlayerPingHistory pingHistory = account.getProperty("pings", new PlayerPingHistory()).getAs(PlayerPingHistory.class);
                reason.append(", ping=").append(pingHistory.getMinimum()).append("/").append(pingHistory.getAverage()).append("/").append(pingHistory.getMaximum());
                infoBuilder.append("§e, §6ping§e=§b").append(pingHistory.getMinimum()).append("/").append(pingHistory.getAverage()).append("/").append(pingHistory.getMaximum()).append("§e}");
                acb.addField(new MessageEmbed.Field("Ping", pingHistory.getMinimum() + "/" + pingHistory.getAverage() + "/" + pingHistory.getMaximum(), false));
            }
        }


        Property alertCount = account.getProperty(alertUniqueId.toString(), 0);
        alertCount.setValue(alertCount.getAsInt() + 1);

        int count = alertCount.getAsInt();

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(String.format(opening, count, alert.getDisplayName(), account.getUsername(), infoBuilder) + "\n§eClique "));
        TextComponent component2 = new TextComponent("§b§lAQUI");
        TextComponent component3 = new TextComponent("§e para se teleportar.");
        component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique para se teleportar.")));
        component2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + account.getUsername()));

        acb.setColor(Color.RED);
        acb.setAuthor(account.getUsername());
        acb.setTitle("**" + alert.getDisplayName() + "**: __" + count + "º__ aviso");
        acb.setDescription("```/go " + account.getUsername() + "```");
        acb.setThumbnail("https://mineskin.eu/helm/" + account.getUniqueId() + "/256");


        TextChannel txt = ProxyGame.getInstance().getDiscord().getJDA().getTextChannelById("1310230220162207834");

        if (txt != null)
            txt.sendMessageEmbeds(acb.build()).queue();

        Constants.getAccountStorage().getAccounts().forEach(acc -> {

            ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(acc.getUniqueId());

            if (proxiedPlayer == null)
                return;

            if (acc.hasPermission(Rank.TRIAL_MODERATOR) && acc.getPreference(Preference.ANTICHEAT)) {
                proxiedPlayer.sendMessage(component, component2, component3);
            }
        });

        if (count == alert.getMaximumAlerts()) {
            try {
                Punish punish = new Punish();
                punish.setApplier("[ANTICHEAT]");
                punish.setReason("" + reason);
                punish.setActive(true);
                punish.setTime(StringTimeUtils.parseDateDiff("30d", true));
                punish.setAddress(account.getData(Columns.ADDRESS).getAsString());
                punish.setCode(Constants.KEY(6, false).toLowerCase());
                punish.setType(PunishType.BAN);
                punish.setAutomatic(true);
                punish.setCategory(PunishCategory.CHEATING);
                punish.setApplyDate(System.currentTimeMillis());

                punish.assign(account);
                BungeeCord.getInstance().getPluginManager().callEvent(new PunishAssignEvent(account, punish));

                System.out.println("Automatic ban applied for " + account.getUsername());

                Constants.getAccountStorage().getAccounts().stream().filter(staff -> staff.hasPermission(Rank.PARTNER_PLUS) && staff.getPreference(Preference.STAFFCHAT)).forEach(acc -> {

                    ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(acc.getUniqueId());

                    if (proxiedPlayer == null)
                        return;

                    proxiedPlayer.sendMessage("§e[STAFF]§r §b§lBOB§e: §eJogador §6" + account.getUsername() + "§e foi banido.");

                });

                //1308862548132495402

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setAuthor("🔨 ANTI-CHEAT " + (punish.isInexcusable() ? " ❌" : ""));
                builder.setTitle(account.getUsername());
                builder.addField(new MessageEmbed.Field(":mega: Motivo", punish.getReason() + " (#" + account.count(punish.getType()) + ")", false));
                if (!punish.isPermanent())
                    builder.addField(":stopwatch: Expira em", StringTimeUtils.formatDifference(StringTimeUtils.Type.SIMPLIFIED, (punish.getTime() + 1000)), false);
                builder.setThumbnail("https://mineskin.eu/helm/" + account.getUniqueId() + "/256");

                TextChannel textChannel = ProxyGame.getInstance().getDiscord().getJDA().getTextChannelById("1310230218665099294");

                if (textChannel != null)
                    textChannel.sendMessageEmbeds(builder.build()).queue();

                DataResolver.getInstance().getData(punish.getAddress()).setBanned(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
