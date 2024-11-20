/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.AccountExecutor;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.whitelist.Whitelist;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.*;
import com.minecraft.core.server.ServerStorage;
import com.minecraft.core.translation.Language;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AccountLoader implements Listener, VariableStorage {

    private final Whitelist whitelist = BukkitGame.getEngine().getWhitelist();
    private final List<Columns> columns;
    @Getter
    @Variable(name = "max_players", permission = Rank.PRIMARY_MOD)
    public int maxPlayers = Bukkit.getMaxPlayers();

    public AccountLoader() {
        loadVariables();
        this.columns = new ArrayList<>(Arrays.asList(Columns.PUNISHMENTS, Columns.CLAN, Columns.RANKS, Columns.PERMISSIONS, Columns.PLUSCOLOR, Columns.PLUSCOLORS, Columns.NICK_OBJECTS, Columns.LAST_NICK, Columns.FIRST_LOGIN, Columns.PREMIUM, Columns.LAST_LOGIN, Columns.PREFERENCES, Columns.SKIN, Columns.FLAGS, Columns.TAGS, Columns.MEDALS, Columns.MEDAL, Columns.CLANTAGS, Columns.CLANTAG, Columns.PREFIXTYPE, Columns.NICK, Columns.LANGUAGE, Columns.TAG));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent) {

        if (asyncPlayerPreLoginEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        UUID uniqueId = asyncPlayerPreLoginEvent.getUniqueId();
        String username = asyncPlayerPreLoginEvent.getName();

        Account account = new Account(uniqueId, username);
        account.setAccountExecutor(new AccountExecutor() {
            @Override
            public void sendMessage(String message) {
                Bukkit.getPlayer(uniqueId).sendMessage(message);
            }

            @Override
            public void sendPluginMessage(String channel, byte[] bytes) {
                Bukkit.getPlayer(uniqueId).sendPluginMessage(BukkitGame.getEngine(), channel, bytes);
            }
        });

        if (account.getDataStorage().loadColumns(columns)) {

            account.loadSkinData();
            account.loadRanks();
            account.loadPermissions();

            account.setProperty("account_language", Language.fromUniqueCode(account.getData(Columns.LANGUAGE).getAsString()));

            if (whitelist.isActive() && !account.hasPermission(whitelist.getMinimumRank()) && !whitelist.isWhitelisted(uniqueId)) {
                asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, account.getLanguage().translate("whitelisted_server"));
                return;
            }

            account.loadPunishments();

            account.loadTags();
            account.getTagList().loadTags();

            Tag tag = Tag.getOrElse(account.getData(Columns.TAG).getAsString(), account.getTagList().getHighestTag());

            if (!account.getTagList().hasTag(tag))
                tag = account.getTagList().getHighestTag();

            account.setProperty("account_tag", tag);

            account.loadMedals();
            account.getMedalList().loadMedals();

            account.loadPlusColors();
            account.getPlusColorList().loadPlusColor();

            if (account.hasClan()) {
                Clan clan = account.getClan();

                if (clan == null || !clan.isMember(account.getUniqueId())) {
                    account.getData(Columns.CLAN).setData(-1);
                    account.getData(Columns.CLAN).setChanged(false);
                }
            }

            account.loadClanTags();
            account.getClanTagList().loadClanTags();

            Clantag clantag = Clantag.getOrElse(account.getData(Columns.CLANTAG).getAsString(), account.getClanTagList().getHighestClanTag());

            if (!account.getClanTagList().hasTag(clantag))
                clantag = account.getClanTagList().getHighestClanTag();

            account.setProperty("account_clan_tag", clantag);

            Medal medal = Medal.getOrElse(account.getData(Columns.MEDAL).getAsString(), account.getMedalList().getHighestMedal());

            if (!account.getMedalList().hasMedal(medal))
                medal = account.getMedalList().getHighestMedal();

            account.setProperty("account_medal", medal);

            PlusColor plusColor = PlusColor.getOrElse(account.getData(Columns.PLUSCOLOR).getAsString(), account.getPlusColorList().getHighestPlusColor());

            if (!account.getPlusColorList().hasPlusColor(plusColor))
                plusColor = account.getPlusColorList().getHighestPlusColor();

            account.setProperty("account_pluscolor", plusColor);

            PrefixType prefixType = PrefixType.fromUniqueCode(account.getData(Columns.PREFIXTYPE).getAsString());

            if (prefixType == null || !account.hasPermission(prefixType.getRank()))
                prefixType = PrefixType.DEFAULT;

            account.setProperty("account_prefix_type", prefixType);

            account.loadNicks();
            String customName = account.getData(Columns.NICK).getAsString();

            if (!customName.equals("...")) {
                account.setDisplayName(customName);
            }

            account.setFlags(account.getData(Columns.FLAGS).getAsInt());
            account.setPreferences(account.getData(Columns.PREFERENCES).getAsInt());

            Constants.getAccountStorage().store(uniqueId, account);
        } else {
            asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Language.PORTUGUESE.translate("unexpected_error"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) { // We don't want to deal with null all the time lol

        Player player = event.getPlayer();
        Account account = Account.fetch(player.getUniqueId());

        if (account == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Language.PORTUGUESE.translate("unexpected_error"));
            return;
        }

        if (account.hasPermission(Rank.TRIAL_MODERATOR))
            player.setOp(true);

        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            ServerStorage serverStorage = BukkitGame.getEngine().getServerStorage();

            if (account.hasPermission(Rank.VIP) || serverStorage.getLocalServer().getBreath().getOnlinePlayers() < maxPlayers) {
                event.allow();
            } else {
                event.setKickMessage(account.getLanguage().translate("server_is_full", Constants.SERVER_STORE));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Constants.getAccountStorage().forget(event.getPlayer().getUniqueId());
        BukkitGame.getEngine().getAntiCheat().removeSuspect(event.getPlayer().getUniqueId());
    }

    public void addColumns(Columns... columns) {
        this.columns.addAll(Arrays.asList(columns));
    }
}