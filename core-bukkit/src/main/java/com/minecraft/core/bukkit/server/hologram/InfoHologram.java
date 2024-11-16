/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.server.hologram;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.leaderboard.libs.LeaderboardUpdate;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.viaversion.viaversion.ViaVersionPlugin;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class InfoHologram extends Hologram implements Listener {

    private final LeaderboardUpdate period;
    private final Updater updater;

    public InfoHologram(Player p, Location location, String notice, String name, LeaderboardUpdate period, @NonNull Updater updater) {
        super(p, location, notice, name, "§e... jogando");
        this.period = period;
        this.updater = updater;

        if (ViaVersionPlugin.getInstance().getApi().getPlayerVersion(p) < 47) {
            setInteract((player, hologram, line, type) -> {
                List<NPC> npcs = new ArrayList<>(BukkitGame.getEngine().getNPCProvider().getPlayerHumans(player));
                npcs.removeIf(NPC::isHidden);

                if (npcs.isEmpty())
                    return;

                npcs.sort(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(getLocation())));
                NPC.Interact interact = npcs.get(0).getInteractExecutor();
                if (interact != null)
                    interact.handle(player, npcs.get(0), NPC.Interact.ClickType.RIGHT);
            });
        }
    }

    @EventHandler
    public void onServerUpdate(ServerHeartbeatEvent event) {
        if (event.isPeriodic(period.getPeriod())) {

            Player player = getTarget();

            Account account = Account.fetch(player.getUniqueId());

            if (account == null) {
                HandlerList.unregisterAll(this);
                return;
            }

            int count = getUpdater().update();

            String str = account.getLanguage().translate("information.playing", (count == -1 ? "..." : count));
            updateText(getHologramLines().size() - 1, ChatColor.YELLOW + str);
        }
    }

    @Override
    public void show() {
        super.show();
        if (updater != null)
            Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
    }

    @Override
    public void hide() {
        super.hide();
        if (updater != null)
            HandlerList.unregisterAll(this);
    }

    public interface Updater {
        int update();
    }
}
