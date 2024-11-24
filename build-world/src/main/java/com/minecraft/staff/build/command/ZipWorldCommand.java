package com.minecraft.staff.build.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.staff.build.Build;
import com.minecraft.staff.build.util.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ZipWorldCommand {

    @Command(name = "zipworld", platform = Platform.PLAYER, usage = "zipworld <worldName>")
    public void handleZipWorld(Context<Player> context, String worldName) {
        Player player = context.getSender();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage("§cO mundo " + worldName + " não existe.");
            return;
        }

        player.sendMessage("§eCompactando mundo §b" + worldName + "§e...");
        try {
            WorldUtils.zipWorld(worldName);
            player.sendMessage("§eMundo §b" + worldName + "§e compactado com sucesso.");
        } catch (Exception e) {
            player.sendMessage("§cErro ao compactar mundo " + worldName + ".");
            e.printStackTrace();
        }
    }

    @Completer(name = "zipworld")
    public List<String> handleComplete(Context<CommandSender> context) {
        List<String> completer = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            completer.add(world.getName());
        }
        return completer;
    }
}
