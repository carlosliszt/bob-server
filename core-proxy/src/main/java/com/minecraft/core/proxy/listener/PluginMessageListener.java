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
import com.minecraft.core.proxy.event.PunishAssignEvent;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.proxy.util.player.PlayerPingHistory;
import com.minecraft.core.proxy.util.server.ServerAPI;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.packet.ServerPayload;
import com.minecraft.core.util.StringTimeUtils;
import com.minecraft.core.util.anticheat.AntiCheatAlert;
import com.minecraft.core.util.anticheat.information.Information;
import com.minecraft.core.util.geodata.DataResolver;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

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
        String opening = "§7AntiCheat> §e%sº aviso: §a%s §7falhou no teste de §a%s §8%s";
        StringBuilder infoBuilder = new StringBuilder("(");

        Iterator<Information> informationIterator = alert.getInformations().iterator();

        if (!informationIterator.hasNext()) {
            PlayerPingHistory pingHistory = account.getProperty("pings", new PlayerPingHistory()).getAs(PlayerPingHistory.class);
            infoBuilder.append(", ping=").append(pingHistory.getMinimum()).append("/").append(pingHistory.getAverage()).append("/").append(pingHistory.getMaximum());
        }

        while (informationIterator.hasNext()) {
            Information information = informationIterator.next();
            infoBuilder.append(information.getDisplayName()).append("=").append(information.getValue());

            if (informationIterator.hasNext())
                infoBuilder.append(", ");
            else {
                PlayerPingHistory pingHistory = account.getProperty("pings", new PlayerPingHistory()).getAs(PlayerPingHistory.class);
                infoBuilder.append(", ping=").append(pingHistory.getMinimum()).append("/").append(pingHistory.getAverage()).append("/").append(pingHistory.getMaximum());
            }
        }

        infoBuilder.append(")");

        Property alertCount = account.getProperty(alertUniqueId.toString(), 0);
        alertCount.setValue(alertCount.getAsInt() + 1);

        int count = alertCount.getAsInt();

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(String.format(opening, count, account.getUsername(), alert.getDisplayName(), infoBuilder)));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para ir!")));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + account.getDisplayName()));

        Constants.getAccountStorage().getAccounts().forEach(acc -> {

            ProxiedPlayer proxiedPlayer = BungeeCord.getInstance().getPlayer(acc.getUniqueId());

            if (proxiedPlayer == null)
                return;

            if (acc.hasPermission(Rank.TRIAL_MODERATOR) && acc.getPreference(Preference.ANTICHEAT)) {
                proxiedPlayer.sendMessage(component);
            }
        });

        if (count == alert.getMaximumAlerts()) {
            try {
                Punish punish = new Punish();
                punish.setApplier("[ANTICHEAT]");
                punish.setReason("20x " + alert.getDisplayName() + " anticheat reports " + infoBuilder);
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

                DataResolver.getInstance().getData(punish.getAddress()).setBanned(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
