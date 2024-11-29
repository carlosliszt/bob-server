package com.minecraft.core.bukkit.accessory.list.gadget.runnable;

import com.minecraft.core.bukkit.BukkitGame;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class BalloonObject {

    private static final Random random = new Random();

    private Location location;

    private boolean spawned = false;

    public BalloonObject(Location location) {
        this.location = location;
    }

    private ItemStack randomItem() {
        ItemStack item = new ItemStack(Material.STAINED_CLAY);
        item.setDurability((short)random.nextInt(15));
        return item;
    }

    public void spawn() {

        int startY = location.getBlockY();

        List<Location> locations = new ArrayList<>();

        int numballoons = 20 + random.nextInt(10);

        for (int i = 0; i < numballoons; i++) {
            int x = 5 + random.nextInt(10);
            if (random.nextBoolean()) {
                x = -x;
            }
            int y = 0 + random.nextInt(5);
            int z = 5 + random.nextInt(10);
            if (random.nextBoolean()) {
                z = -z;
            }
            Location balloonLoc = location.clone().add(x, y, z);
            locations.add(balloonLoc);
        }

        this.spawned = true;

        List<LivingEntity> balloons = new ArrayList<>();
        for (Location loc : locations) {
            LivingEntity entity;
            if (random.nextBoolean()) {
                Giant armorStand = (Giant) loc.getWorld().spawnEntity(loc.clone(), EntityType.GIANT);
                armorStand.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 * 20, 5));
                armorStand.setCustomNameVisible(false);
                armorStand.getEquipment().setItemInHand(randomItem());
                entity = armorStand;
            } else {
                ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone(), EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setCustomNameVisible(false);
                armorStand.setGravity(true);// false
                armorStand.setHelmet(randomItem());
                entity = armorStand;
            }
            entity.setMetadata("balloon", new FixedMetadataValue(JavaPlugin.getPlugin(BukkitGame.class), "balloon"));
            balloons.add(entity);
        }


        int endY = startY + 80;
        long endTime = System.currentTimeMillis()+(15*1000L);
        new BalloonRunnable(balloons, endTime, endY).runTaskTimer(JavaPlugin.getPlugin(BukkitGame.class), 0L, 2L);

    }

    public void destroy() {
        this.spawned = false;
    }

}
