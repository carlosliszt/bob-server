/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.bukkit.util.worldedit.WorldEditProvider;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WorldEditCommand implements BukkitInterface {

    @Command(name = "/cyl", aliases = {"cyl", "/hcyl", "hcyl"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <block> <radius> [height]")
    public void circleCommand(Context<Player> context, String materialString, int radius) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();
        String[] args = context.getArgs();
        int height = 1;

        if (args.length > 2) {
            if (isInteger(args[2])) {

                int parsedInt = Integer.parseInt(args[2]);

                if (parsedInt <= 0) {
                    context.info("command.number_negative");
                    return;
                }

                height = parsedInt;
            }
        }

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> materials = Pattern.parse(materialString);

        if (materials == null || materials.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        Map<Location, BlockState> map = new HashMap<>();

        boolean filled = !context.getLabel().toLowerCase().endsWith("hcyl");

        AtomicInteger changedBlocks = new AtomicInteger();


        int finalHeight = height;
        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().makeCylinder(player.getLocation(), radius, radius, finalHeight, filled);

            sync(() -> {
                positions.forEach(location -> {

                    Pattern pattern = materials.get(Constants.RANDOM.nextInt(materials.size()));

                    Material material = pattern.getMaterial();
                    byte data = pattern.getData();

                    if (location.getBlock().getType() != material || location.getBlock().getData() != data) {
                        map.put(location.clone(), location.getBlock().getState());
                        WorldEditAPI.getInstance().setBlock(location, material, data);
                        changedBlocks.getAndIncrement();
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }

    @Command(name = "/sphere", aliases = {"sphere", "/hsphere", "hsphere"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <block> <radius> [height]")
    public void sphereCommand(Context<Player> context, String materialString, int radius) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();
        String[] args = context.getArgs();

        int height = radius;

        if (args.length > 2) {
            if (isInteger(args[2])) {

                int parsedInt = Integer.parseInt(args[2]);

                if (parsedInt <= 0) {
                    context.info("command.number_negative");
                    return;
                }

                height = parsedInt;
            }
        }

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> materials = Pattern.parse(materialString);

        if (materials == null || materials.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        Map<Location, BlockState> map = new HashMap<>();

        boolean filled = !context.getLabel().toLowerCase().endsWith("hsphere");

        AtomicInteger changedBlocks = new AtomicInteger();


        int finalHeight = height;
        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().makeSphere(player.getLocation(), radius, radius, finalHeight, filled);

            sync(() -> {
                positions.forEach(location -> {

                    Pattern pattern = materials.get(Constants.RANDOM.nextInt(materials.size()));

                    Material material = pattern.getMaterial();
                    byte data = pattern.getData();

                    if (location.getBlock().getType() != material || location.getBlock().getData() != data) {
                        map.put(location.clone(), location.getBlock().getState());
                        WorldEditAPI.getInstance().setBlock(location, material, data);
                        changedBlocks.getAndIncrement();
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }


    @Command(name = "/pyramid", aliases = {"pyramid", "/hpyramid", "hpyramid"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <block> <radius>")
    public void pyramidCommand(Context<Player> context, String materialString, int size) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> materials = Pattern.parse(materialString);

        if (materials == null || materials.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        Map<Location, BlockState> map = new HashMap<>();

        boolean filled = !context.getLabel().toLowerCase().endsWith("hpyramid");

        AtomicInteger changedBlocks = new AtomicInteger();


        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().makePyramid(player.getLocation(), size, filled);

            sync(() -> {

                positions.forEach(location -> {

                    Pattern pattern = materials.get(Constants.RANDOM.nextInt(materials.size()));

                    Material material = pattern.getMaterial();
                    byte data = pattern.getData();

                    if (location.getBlock().getType() != material || location.getBlock().getData() != data) {
                        map.put(location.clone(), location.getBlock().getState());
                        WorldEditAPI.getInstance().setBlock(location, material, data);
                        changedBlocks.getAndIncrement();
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }

    @Command(name = "/pos1", aliases = {"pos1", "/pos2", "pos2"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <block>")
    public void positionCommand(Context<Player> context) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();
        boolean firstPosition = context.getLabel().endsWith("1");
        Location location = player.getLocation();

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();

        if (firstPosition)
            worldEditProvider.setFirstPosition(player, location);
        else
            worldEditProvider.setSecondPosition(player, location);

        context.info("worldedit.position_set", (firstPosition ? 1 : 2), (int) location.getX(), (int) location.getY(), (int) location.getZ());
    }

    @Command(name = "/replace", aliases = {"replace"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <from> <to>")
    public void replaceCommand(Context<Player> context, String fromString, String toString) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> from = Pattern.parse(fromString), to = Pattern.parse(toString);

        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        if (!worldEditProvider.hasFirstPosition(player)) {
            context.info("worldedit.unknown_position", 1);
            return;
        }

        if (!worldEditProvider.hasSecondPosition(player)) {
            context.info("worldedit.unknown_position", 2);
            return;
        }

        Map<Location, BlockState> map = new HashMap<>();

        Location loc1 = worldEditProvider.getFirstPosition(player), loc2 = worldEditProvider.getSecondPosition(player);

        AtomicInteger changedBlocks = new AtomicInteger();


        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().getBlocksBetween(loc1, loc2);

            sync(() -> {
                positions.forEach(location -> {

                    Block block = location.getBlock();

                    for (Pattern fromBlock : from) {

                        boolean byteEquals = (fromBlock.getData() == 0 || fromBlock.getData() == block.getData());

                        if (fromBlock.getMaterial() == block.getType() && byteEquals) {
                            map.put(location.clone(), block.getState());

                            Pattern pattern = to.get(Constants.RANDOM.nextInt(to.size()));

                            Material material = pattern.getMaterial();
                            byte data = pattern.getData();

                            WorldEditAPI.getInstance().setBlock(location, material, data);
                            changedBlocks.getAndIncrement();
                        }
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }

    @Command(name = "/up", aliases = {"up"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <number>")
    public void upCommand(Context<Player> context, int number) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();
        Location location = player.getLocation();

        Location block = location.clone().add(0, number - 1, 0);

        Map<Location, BlockState> map = new HashMap<>();
        map.put(block, block.getBlock().getState());

        WorldEditAPI.getInstance().setBlock(block, Material.GLASS, (byte) 0);
        player.teleport(block.clone().add(0, 1.1, 0));
        context.info("worldedit.up", number);
        BukkitGame.getEngine().getWorldEditProvider().addUndo(player, map);
    }

    @Command(name = "/replacenear", aliases = {"replacenear"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <size> <from> <to>")
    public void replaceNearCommand(Context<Player> context, int size, String fromString, String toString) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> from = Pattern.parse(fromString), to = Pattern.parse(toString);

        if (from == null || from.isEmpty() || to == null || to.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        Map<Location, BlockState> map = new HashMap<>();

        Location playerLocation = player.getLocation();

        Location loc1 = playerLocation.clone().subtract(size, size, size), loc2 = playerLocation.clone().add(size, size, size);

        AtomicInteger changedBlocks = new AtomicInteger();


        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().getBlocksBetween(loc1, loc2);

            sync(() -> {
                positions.forEach(location -> {

                    Block block = location.getBlock();

                    for (Pattern fromBlock : from) {

                        boolean byteEquals = (fromBlock.getData() == 0 || fromBlock.getData() == block.getData());

                        if (fromBlock.getMaterial() == block.getType() && byteEquals) {
                            map.put(location.clone(), block.getState());

                            Pattern pattern = to.get(Constants.RANDOM.nextInt(to.size()));

                            Material material = pattern.getMaterial();
                            byte data = pattern.getData();

                            WorldEditAPI.getInstance().setBlock(location, material, data);
                            changedBlocks.getAndIncrement();
                        }
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }

    @Command(name = "/set", aliases = {"set"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <block>")
    public void setCommand(Context<Player> context, String materialString) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> materials = Pattern.parse(materialString);

        if (materials == null || materials.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        if (!worldEditProvider.hasFirstPosition(player)) {
            context.info("worldedit.unknown_position", 1);
            return;
        }

        if (!worldEditProvider.hasSecondPosition(player)) {
            context.info("worldedit.unknown_position", 2);
            return;
        }

        Location first = worldEditProvider.getFirstPosition(player);
        Location second = worldEditProvider.getSecondPosition(player);

        Map<Location, BlockState> map = new HashMap<>();

        AtomicInteger changedBlocks = new AtomicInteger();


        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().getBlocksBetween(first, second);

            sync(() -> {
                positions.forEach(location -> {

                    Pattern pattern = materials.get(Constants.RANDOM.nextInt(materials.size()));

                    Material material = pattern.getMaterial();
                    byte data = pattern.getData();

                    if (location.getBlock().getType() != material || location.getBlock().getData() != data) {
                        map.put(location.clone(), location.getBlock().getState());
                        WorldEditAPI.getInstance().setBlock(location, material, data);
                        changedBlocks.getAndIncrement();
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }

    @Command(name = "/walls", aliases = {"walls"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <block>")
    public void wallsCommand(Context<Player> context, String materialString) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        Player player = context.getSender();

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        List<Pattern> materials = Pattern.parse(materialString);

        if (materials == null || materials.isEmpty()) {
            context.info("worldedit.invalid_materials");
            return;
        }

        if (!worldEditProvider.hasFirstPosition(player)) {
            context.info("worldedit.unknown_position", 1);
            return;
        }

        if (!worldEditProvider.hasSecondPosition(player)) {
            context.info("worldedit.unknown_position", 2);
            return;
        }

        Location first = worldEditProvider.getFirstPosition(player);
        Location second = worldEditProvider.getSecondPosition(player);

        Map<Location, BlockState> map = new HashMap<>();

        AtomicInteger changedBlocks = new AtomicInteger();


        async(() -> {

            List<Location> positions = WorldEditAPI.getInstance().getWalls(first, second);

            sync(() -> {
                positions.forEach(location -> {

                    Pattern pattern = materials.get(Constants.RANDOM.nextInt(materials.size()));

                    Material material = pattern.getMaterial();
                    byte data = pattern.getData();

                    if (location.getBlock().getType() != material || location.getBlock().getData() != data) {
                        map.put(location.clone(), location.getBlock().getState());
                        WorldEditAPI.getInstance().setBlock(location, material, data);
                        changedBlocks.getAndIncrement();
                    }
                });
                context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(changedBlocks.get()));
                if (!map.isEmpty())
                    worldEditProvider.addUndo(player, map);
            });
        });
    }

    private final List<Material> MATERIALS = Arrays.stream(Material.values()).filter(Material::isBlock).collect(Collectors.toList());

    @Command(name = "/undo", aliases = {"undo"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void undoCommand(Context<Player> context) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        WorldEditProvider worldEditProvider = BukkitGame.getEngine().getWorldEditProvider();
        Player sender = context.getSender();

        if (!worldEditProvider.hasAvailableUndo(sender)) {
            context.info("worldedit.no_undo");
            return;
        }

        Map<Location, BlockState> map = worldEditProvider.getUndoList(sender).get(worldEditProvider.getUndoList(sender).size() - 1);

        int amount = 0;

        for (Map.Entry<Location, BlockState> entry : map.entrySet()) {
            Location location = entry.getKey();
            Material material = entry.getValue().getType();
            byte data = entry.getValue().getData().getData();
            WorldEditAPI.getInstance().setBlock(location, material, data);
            amount++;
        }

        map.clear();

        context.info("worldedit.changed_blocks", Constants.DECIMAL_FORMAT.format(amount));
        worldEditProvider.removeUndo(sender, map);
    }


    @Command(name = "/wand", aliases = {"wand"}, platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR)
    public void handleWandCommand(Context<Player> context) {

        if (isRestricted(context)) {
            context.info("flag.locked");
            return;
        }

        context.info("worldedit.wand_receive");
        BukkitGame.getEngine().getWorldEditProvider().giveWand(context.getSender());
    }

    @Completer(name = "/set")
    public List<String> setCompleter(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return MATERIALS.stream().map(m -> m.name().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Completer(name = "/sphere")
    public List<String> sphereCompleter(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return MATERIALS.stream().map(m -> m.name().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Completer(name = "/pyramid")
    public List<String> pyramidCompleter(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return MATERIALS.stream().map(m -> m.name().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Completer(name = "/cyl")
    public List<String> cylCompleter(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return MATERIALS.stream().map(m -> m.name().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Completer(name = "/walls")
    public List<String> wallsCompleter(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            String[] split = context.getArg(0).split(" ");
            String lastArg = split[split.length - 1];
            return MATERIALS.stream().map(m -> m.name().toLowerCase()).filter(name -> startsWith(name, lastArg)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Completer(name = "/replace")
    public List<String> replaceCompleter(Context<CommandSender> context) {
        return MATERIALS.stream().map(m -> m.name().toLowerCase()).filter(name -> startsWith(name, context.getArg(0))).collect(Collectors.toList());
    }

    public boolean isRestricted(Context<Player> context) {
        return context.getAccount().getFlag(Flag.WORLD_EDIT);
    }
}