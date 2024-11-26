package com.minecraft.hungergames.user.celebrations.list;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.celebrations.Celebration;
import com.minecraft.hungergames.user.celebrations.pattern.CelebrationRarity;
import com.minecraft.hungergames.util.celebrations.Particles;
import com.minecraft.hungergames.util.celebrations.UtilParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.github.paperspigot.Title;

import java.util.ArrayList;
import java.util.List;

public class Rocket extends Celebration {

    public static final List<Block> BLOCKS = new ArrayList<>();

    boolean launching;
    ArmorStand armorStand;
    List<FallingBlock> fallingBlocks = new ArrayList<>();

    public Rocket(HungerGames hungerGames) {
        super(hungerGames);
        setDisplayName("Foguete");
        setDescription("Use seu foguete para decolar e ir até o espaço!");
        setIcon(Pattern.of(Material.FIREWORK));
        setRarity(CelebrationRarity.EPIC);
        setRank(Rank.BLAZE_PLUS);
    }

    @Override
    public void onVictory(Player player) {

        player.setVelocity(new Vector(0, 3, 0));
        final Location loc = player.getLocation();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + 0.5);
        Bukkit.getScheduler().runTaskLater(getHungerGames(), () -> {
            for (int i = 0; i < 2; i++) {
                Block b1 = loc.clone().add(1, i, 0).getBlock();
                Block b2 = loc.clone().add(-1, i, 0).getBlock();
                Block b3 = loc.clone().add(0, i, 1).getBlock();
                Block b4 = loc.clone().add(0, i, -1).getBlock();
                Block b5 = loc.clone().add(0, i + 1, 0).getBlock();
                b1.setType(Material.FENCE);
                b2.setType(Material.FENCE);
                b3.setType(Material.FENCE);
                b4.setType(Material.FENCE);
                b5.setType(Material.QUARTZ_BLOCK);
                BLOCKS.add(b1);
                BLOCKS.add(b2);
                BLOCKS.add(b3);
                BLOCKS.add(b4);
                BLOCKS.add(b5);
            }
            armorStand = loc.getWorld().spawn(loc.add(0, 1.5, 0), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
        }, 10);
        Bukkit.getScheduler().runTaskLater(getHungerGames(), () -> {
            armorStand.setPassenger(player);
            BukkitRunnable runnable = new BukkitRunnable() {
                int i = 5;

                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        return;
                    }

                    if (i > 0) {
                        player.sendTitle(new Title(ChatColor.RED + "" + ChatColor.BOLD + i, ""));
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + i);
                        player.playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 1.0f, 1.0f);
                        i--;
                    } else {

                        player.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                        armorStand.remove();
                        armorStand = null;


                        for (Block block : BLOCKS) {
                            block.setType(Material.AIR);
                        }

                        BLOCKS.clear();

                        final FallingBlock top = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 3, 0), Material.QUARTZ_BLOCK, (byte) 0);
                        FallingBlock base = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 2, 0), Material.QUARTZ_BLOCK, (byte) 0);
                        for (int i = 0; i < 2; i++) {
                            FallingBlock fence1 = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 1 + i, 1), Material.FENCE, (byte) 0);
                            FallingBlock fence2 = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 1 + i, -1), Material.FENCE, (byte) 0);
                            FallingBlock fence3 = player.getWorld().spawnFallingBlock(player.getLocation().add(1, 1 + i, 0), Material.FENCE, (byte) 0);
                            FallingBlock fence4 = player.getWorld().spawnFallingBlock(player.getLocation().add(-1, 1 + i, 0), Material.FENCE, (byte) 0);
                            fallingBlocks.add(fence1);
                            fallingBlocks.add(fence2);
                            fallingBlocks.add(fence3);
                            fallingBlocks.add(fence4);
                        }

                        fallingBlocks.add(top);
                        fallingBlocks.add(base);
                        if (fallingBlocks.get(8).getPassenger() == null) {
                            fallingBlocks.get(8).setPassenger(player);
                        }
                        top.setPassenger(player);
                        launching = true;
                        Bukkit.getScheduler().runTaskLater(getHungerGames(), () -> {
                            fallingBlocks.forEach(Entity::remove);
                            fallingBlocks.clear();
                            player.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                            UtilParticles.display(Particles.EXPLOSION_HUGE, player.getLocation());
                            launching = false;
                        }, 100);
                        cancel();
                    }
                }
            };
            runnable.runTaskTimer(getHungerGames(), 0, 20);
        }, 12);

        new BukkitRunnable() {
            public void run() {
                if (armorStand != null) {
                    if (armorStand.getPassenger() == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                armorStand.setPassenger(player);
                            }
                        }.runTask(getHungerGames());
                    }

                    UtilParticles.display(Particles.SMOKE_LARGE, 0.3f, 0.2f, 0.3f, armorStand.getLocation().add(0, -3, 0), 10);
                    armorStand.getLocation().getWorld().playSound(armorStand.getLocation().clone().add(0, -3, 0), Sound.FIZZ, 0.025f, 1.0f);
                }
                for (FallingBlock fallingBlock : fallingBlocks) {
                    fallingBlock.setVelocity(new Vector(0, 0.8, 0));
                }
                if (launching && !fallingBlocks.isEmpty()) {
                    if (fallingBlocks.get(8).getPassenger() == null) {
                        fallingBlocks.get(8).setPassenger(player);
                    }
                    UtilParticles.display(Particles.FLAME, 0.3f, 0.2f, 0.3f, player.getLocation().add(0, -3, 0), 10);
                    UtilParticles.display(Particles.LAVA, 0.3f, 0.2f, 0.3f, player.getLocation().add(0, -3, 0), 10);
                    fallingBlocks.get(9).getWorld().playSound(fallingBlocks.get(9).getLocation().clone().add(0, -1, 0), Sound.BAT_LOOP, 1.5F, 1.0f);
                    fallingBlocks.get(9).getWorld().playSound(fallingBlocks.get(9).getLocation().clone().add(0, -1, 0), Sound.FIZZ, 0.025f, 1.0f);
                }
            }
        }.runTaskTimer(BukkitGame.getEngine(), 0, 5);
    }

}