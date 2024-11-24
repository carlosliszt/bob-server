/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountExecutor;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.clan.member.Member;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.staff.Staffer;
import com.minecraft.core.proxy.util.antibot.list.AddressLimit;
import com.minecraft.core.proxy.util.antibot.list.CountryBlocker;
import com.minecraft.core.proxy.util.antibot.list.NameBlocker;
import com.minecraft.core.proxy.util.antibot.list.RateLimit;
import com.minecraft.core.proxy.util.player.PlayerPingHistory;
import com.minecraft.core.proxy.util.player.SkinChanger;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishCategory;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.core.util.geodata.AddressData;
import com.minecraft.core.util.geodata.DataResolver;
import com.minecraft.core.util.skin.Skin;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.minecraft.core.database.enums.Tables.*;

public class AccountLoader implements Listener {

    private final AddressLimit addressLimit = new AddressLimit(5);
    private final RateLimit rateLimit = new RateLimit();
    private final NameBlocker nameBlocker = new NameBlocker().blockNames("cipher_bot", "mcbot", "dortware", "dropbot", "mcspam", "mcstorm", "mcdrop");
    private final CountryBlocker countryBlocker = new CountryBlocker(
            "Kenya", "China", "Indonesia", "Mongolia", "Armenia",
            "Russia", "India", "Combodia", "Thailand", "Chipre", "Fiji", "Iran",
            "Vietnam", "Nepal", "Bengladesh", "South Africa", "Japan", "Moldovia",
            "Africa", "North Africa", "Pakistan", "Angola", "Bangladesh", "Iraq",
            "Italy", "Croatia", "Taiwan", "Poland", "Hungary");

    private final List<Columns> defaultColumns = Arrays.asList(Columns.BANNED, Columns.ULTRA_PLUS_MONTHS, Columns.NICK_OBJECTS, Columns.LAST_NICK, Columns.LAST_LOGIN, Columns.CLAN, Columns.SKIN, Columns.FIRST_LOGIN, Columns.RANKS, Columns.PERMISSIONS, Columns.PUNISHMENTS, Columns.PREMIUM, Columns.FLAGS, Columns.TAGS, Columns.CLANTAGS, Columns.CLANTAG, Columns.MEDALS, Columns.MEDAL, Columns.PLUSCOLORS, Columns.PLUSCOLOR, Columns.PREFIXTYPE, Columns.NICK, Columns.LANGUAGE, Columns.TAG);

    @EventHandler
    public void onConnectionInit(ClientConnectEvent event) {
        if (rateLimit.isViolator(null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHandshake(PlayerHandshakeEvent event) {
        if (event.getHandshake().getRequestedProtocol() != 1) // != Ping
            rateLimit.increment();
    }

    @EventHandler(priority = EventPriority.LOWEST)

    public void onPreLoginEvent(PreLoginEvent event) {

        PendingConnection pendingConnection = event.getConnection();
        String username = pendingConnection.getName();

        int version = pendingConnection.getVersion();

        if (version <= 4 /*|| version > 47*/) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§cVersão não suportada. Utilize ao menos a versão 1.7.10 para jogar."));
            return;
        }

        if (nameBlocker.isViolator(pendingConnection)) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§cForam detectadas irregularidades em sua conexão.\n§cAguarde para conectar novamente."));
            return;
        }

        if (addressLimit.isViolator(pendingConnection)) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§cVocê atingiu o número limite de contas conectadas no mesmo endereço IP."));
            event.completeIntent(ProxyGame.getInstance());
            return;
        }

        if (BungeeCord.getInstance().getPlayer(username) != null) {
            event.setCancelled(true);
            event.setCancelReason(TextComponent.fromLegacyText("§cFalha no login: Você já está conectado."));
        }

        BungeeCord.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("§aProcessing " + username + "§f(" + event.getConnection().getAddress().getHostString() + ")§a connection..."));
        event.registerIntent(ProxyGame.getInstance());

