/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.structure;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.Explorer;
import com.minecraft.hungergames.util.arena.FileArena;
import com.minecraft.hungergames.util.bo3.BO3;
import com.minecraft.hungergames.util.bo3.EmptyBO3;
import com.minecraft.hungergames.util.constructor.Assistance;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class Minifeast implements BukkitInterface, Assistance {

    private MinifeastGenerator minifeastGenerator;

    private ItemStack[] stacks = {new ItemStack(Material.FLINT_AND_STEEL), new ItemStack(Material.WATER_BUCKET),
            new ItemStack(Material.GOLD_PICKAXE), new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GOLD_AXE),
            new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.ENDER_PEARL, random(3)),
            new ItemStack(Material.POTION, 1, (short) 16421), new ItemStack(Material.WEB, random(7)),
            new ItemStack(Material.POTION, 1, (short) 16426), new ItemStack(Material.POTION, 1, (short) 8225),
            new ItemStack(Material.IRON_INGOT, random(2)), new ItemStack(Material.MUSHROOM_SOUP, random(6))};

    private BO3 bO3;
    private Location location;

    public Minifeast(BO3 bo3) {
        this.bO3 = bo3;
    }

    public static Minifeast fetch() {
        BO3 bo3 = FileArena.getArena("minifeast").getBO3();
        return new Minifeast(bo3);
    }

    public Minifeast findLocation(int radius) {
        World world = getGame().getWorld();

        MinifeastGenerator generator = this.minifeastGenerator = new MinifeastGenerator(radius);

        int x = generator.getHighX();
        int z = generator.getHighZ();
        int y = world.getHighestBlockYAt(x, z);

        this.location = new Location(world, x, y, z);
        return this;
    }

    public Minifeast setLocation(final Location location) {
        this.location = location;
        return this;
    }

    public Minifeast prepare() {

        System.out.println("Minifeast -> " + location.getX() + " " + location.getZ());

        for (int x = -3; x < 3; x++) {
            for (int z = -3; z < 3; z++) {
                for (int y = 1; y < 5; y++) {
                    Location loc = this.location.clone().add(x, y, z);
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }
        return this;
    }

    public Minifeast alertExplorers() {
        final Kit EXPLORER = HungerGames.getInstance().getKitStorage().getKit(Explorer.class);

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player == null)
                continue;

            final User user = User.fetch(player.getUniqueId());

            if (EXPLORER.isUser(user)) {
                player.sendMessage("§6§lEXPLORER §eAs coordenadas exatas desse Minifeast são §bX:" + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ());
            }
        }

        return this;
    }

    public Minifeast generate() {

        if (getBO3() instanceof EmptyBO3)
            return this;

        List<Chest> chests = new ArrayList<>();

        bO3.spawn(this.location, (location, pattern) -> {

            if (pattern.getMaterial() == Material.LAPIS_BLOCK) {
                location.getBlock().setType(Material.AIR);
            } else if (pattern.getMaterial() == Material.SIGN_POST) {
                location.getBlock().setType(Material.CHEST);
                chests.add((Chest) location.getBlock().getState());
            } else {
                location.getBlock().setTypeIdAndData(pattern.getMaterial().getId(), pattern.getData(), false);
            }
            return false;
        });

        this.bO3 = new EmptyBO3();
        chests.forEach(this::fill);
        this.stacks = new ItemStack[0];
        return this;
    }

    public void fill(Chest chest) {
        for (ItemStack stack : stacks) {
            if (Constants.RANDOM.nextInt(100) < 30)
                chest.getInventory().setItem(Constants.RANDOM.nextInt(27), stack);
        }
        chest.update();
    }

    public void broadcast() {

        int x = getLocation().getBlockX() - 50;
        int z = getLocation().getBlockZ() - 50;

        int fakeX = getMinifeastGenerator().getLowX();
        int fakeZ = getMinifeastGenerator().getLowZ();

        Bukkit.getOnlinePlayers().forEach(player -> {
            User user = User.fetch(player.getUniqueId());
            player.sendMessage(user.getAccount().getLanguage().translate("hg.minifeast.spawn_broadcast", x, fakeZ, fakeX, z));
        });

        this.minifeastGenerator = null;
    }

    private int random(int i) {
        return Math.max(1, Constants.RANDOM.nextInt(i + 1));
    }

    public void then(Consumer<Minifeast> consumer) {
        consumer.accept(this);
    }

    @Getter
    public static class MinifeastGenerator implements Assistance {

        private final int lowX, lowZ;
        private int highX, highZ;

        public MinifeastGenerator(int max) {

            int x1 = randomize(220, max);
            int x2 = randomize(220, max);

            int z1 = randomize(220, max);
            int z2 = randomize(220, max);

            this.lowX = Math.min(x1, x2);
            this.lowZ = Math.min(z1, z2);
            this.highX = Math.max(x1, x2);
            this.highZ = Math.max(z1, z2);

            if (Math.abs(highX) <= 120) {
                if (highX > 0)
                    highX += 160;
                else
                    highX -= 160;
            }

            if (Math.abs(highZ) <= 120) {
                if (highZ > 0)
                    highZ += 160;
                else
                    highZ -= 160;
            }

        }

        private int randomize(int min, int max) {
            int random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
            return Constants.RANDOM.nextBoolean() ? random_int : -random_int;
        }
    }
}
