/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.util.arena.Arena;
import com.minecraft.hungergames.util.arena.FileArena;
import com.minecraft.hungergames.util.arena.shape.Cylinder;
import com.minecraft.hungergames.util.arena.shape.Square;
import com.minecraft.hungergames.util.bo3.BO3;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaCommand implements BukkitInterface, Assistance {

    @Command(name = "arena", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<Player> context) {

        String[] args = context.getArgs();

        if (context.argsCount() == 1) {

            FileArena fileArena = FileArena.getArena(context.getArg(0));

            if (fileArena == null) {
                context.sendMessage("§aProcessando...");

                File file = new File(FileArena.getDirectory(), context.getArg(0));

                FileArena.load(file);
                fileArena = FileArena.getArena(context.getArg(0));

                if (!file.exists() || fileArena == null) {
                    context.info("object.not_found", "Arena");
                    return;
                }
            }

            final Map<Location, BlockState> map = new HashMap<>();

            context.sendMessage("§aArena carregada!");

            fileArena.getBO3().spawn(context.getSender().getLocation().clone(), (location, pattern) -> {
                map.put(location, location.getBlock().getState());
                return true;
            });

            if (!map.isEmpty())
                getPlugin().getWorldEditProvider().addUndo(context.getSender(), map);

        } else if (context.argsCount() >= 4) {

            if (!isInteger(args[1]) || !isInteger(args[2])) {
                context.info("command.variable.failed_to_convert");
                return;
            }

            int size = Integer.parseInt(args[1]);
            int height = Integer.parseInt(args[2]);

            List<Pattern> patternList = Pattern.parse(args[0]);

            if (patternList == null) {
                context.info("worldedit.invalid_materials");
                return;
            }

            Arena arena = null;

            if (args[3].equalsIgnoreCase("circle"))
                arena = new Cylinder(size, height, patternList, true);
            else if (args[3].equalsIgnoreCase("square"))
                arena = new Square(size, height, patternList, true, true);

            if (arena != null) {

                final Map<Location, BlockState> map = new HashMap<>();

                BO3.BlockHandle blockHandle = (location, pattern) -> {
                    if (map.containsKey(location))
                        return false;

                    map.put(location, location.getBlock().getState());
                    return true;
                };

                Location location = context.getSender().getLocation().clone().subtract(0, 1, 0);

                arena.spawn(location, blockHandle); // Spawning the arena
                clear(location, size, height, blockHandle); // Clearing the arena.

                if (!map.isEmpty())
                    getPlugin().getWorldEditProvider().addUndo(context.getSender(), map);
            } else {
                context.sendMessage("§cUso do /arena:");
                context.sendMessage("§c* /arena <arena>");
                context.sendMessage("§c* /arena <block> <size> <height> <type>");
            }
        } else {
            context.sendMessage("§cUso do /arena:");
            context.sendMessage("§c* /arena <arena>");
            context.sendMessage("§c* /arena <block> <size> <height> <type>");
        }
    }

    private void clear(Location location, int size, int height, BO3.BlockHandle blockHandle) {
        size += 2;
        height += 1;
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                for (int y = -2; y < height; y++) {
                    Location loc = location.clone().add(x, y, z);

                    if (loc.getBlock().getType() == Material.AIR)
                        continue;

                    if (blockHandle.canPlace(loc, null))
                        WorldEditAPI.getInstance().setBlock(loc, Material.AIR, (byte) 0);
                }
            }
        }
    }

    @Completer(name = "arena")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {

            File[] file = FileArena.getDirectory().listFiles();

            if (file == null)
                return Collections.emptyList();

            return Arrays.stream(file).filter(c -> !c.isDirectory() && startsWith(c.getName(), context.getArg(0))).map(File::getName).collect(Collectors.toList());
        } else if (context.argsCount() == 4)
            return Stream.of("circle", "square").filter(c -> startsWith(c, context.getArg(3))).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
