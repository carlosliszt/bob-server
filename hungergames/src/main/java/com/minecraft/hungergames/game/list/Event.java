/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.list;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.punish.Punish;
import com.minecraft.core.punish.PunishType;
import com.minecraft.core.server.ServerType;
import com.minecraft.core.translation.Language;
import com.minecraft.core.util.DateUtils;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.command.OpenEventCommand;
import com.minecraft.hungergames.command.StormSurgeCommand;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.game.Loadable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Loadable
public class Event extends Game {

    public Event(HungerGames hungerGames) {
        super(hungerGames);
        setName("Evento");
        getVariables().setMinimumPlayers(10);
        getVariables().setCountStats(false);
        getVariables().setFinalArenaSpawn(false);
        getVariables().setFinalCombatSpawn(false);
        Constants.setServerType(ServerType.EVENT);
        hungerGames.getWhitelist().setActive(true);
        hungerGames.getWhitelist().setMinimumRank(Rank.TRIAL_MODERATOR);
        getPlugin().setRankingFactory(null);
        getPlugin().getBukkitFrame().registerAdapter(EventMode.class, EventMode::get);
        getPlugin().getBukkitFrame().registerCommands(new OpenEventCommand(), new StormSurgeCommand());
    }

    @Variable(name = "event.mode", permission = Rank.TRIAL_MODERATOR)
    @Getter
    @Setter
    private EventMode eventMode = EventMode.NONE;

    @Override
    public void load() {
        super.load();
        try {
            getVariable("hg.timer.pregame_reduce_time").setValue(false);
            getVariable("hg.kit.free").setValue(false);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to change local variables default values", e);
        }
    }

    @Override
    public void unload() {
        super.unload();
        getPlugin().getBukkitFrame().unregisterCommand("openevent");
        getPlugin().getBukkitFrame().unregisterCommand("stormsurge");
    }

    @Override
    public void handleSidebar(User user) {
        Game game = this;
        GameStage stage = game.getStage();
        String time = format(game.getTime());
        String count = getCount();
        Account account = user.getAccount();
        GameScoreboard scoreboard = user.getScoreboard();

        if (scoreboard == null) //Preventing NPE
            return;

        scoreboard.updateTitle("§b§l" + game.getName().toUpperCase());

        List<String> scores = new ArrayList<>();
        scores.add(" ");

        if (account.getLanguage() == Language.PORTUGUESE) {

            if (stage == GameStage.WAITING)
                scores.add("§fIniciando em: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvencível por: §7" + time);
            else
                scores.add("§fTempo: §7" + time);

            scores.add("§fJogadores: §7" + count);

            if (!user.isAlive()) {
                scores.add(" ");
                scores.add(user.isVanish() ? "§cMODO VANISH" : "§7MODO ESPECTADOR");
            } else {
                Iterator<Kit> iterator = user.getKitList().stream().filter(kit -> !kit.isNone()).iterator();

                if (iterator.hasNext())
                    scores.add(" ");

                int kitCount = 0;

                while (iterator.hasNext()) {

                    Kit kit = iterator.next();

                    if (iterator.hasNext() || kitCount != 0) {
                        kitCount++;
                        scores.add("§fKit " + kitCount + ": §a" + kit.getDisplayName());
                    } else
                        scores.add("§fKit: §a" + kit.getDisplayName());
                }

                if (stage == GameStage.PLAYING || stage == GameStage.VICTORY)
                    scores.add("§fKills: §a" + user.getKills());
            }
        } else {

            if (stage == GameStage.WAITING)
                scores.add("§fStarting in: §7" + time);
            else if (stage == GameStage.INVINCIBILITY)
                scores.add("§fInvincible for: §7" + time);
            else
                scores.add("§fGame Time: §7" + time);

            scores.add("§fPlayers: §7" + count);

            if (!user.isAlive()) {
                scores.add(" ");
                scores.add(user.isVanish() ? "§cVANISH MODE" : "§7SPECTATOR MODE");
            } else {
                Iterator<Kit> iterator = user.getKitList().stream().filter(kit -> !kit.isNone()).iterator();
                int kitCount = 0;

                if (iterator.hasNext())
                    scores.add(" ");

                while (iterator.hasNext()) {

                    Kit kit = iterator.next();

                    if (iterator.hasNext() || kitCount != 0) {
                        kitCount++;
                        scores.add("§fKit " + kitCount + ": §a" + kit.getDisplayName());
                    } else
                        scores.add("§fKit: §a" + kit.getDisplayName());
                }

                if (stage == GameStage.PLAYING || stage == GameStage.VICTORY)
                    scores.add("§fKills: §a" + user.getKills());

            }
        }

        EventMode mode = getEventMode();

        if (mode != EventMode.NONE) {
            scores.add(" ");
            if (account.getLanguage() == Language.PORTUGUESE)
                scores.add("§fModo: §a" + account.getLanguage().translate(mode.getKey()));
            else
                scores.add("§fType: §a" + account.getLanguage().translate(mode.getKey()));
        }

        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        scoreboard.updateLines(scores);
    }

    @Variable(name = "hg.event.blacklist", permission = Rank.ADMINISTRATOR)
    public boolean blacklist = true;

    @Variable(name = "hg.event.block_new_accounts", permission = Rank.ADMINISTRATOR)
    public boolean blockNewAccounts = true;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        Account account = Account.fetch(event.getUniqueId());

        if (account == null)
            return;


        if (blockNewAccounts && !account.getData(Columns.PREMIUM).getAsBoolean() && account.getData(Columns.HG_KILLS).getAsInt() < 30 && System.currentTimeMillis() < (account.getData(Columns.FIRST_LOGIN).getAsLong() + 604800000)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cVocê não possuí os requisitos mínimos para entrar nessa sala.");
            return;
        }


        if (blacklist && account.isPunished(PunishType.EVENT)) {
            Punish punish = account.getPunish(PunishType.EVENT);

            StringBuilder stringBuilder = new StringBuilder();

            if (account.getLanguage() == Language.PORTUGUESE) {

                stringBuilder.append("§cVocê está restrito de participar de eventos por ").append(punish.getReason()).append(".");

                if (!punish.isPermanent())
                    stringBuilder.append(" Expira em: ").append(DateUtils.formatDifference(punish.getTime(), Language.PORTUGUESE, DateUtils.Style.NORMAL));
            } else {
                stringBuilder.append("§cYou are restricted from playing events by ").append(punish.getReason()).append(".");

                if (!punish.isPermanent())
                    stringBuilder.append("Expires in: ").append(DateUtils.formatDifference(punish.getTime(), Language.ENGLISH, DateUtils.Style.NORMAL));
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, stringBuilder.toString());

            return;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum EventMode {

        NONE(null, null),
        SOLO("event.mode.solo.name", ""),
        DUOS("event.mode.duos.name", ""),
        THREESOME("event.mode.threesome.name", ""),
        UNLIMITED("event.mode.unlimited.name", "");

        private final String key, arena;

        public static EventMode get(String i) {
            return Arrays.stream(values()).filter(c -> c.name().equalsIgnoreCase(i)).findFirst().orElse(null);
        }
    }

    @Override
    public String getDisplay() {
        return getName();
    }
}
