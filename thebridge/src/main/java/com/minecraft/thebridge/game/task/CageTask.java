package com.minecraft.thebridge.game.task;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.thebridge.game.Game;
import com.minecraft.thebridge.game.cage.Cage;
import com.minecraft.thebridge.game.enums.GameStage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.github.paperspigot.Title;

import java.util.concurrent.atomic.AtomicInteger;

public class CageTask {

    private final Game game;
    private final Cage blueCage, redCage;

    private final Location blue, red;

    private String title;

    protected BukkitTask scheduler;

    public CageTask(final Game game) {
        this.game = game;
        this.title = "";

        this.blue = game.getConfiguration().getBlueCage();
        this.red = game.getConfiguration().getRedCage();

        this.blueCage = game.getBlue().getUserList().get(0).getCage();
        this.redCage = game.getRed().getUserList().get(0).getCage();
    }

    public void run() {
        if (this.game.getStage() != GameStage.PLAYING)
            return;

        this.blueCage.spawnBlue(this.blue);
        this.redCage.spawnRed(this.red);

        final boolean countStats = this.game.isCountStats();

        game.getBlue().getUsers().forEach(user -> {
            user.giveItems(game.getBlue());
            user.setInCage(true);

            final Player player = user.getPlayer();

            player.getActivePotionEffects().clear();
            if(user.getCage().getDisplayName().equalsIgnoreCase("floreto")) {
                player.teleport(this.blue.clone().add(0,2,0));
            } else {
                player.teleport(this.blue.clone().add(0,1,0));
            }
            player.setHealth(20.0D);
            player.setVelocity(new Vector(0,0,0));
            player.setItemOnCursor(null);
            player.getOpenInventory().getTopInventory().clear();

            if (countStats)
                user.getAccount().addInt(1, game.getType().getRounds());
        });

        game.getRed().getUsers().forEach(user -> {
            user.giveItems(game.getRed());
            user.setInCage(true);

            final Player player = user.getPlayer();

            player.getActivePotionEffects().clear();
            if(user.getCage().getDisplayName().equalsIgnoreCase("floreto")) {
                player.teleport(this.red.clone().add(0,2,0));
            } else {
                player.teleport(this.red.clone().add(0,1,0));
            }
            player.setVelocity(new Vector(0,0,0));
            player.setHealth(20.0D);

            player.setItemOnCursor(null);
            player.getOpenInventory().getTopInventory().clear();

            if (countStats)
                user.getAccount().addInt(1, game.getType().getRounds());
        });

        final AtomicInteger atomicInteger = new AtomicInteger(5);

        this.scheduler = Bukkit.getScheduler().runTaskTimer(BukkitGame.getEngine(), () -> {
            atomicInteger.getAndDecrement();

            if (game.getStage() != GameStage.PLAYING) {
                destroy();
                return;
            }

            if (game.getWorld().getPlayers().size() == 0) {
                destroy();
                return;
            }

            int time = atomicInteger.get();

            if (time <= 0) {
                destroy();
                return;
            }

            game.playSound(Sound.CLICK, 3.5F, 3.5F);
            game.sendMessage("§eAbrindo cabines em: §c" + time + " §esegundo" + (time > 1 ? "s." : "."));
            if (!this.title.equalsIgnoreCase(""))
                game.playSound(Sound.FIREWORK_LAUNCH, 3F, 1F);
            game.sendTitle(new Title(this.title, "§7Cabines abrem em §a" + time + "...", 1, 21, 10));
        }, 1L, 20L);
    }

    protected void destroy() {
        if (this.scheduler != null) {

            this.blueCage.destroyBlue();
            this.redCage.destroyRed();

            this.game.playSound(Sound.NOTE_PLING, 3.5F, 3.5F);
            this.game.getAliveUsers().forEach(user -> user.setInCage(false));

            this.scheduler.cancel();
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }
}