        ProxyServer.getInstance().getScheduler().runAsync(ProxyGame.getInstance(), () -> {

            final String hostName = pendingConnection.getAddress().getHostString();

            AddressData addressData = DataResolver.getInstance().getData(hostName);

            if (countryBlocker.isViolator(pendingConnection)) {
                System.out.println(addressData.getAddress() + " is logging from a untrusted country. (" + addressData.getCountry() + ")");
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§cVocê está tentando conectar através de um país não permitido."));
                event.completeIntent(ProxyGame.getInstance());
                return;
            }

            if (!addressData.isTrusted()) {
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§cNão foi possível autorizar sua entrada.\nContate-nos em " + Constants.SERVER_DISCORD + " enviando as informações abaixo.\n\n§cUsername: " + username + "\n§cEndereço IP: " + hostName));
                event.completeIntent(ProxyGame.getInstance());
                return;
            }

            try {
                boolean premium = Constants.getMojangAPI().getUniqueId(username) != null;

                pendingConnection.setOnlineMode(premium);
                if (!premium) {
                    UUID uuid = Constants.getCrackedUniqueId(username);
                    pendingConnection.setUniqueId(uuid);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                event.setCancelled(true);
                event.setCancelReason(TextComponent.fromLegacyText("§cSua conta não pôde ser validada. (Mojang issue)"));
            }
            event.completeIntent(ProxyGame.getInstance());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoginEvent(LoginEvent loginEvent) {

        final PendingConnection pendingConnection = loginEvent.getConnection();

        loginEvent.registerIntent(ProxyGame.getInstance());
        ProxyServer.getInstance().getScheduler().runAsync(ProxyGame.getInstance(), () -> {

            final String hostName = pendingConnection.getAddress().getHostString();

            final UUID uniqueId = pendingConnection.getUniqueId();
            final String name = pendingConnection.getName();

            Account account = new Account(uniqueId, name);

            account.setAccountExecutor(new AccountExecutor() {
                @Override
                public void sendMessage(String message) {
                    ProxyServer.getInstance().getPlayer(uniqueId).sendMessage(TextComponent.fromLegacyText(message));
                }
            });

            List<Columns> columns = new ArrayList<>(defaultColumns);

            if (!pendingConnection.isOnlineMode())
                columns.addAll(Arrays.asList(Columns.SESSION_EXPIRES_AT, Columns.SESSION_ADDRESS, Columns.PASSWORD));

            if (account.getDataStorage().loadColumns(columns)) {

                AddressData addressData = DataResolver.getInstance().getData(pendingConnection.getAddress().getHostString());

                account.loadPunishments();
                account.setProperty("account_language", Language.fromUniqueCode(account.getData(Columns.LANGUAGE).getAsString()));
                account.setProperty("pings", new PlayerPingHistory());

                if (account.getLanguage() != Language.ENGLISH && !addressData.getCountry().equals("Brazil"))
                    account.setProperty("alert_lang", (byte) 0x1);

                if (isPunished(loginEvent, account))
                    return;

                if (account.getData(Columns.BANNED).getAsBoolean())
                    account.getData(Columns.BANNED).setData(false);

                account.setVersion(pendingConnection.getVersion());

                account.loadRanks();
                account.loadPermissions();


                if (isFull(loginEvent, account))
                    return;

                account.loadTags();
                account.loadMedals();
                account.loadPlusColors();
                account.loadSkinData();
                account.loadClanTags();

                account.getData(Columns.USERNAME).setData(name);

                account.getData(Columns.ADDRESS).setData(hostName);

                account.setFlags(account.getData(Columns.FLAGS).getAsInt());
                account.setPreferences(account.getData(Columns.PREFERENCES).getAsInt());

                if (account.hasClan()) {
                    Clan clan = account.getClan();

                    if (clan == null || !clan.isMember(account.getUniqueId())) {
                        account.getData(Columns.CLAN).setData(-1);
                    } else {
                        Member member = clan.getMember(account.getUniqueId());
                        member.setName(account.getUsername());
                    }
                }

                SkinData skinData = account.getSkinData();

                if (pendingConnection.isOnlineMode()) {

                    if (skinData.getSource() == SkinData.Source.UNDEFINED) {

                        Property property = SkinChanger.getInstance().getSkin(pendingConnection);

                        if (property != null) {
                            skinData.setSource(SkinData.Source.ACCOUNT);
                            skinData.setName(pendingConnection.getName());
                            skinData.setValue(property.getValue());
                            skinData.setSignature(property.getSignature());
                            skinData.setUpdatedAt(System.currentTimeMillis());

                            account.getData(Columns.SKIN).setData(skinData.toJson());

                            account.getDataStorage().saveColumn(Columns.SKIN);
                        }
                    } else if (skinData.getSource() == SkinData.Source.ACCOUNT) {

                        Property property = SkinChanger.getInstance().getSkin(pendingConnection);

                        if (!property.getValue().equals(skinData.getValue())) {

                            skinData.setSource(SkinData.Source.ACCOUNT);
                            skinData.setName(pendingConnection.getName());
                            skinData.setValue(property.getValue());
                            skinData.setSignature(property.getSignature());
                            skinData.setUpdatedAt(System.currentTimeMillis());

                            account.getData(Columns.SKIN).setData(skinData.toJson());
                            account.getDataStorage().saveColumn(Columns.SKIN);
                        }
                    }
                } else {
                    if (skinData.getSource() == SkinData.Source.UNDEFINED) {

                        Skin skin = Skin.getRandomSkin();

                        skinData.setSource(SkinData.Source.ACCOUNT);
                        skinData.setName(pendingConnection.getName());
                        skinData.setValue(skin.getCustomProperty().getValue());
                        skinData.setSignature(skin.getCustomProperty().getSignature());
                        skinData.setUpdatedAt(System.currentTimeMillis());

                        account.getData(Columns.SKIN).setData(skinData.toJson());

                        account.getDataStorage().saveColumn(Columns.SKIN);
                    }
                }

                //ProxyGame.getInstance().getStore().getPendingOrders(name).forEach(StoreHistoryData::apply);

                if (account.getData(Columns.FIRST_LOGIN).getAsObject().equals(Columns.FIRST_LOGIN.getDefaultValue())) {

                    if (addressData.isBanned()) {
                        loginEvent.setCancelled(true);
                        loginEvent.setCancelReason(TextComponent.fromLegacyText("§cNão foi possível cadastrar sua conta: Você está restrito de criar novas contas devido um banimento vinculado."));
                        loginEvent.completeIntent(ProxyGame.getInstance());
                        return;
                    }

                    account.getData(Columns.PREMIUM).setData(pendingConnection.isOnlineMode());
                    account.getData(Columns.FIRST_LOGIN).setData(System.currentTimeMillis());
                }

                account.getData(Columns.LAST_LOGIN).setData(System.currentTimeMillis());

                account.setProperty("account_address_data", DataResolver.getInstance().getData(hostName));
                account.setProperty("authenticated", false);

                account.loadNicks();
                String customName = account.getData(Columns.NICK).getAsString();

                if (!customName.equals("...") && account.getRank().getId() >= Rank.ULTRA_PLUS.getId()) {
                    account.setDisplayName(customName);
                } else account.getData(Columns.NICK).setData("...");

                if (pendingConnection.isOnlineMode() && name.contains("BOB")) {
                    account.giveMedal(Medal.BOB, -1, "[SERVER]");
                } else account.removeMedal(Medal.BOB);

                if (account.getRank().isStaffer()) {
                    account.getDataStorage().load(STAFF);
                }

                account.loadPlusColors();
                account.getPlusColorList().loadPlusColor();

                if (account.getData(Columns.ULTRA_PLUS_MONTHS).getAsInt() >= 1) {
                    if (!account.hasTag(Tag.ULTRA_PLUS_1)) {
                        account.giveTag(Tag.ULTRA_PLUS_1, -1, "[SERVER]");
                    }
                }

                if (account.getData(Columns.ULTRA_PLUS_MONTHS).getAsInt() >= 6) {
                    if (!account.hasTag(Tag.ULTRA_PLUS_2)) {
                        account.giveTag(Tag.ULTRA_PLUS_2, -1, "[SERVER]");
                    }
                }

                if (account.getData(Columns.ULTRA_PLUS_MONTHS).getAsInt() >= 12) {
                    if (!account.hasTag(Tag.ULTRA_PLUS_3)) {
                        account.giveTag(Tag.ULTRA_PLUS_3, -1, "[SERVER]");
                    }
                }

                account.getDataStorage().saveTable(ACCOUNT);
                account.getDataStorage().saveTable(OTHER);

                Constants.getAccountStorage().store(uniqueId, account);

                if (account.hasPermission(Rank.PARTNER_PLUS)) {
                    Staffer user = new Staffer(account);
                    ProxyGame.getInstance().getStaffStorage().store(account.getUniqueId(), user);
                }
            } else {
                loginEvent.setCancelled(true);
                loginEvent.setCancelReason(TextComponent.fromLegacyText(Language.PORTUGUESE.translate("unexpected_error")));
            }
            loginEvent.completeIntent(ProxyGame.getInstance());
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Account account = Account.fetch(player.getUniqueId());

        Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY);

        if (server == null) {
            player.disconnect(TextComponent.fromLegacyText(account.getLanguage().translate("no_server_available", "lobby")));
            return;
        }

        if (account.hasProperty("alert_lang")) {
            player.sendMessage(TextComponent.fromLegacyText("§6§lPRO TIP §eYou can change your primary language using §b/language§e for a better gaming experience."));
            account.removeProperty("alert_lang");
        }

        ServerInfo lobby = BungeeCord.getInstance().getServerInfo(server.getName());

        if (player.getPendingConnection().isOnlineMode()) {
            account.setProperty("authenticated", true);
            if (player.getServer() == null || !player.getServer().getInfo().getName().equals(lobby.getName()))
                player.connect(lobby);
        } else {
            if (account.isSessionValid(account.getData(Columns.ADDRESS).getAsString())) {
                account.setProperty("authenticated", true);
                if (player.getServer() == null || !player.getServer().getInfo().getName().equals(lobby.getName()))
                    player.connect(lobby);
            } else {
                server = ServerCategory.AUTH.getServerFinder().getBestServer(ServerType.AUTH);

                if (server == null) {
                    player.disconnect(TextComponent.fromLegacyText(account.getLanguage().translate("no_server_available", "auth")));
                    return;
                }

                player.connect(BungeeCord.getInstance().getServerInfo(server.getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Account account = Account.fetch(player.getUniqueId());

        if (account == null)
            return;

        boolean pendingCracked = !player.getPendingConnection().isOnlineMode() && !account.getProperty("authenticated").getAsBoolean();
        ServerInfo auth = ProxyServer.getInstance().getServerInfo("auth");

        if (event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY && pendingCracked)
            event.setTarget(auth);
        else if (pendingCracked && !event.getTarget().equals(auth))
            event.setTarget(auth);

        if (event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) {
            SkinData skinData = account.getSkinData();
            SkinChanger.getInstance().changeTexture(player.getPendingConnection(), skinData.getValue(), skinData.getSignature());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {

        Account account = Account.fetch(event.getPlayer().getUniqueId());

        if (account == null) // Preventing quick account re-log null error.
            return;

        // Forgetting it before cleaning up, the methods below may
        // throw NullPointerException and account keep stored forever in memory.

        Constants.getAccountStorage().forget(account);

        if (account.hasProperty("pings")) {
            account.getProperty("pings").getAs(PlayerPingHistory.class).getPings().clear();
        }

        account.getTagList().getTags().clear();
        account.getMedalList().getMedals().clear();
        account.getPlusColorList().getPlusColor().clear();
        account.getPlusColors().clear();
        account.getMedals().clear();
        account.getRanks().clear();
        SkinData skinData = account.getSkinData();

        skinData.setName("");
        skinData.setSignature("");
        skinData.setValue("");
    }

    public boolean isPunished(LoginEvent loginEvent, Account account) {
        if (account.isPunished(PunishType.BAN)) {
            Punish punish = account.getPunish(PunishType.BAN);

            if (punish == null) {
                loginEvent.setCancelled(true);
                loginEvent.setCancelReason(TextComponent.fromLegacyText(account.getLanguage().translate("unexpected_error")));
                loginEvent.completeIntent(ProxyGame.getInstance());
                return true;
            }

            StringBuilder msg = new StringBuilder();

            if (account.getLanguage() == Language.PORTUGUESE) {
                msg.append(punish.isPermanent() ? "§cVocê foi suspenso permanentemente." : "§cVocê está suspenso temporariamente.").append("\n");
                if (punish.getCategory() != PunishCategory.NONE)
                    msg.append("§cMotivo: ").append(punish.getCategory().getDisplay(Language.PORTUGUESE)).append("\n");

                if (!punish.isPermanent())
                    msg.append("§cExpira em: ").append(DateUtils.formatDifference(punish.getTime())).append("\n");

                msg.append("§cPode comprar unban: ").append(punish.isInexcusable() ? "Não" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? "Não" : "Sim")).append("\n");

                msg.append("§cID: #").append(punish.getCode()).append("\n\n");
                msg.append("§cSaiba mais em ").append(Constants.SERVER_WEBSITE);
            } else {
                msg.append(punish.isPermanent() ? "§cYou are permanently banned." : "§cYou are temporarily banned.").append("\n");
                if (punish.getCategory() != PunishCategory.NONE)
                    msg.append("§cReason: ").append(punish.getCategory().getDisplay(Language.ENGLISH)).append("\n");

                if (!punish.isPermanent())
                    msg.append("§cExpires in: ").append(DateUtils.formatDifference(punish.getTime())).append("\n");

                msg.append("§cCan buy unban: ").append(punish.isInexcusable() ? "No" : (account.count(punish.getType(), PunishCategory.CHEATING) >= 3 ? "No" : "Yes")).append("\n");

                msg.append("§cID: #").append(punish.getCode()).append("\n\n");
                msg.append("§cFind out more on ").append(Constants.SERVER_WEBSITE);
            }
            loginEvent.setCancelled(true);
            loginEvent.setCancelReason(TextComponent.fromLegacyText(msg.toString()));
            loginEvent.completeIntent(ProxyGame.getInstance());
            return true;
        }
        return false;
    }

    public boolean isFull(LoginEvent loginEvent, Account account) {
        if (!account.hasPermission(Rank.VIP) && ProxyServer.getInstance().getOnlineCount() >= ProxyGame.getInstance().getProxy().getConfig().getPlayerLimit()) {
            StringBuilder stringBuilder = new StringBuilder();

            if (account.getLanguage() == Language.PORTUGUESE) {
                stringBuilder.append("§cO servidor está lotado.").append("\n");
                stringBuilder.append("§cAdquira VIP para entrar em servidores lotados.").append("\n").append("\n");
            } else {
                stringBuilder.append("§cThe server is full.").append("\n");
                stringBuilder.append("§cBuy VIP to join full servers.").append("\n").append("\n");
            }

            stringBuilder.append("§c").append(Constants.SERVER_STORE);

            loginEvent.setCancelled(true);
            loginEvent.setCancelReason(TextComponent.fromLegacyText(stringBuilder.toString()));
            loginEvent.completeIntent(ProxyGame.getInstance());

            return true;
        }
        return false;
    }

}