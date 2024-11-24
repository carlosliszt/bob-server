package com.minecraft.staff.build.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.staff.build.Build;
import com.minecraft.staff.build.util.WorldUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CreateWorld implements BukkitInterface {

    @Command(name = "createworld", usage = "createworld <worldName>")
    public void handleCreateWorldCommand(Context<CommandSender> context, String worldName) {
        if (Bukkit.getServer().getWorld(worldName) == null) {
            System.out.println("Creating build world...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            wc.createWorld();
            World world = wc.createWorld();
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("naturalRegeneration", "false");
            world.setGameRuleValue("sendCommandFeedback", "true");
            world.setGameRuleValue("logAdminCommands", "true");

            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MIN_VALUE);
            world.setThunderDuration(Integer.MIN_VALUE);

            context.getSender().sendMessage("§eMundo §b" + worldName + "§e criado com sucesso");
            if (context.isPlayer()) {
                TextComponent textComponent = new TextComponent("§ePara ir até ele, clique ");
                TextComponent textComponent2 = createTextComponent("§b§lAQUI", HoverEvent.Action.SHOW_TEXT, "§7Clique para teleportar.", ClickEvent.Action.RUN_COMMAND, "/goworld " + worldName);
                Player player = (Player) context.getSender();
                player.spigot().sendMessage(textComponent, textComponent2);
            }

        } else {
            System.out.println("§cO mundo " + worldName + " já existe.");
        }
    }

    @Command(name = "goworld", platform = Platform.PLAYER, usage = "goworld <worldName>")
    public void handleGoWorldCommand(Context<Player> context, String worldName) {
        Player player = context.getSender();
        player.setOp(true);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.teleport(new Location(Bukkit.getWorld(worldName), 0, 0, 0));
        player.sendMessage("§eTeleportado para o mundo §b" + worldName);
    }

    @Command(name = "worldinfo", platform = Platform.PLAYER, usage = "worldinfo")
    public void handleWorldInfo(Context<Player> context) {
        Player player = context.getSender();
        World world = player.getWorld();

        player.sendMessage("§eMundo: §b" + world.getName());
        player.sendMessage("§eTipo: §b" + world.getWorldType());
        player.sendMessage("§eSpawn: §bX" + world.getSpawnLocation().getX() + " Y" + world.getSpawnLocation().getY() + " Z" + world.getSpawnLocation().getZ());
    }

    @Command(name = "worldlist", platform = Platform.PLAYER, usage = "worldlist")
    public void handleWorldList(Context<Player> context) {
        Player player = context.getSender();
        player.sendMessage("§eMundos carregados:");
        for (World world : Bukkit.getWorlds()) {
            TextComponent textComponent = new TextComponent("§e " + world.getName() + " - ");

            TextComponent textComponent2 = createTextComponent("§b§lIR", HoverEvent.Action.SHOW_TEXT, "§7Clique para teleportar.", ClickEvent.Action.RUN_COMMAND, "/goworld " + world.getName());
            player.spigot().sendMessage(textComponent, textComponent2);
        }
    }

    @Completer(name = "goworld")
    public List<String> handleComplete(Context<CommandSender> context) {
        List<String> completer = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            completer.add(world.getName());
        }
        return completer;
    }

}
