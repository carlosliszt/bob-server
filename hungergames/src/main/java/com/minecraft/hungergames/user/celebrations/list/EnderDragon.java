package com.minecraft.hungergames.user.celebrations.list;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.celebrations.Celebration;
import com.minecraft.hungergames.user.celebrations.pattern.CelebrationRarity;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EnderDragon extends Celebration {

    public EnderDragon(HungerGames hungerGames) {
        super(hungerGames);
        setDisplayName("Domador de dragão");
        setDescription("Monte em um dragão capaz de destruir o mapa.");
        setIcon(Pattern.of(Material.EYE_OF_ENDER));
        setRarity(CelebrationRarity.LEGENDARY);
        setRank(Rank.BLAZE_PLUS);
    }

    @Override
    public void onVictory(Player player) {
        org.bukkit.entity.EnderDragon entity = player.getWorld().spawn(player.getLocation(), org.bukkit.entity.EnderDragon.class);

        entity.setCustomNameVisible(false);
        entity.setCustomName("§d§lVITÓRIA DE " + player.getName().toUpperCase());
        entity.setPassenger(player);

        new BukkitRunnable() {
            public void run() {
                if (entity.getPassenger() == null) {
                    cancel();
                    return;
                }

                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Vector vector = player.getLocation().toVector();

                double rotX = player.getLocation().getYaw();
                double rotY = player.getLocation().getPitch();

                vector.setY(-Math.sin(Math.toRadians(rotY)));

                double h = Math.cos(Math.toRadians(rotY));

                vector.setX(-h * Math.sin(Math.toRadians(rotX)));
                vector.setZ(h * Math.cos(Math.toRadians(rotX)));

                EntityEnderDragon ec = ((CraftEnderDragon) entity).getHandle();

                ec.hurtTicks = -1;

                ec.getBukkitEntity().setVelocity(vector);

                ec.pitch = player.getLocation().getPitch();
                ec.yaw = player.getLocation().getYaw() - 180;
            }
        }.runTaskTimer(BukkitGame.getEngine(), 0, 1);
    }

}