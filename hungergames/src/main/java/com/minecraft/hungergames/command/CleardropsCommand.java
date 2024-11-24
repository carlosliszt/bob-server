/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Optional;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class CleardropsCommand implements BukkitInterface {

    @Command(name = "cleardrops", platform = Platform.BOTH, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<CommandSender> context, @Optional(def = "0") String[] args) {

        if (!isInteger(args[0])) {
            context.info("invalid_number", 1, "?");
            return;
        }

        int time = Integer.parseInt(args[0]);

        if (time < 0) {
            context.info("command.number_negative");
            return;
        }

        World world = context.isPlayer() ? ((Player) context.getSender()).getWorld() : Bukkit.getWorlds().get(0);

        if (time == 0) {
            int entityCount = 0;
            int count = 0;

            for (Entity entity : world.getEntitiesByClasses(Item.class)) {
                Item item = (Item) entity;
                count += item.getItemStack().getAmount();
                entityCount++;
                item.remove();
            }

            context.info("command.cleardrops.drops_cleared", "(" + count + " drops e " + entityCount + " entidades)");

        } else {
            context.sendMessage("§aOs drops serão limpos daqui %s!", context.getLanguage().translateTime(time));
            new ClearTask(time, world);
        }
    }

    public static class ClearTask implements Listener, BukkitInterface {

        private int time;
        private final World world;

        public ClearTask(int delay, World world) {
            this.time = delay;
            this.world = world;
            Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());

            if (time > 60)
                broadcast("command.cleardrops.broadcast", time);
        }

        @EventHandler
        public void onServerHeartBeat(ServerHeartbeatEvent event) {
            if (event.isPeriodic(20)) {
                if (time != 0 && time % 30 == 0 && time <= 60 || time != 0 && time % 5 == 0 && time <= 15 || time != 0 && time <= 5)
                    broadcast("command.cleardrops.broadcast", time);
                else if (time == 0) {
                    world.getEntitiesByClasses(Item.class).forEach(Entity::remove);
                    broadcast("command.cleardrops.drops_cleared", "");
                    HandlerList.unregisterAll(this);
                }
                this.time--;
            }
        }

        public void broadcast(String key, int time) {
            for (Player p : this.world.getPlayers()) {
                Account account = Account.fetch(p.getUniqueId());
                if (account == null)
                    continue;
                p.sendMessage(account.getLanguage().translate(key, account.getLanguage().translateTime(time)));
            }
        }
    }
}
