/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.cooldown;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.cooldown.CooldownFinishEvent;
import com.minecraft.core.bukkit.event.cooldown.CooldownStartEvent;
import com.minecraft.core.bukkit.event.cooldown.CooldownStopEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.actionbar.ActionBar;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.translation.Language;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownProvider implements Listener {

    @Getter
    private static final CooldownProvider genericInstance = new CooldownProvider();

    private static final char CHAR = '|';

    @Getter
    private final Map<UUID, List<Cooldown>> cooldowns;

    @Getter
    private final BukkitGame bukkitGame;
    private final DynamicListener listener;

    public CooldownProvider() {
        cooldowns = new ConcurrentHashMap<>();
        listener = new CooldownListener();
        bukkitGame = BukkitGame.getEngine();
    }

    public void addCooldown(Player player, Cooldown cooldown) {
        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            List<Cooldown> list = cooldowns.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());

            boolean add = true;

            for (Cooldown cool : list) {
                if (cool.getKey().equals(cooldown.getKey())) {
                    cool.update(cooldown.getDuration(), cooldown.getStartTime());
                    add = false;
                }
            }

            if (add)
                list.add(cooldown);

            if (!cooldowns.isEmpty())
                listener.register();
        }
    }

    public void addCooldown(UUID uuid, String displayName, double duration) {
        addCooldown(uuid, displayName, displayName, duration, true);
    }

    public void addCooldown(UUID uuid, String displayName, double duration, boolean display) {
        addCooldown(uuid, displayName, displayName, duration, display);
    }

    public void addCooldown(UUID uuid, String displayname, String key, double duration, boolean display) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        Cooldown cooldown = new Cooldown(displayname, key, duration, display);
        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            List<Cooldown> list = cooldowns.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());

            boolean add = true;

            for (Cooldown cool : list) {
                if (cool.getKey().equals(cooldown.getKey())) {
                    cool.update(cooldown.getDuration(), cooldown.getStartTime());
                    add = false;
                }
            }

            if (add)
                list.add(cooldown);

            if (!cooldowns.isEmpty())
                listener.register();
        }
    }


    public boolean removeCooldown(Player player, String name) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            List<Cooldown> list = cooldowns.get(player.getUniqueId());
            Iterator<Cooldown> it = list.iterator();
            while (it.hasNext()) {
                Cooldown cooldown = it.next();
                if (cooldown.getKey().equals(name)) {
                    it.remove();
                    Bukkit.getPluginManager().callEvent(new CooldownStopEvent(player, cooldown));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if player has cooldown
     *
     * @param player
     * @param name
     * @return boolean
     */

    public boolean hasCooldown(Player player, String name) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            List<Cooldown> list = cooldowns.get(player.getUniqueId());
            for (Cooldown cooldown : list)
                if (cooldown.getKey().equals(name))
                    return true;
        }
        return false;
    }

    /**
     * Check if uniqueId has cooldown
     *
     * @param uniqueId
     * @param name
     * @return
     */

    public boolean hasCooldown(UUID uniqueId, String name) {
        if (cooldowns.containsKey(uniqueId)) {
            List<Cooldown> list = cooldowns.get(uniqueId);
            for (Cooldown cooldown : list)
                if (cooldown.getKey().equals(name))
                    return true;
        }
        return false;
    }

    /**
     * Return the cooldown of player, if the player not have cooldown will return
     * null
     *
     * @param uniqueId
     * @param name
     * @return
     */

    public Cooldown getCooldown(UUID uniqueId, String name) {
        if (cooldowns.containsKey(uniqueId)) {
            List<Cooldown> list = cooldowns.get(uniqueId);

            for (Cooldown cooldown : list)
                if (cooldown.getKey().equals(name))
                    return cooldown;
        }
        return null;
    }

    public class CooldownListener extends DynamicListener {

        @EventHandler
        public void onUpdate(ServerHeartbeatEvent ignored) {

            Iterator<UUID> iterator = cooldowns.keySet().iterator();

            while (iterator.hasNext()) {

                UUID uuid = iterator.next();
                Player player = Bukkit.getPlayer(uuid);
                List<Cooldown> list = cooldowns.get(uuid);

                if (player == null || list.isEmpty()) {
                    iterator.remove();
                    continue;
                }

                accept(list, player);
            }
        }

        private void accept(List<Cooldown> list, Player player) {
            Iterator<Cooldown> it = list.iterator();

            Cooldown found = null;
            while (it.hasNext()) {
                Cooldown cooldown = it.next();
                if (cooldown.expired()) {
                    it.remove();
                    CooldownFinishEvent e = new CooldownFinishEvent(player, cooldown);
                    Bukkit.getServer().getPluginManager().callEvent(e);
                }
            }

            Account account = Account.fetch(player.getUniqueId());

            if (account != null && !account.getPreference(Preference.EXTRA_INFO))
                return;

            List<Cooldown> sortedList = new ArrayList<>(list);
            sortedList.removeIf(c -> !c.isDisplay());

            if (!sortedList.isEmpty()) {
                sortedList.sort(Comparator.comparingDouble(Cooldown::getRemaining));
                found = sortedList.get(0);
            }

            if (found != null) {
                display(player, found);
            } else if (list.isEmpty()) {
                ActionBar.sendActionBar(player, " ");
            }
            sortedList.clear();
        }

        @EventHandler
        public void onCooldown(CooldownStopEvent event) {
            if (cooldowns.isEmpty()) {
                unregister();
            }
        }

        private final DecimalFormat decimalFormat = Constants.SIMPLE_DECIMAL_FORMAT;

        private void display(Player player, Cooldown cooldown) {
            StringBuilder bar = new StringBuilder();
            double percentage = cooldown.getPercentage();
            double count = 35 - Math.max(percentage > 0D ? 1 : 0, percentage / 2.8);
            for (int a = 0; a < 35 - count; a++)
                bar.append("§a" + CHAR);
            for (int a = 0; a < count; a++)
                bar.append("§c" + CHAR);

            String seconds = Account.fetch(player.getUniqueId()).getLanguage() == Language.PORTUGUESE ? "segundos" : "seconds";

            ActionBar.sendActionBar(player, "§f" + cooldown.getDisplayName() + " " + bar + " §f" + decimalFormat.format(cooldown.getRemaining()) + " " + seconds);
        }
    }
}

