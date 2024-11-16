/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GetlocationCommand implements BukkitInterface {

    @Command(name = "getlocation", platform = Platform.PLAYER, rank = Rank.DEVELOPER_ADMIN)
    public void handleCommand(Context<Player> context) {
        Player player = context.getSender();
        Location location = player.getLocation();

        World world = location.getWorld();

        double x = location.getBlockX() + 0.5;
        double y = location.getY();
        double z = location.getBlockZ() + 0.5;

        float yaw = Math.round(location.getYaw());

        String loc = "new Location(Bukkit.getWorld(\"" + world.getName() + "\"), " + x + ", " + y + ", " + z + ", " + yaw + ", 0)";

        TextComponent textComponent = new TextComponent("§ePara copiar sua localização, clique ");
        TextComponent textComponent2 = createTextComponent("§b§lAQUI", HoverEvent.Action.SHOW_TEXT, "§7Clique para copiar.", ClickEvent.Action.SUGGEST_COMMAND, loc);

        player.spigot().sendMessage(textComponent, textComponent2);
    }

}