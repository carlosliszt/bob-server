package com.minecraft.core.bukkit.util.title;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.list.title.TitleAccessory;
import com.minecraft.core.bukkit.util.scoreboard.AnimatedString;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TitleHandler {

    @Getter
    private static TitleHandler instance = new TitleHandler();

    private HashMap<Player, TitleObject> titles = new HashMap<>();
    private static int tickCounter = 0;


    public void setTitle(final Player player, TitleAccessory.TitleAnimation animation, final String title) {

        if (titles.containsKey(player)) {
            removeTitle(player);
        }

        Location loc = player.getLocation().add(0, 2.1, 0);
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(worldServer);
        armorStand.setInvisible(true);
        armorStand.setCustomName(title);
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setPosition(loc.getX(), loc.getY(), loc.getZ());
        armorStand.getDataWatcher().watch(10, (byte) 0x10);

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(armorStand);
        sendPacketToAllPlayers(player, spawnPacket);

        titles.put(player, new TitleObject(player, title, Collections.singletonList(armorStand)));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Location updatedLoc = player.getLocation().add(0, 2.1, 0);
                armorStand.setPosition(updatedLoc.getX(), updatedLoc.getY(), updatedLoc.getZ());
                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(armorStand);
                sendPacketToAllPlayers(player, teleportPacket);
            }
        }.runTaskTimer(JavaPlugin.getProvidingPlugin(this.getClass()), 1L, 1L);

        switch (animation) {
            case NONE: {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.setCustomName(title);
                        DataWatcher watcher = armorStand.getDataWatcher();
                        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(
                                armorStand.getId(),
                                watcher,
                                true
                        );
                        sendPacketToAllPlayers(player, metadataPacket);
                    }
                }.runTaskTimer(BukkitGame.getEngine(), 0L, 3L);
                break;
            }

            case RAINBOW: {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.setCustomName("§6✯ " + getRainbowTitle(title) + "§r§6 ✯");
                        DataWatcher watcher = armorStand.getDataWatcher();
                        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(
                                armorStand.getId(),
                                watcher,
                                true
                        );
                        tickCounter++;
                        sendPacketToAllPlayers(player, metadataPacket);
                    }
                }.runTaskTimer(BukkitGame.getEngine(), 0L, 3L);
                break;
            }

            case FADE: {
                AnimatedString wave = new AnimatedString(title, "§f§l", "§e§l", "§6§l");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        armorStand.setCustomName("§6✯ " + wave.next() + "§r§6 ✯");
                        DataWatcher watcher = armorStand.getDataWatcher();
                        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(
                                armorStand.getId(),
                                watcher,
                                true
                        );

                        sendPacketToAllPlayers(player, metadataPacket);
                    }
                }.runTaskTimer(BukkitGame.getEngine(), 0L, 2L);
                break;
            }

        }

    }

    public void removeTitle(Player player) {
        while (titles.containsKey(player)) {
            List<EntityArmorStand> armorStandList = new ArrayList<>(titles.remove(player).getArmorStands());
            while (!armorStandList.isEmpty()) {
                EntityArmorStand stand = armorStandList.remove(0);
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(stand.getId());
                sendPacketToAllPlayers(player, destroyPacket);
            }
        }
    }

    private void sendPacketToAllPlayers(Player target, Object packet) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.canSee(target)) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet) packet);
            }
        }
    }

    private String getRainbowTitle(String title) {
        ChatColor[] colors = {
                ChatColor.DARK_RED,
                ChatColor.RED,
                ChatColor.GOLD,
                ChatColor.YELLOW,
                ChatColor.GREEN,
                ChatColor.DARK_GREEN,
                ChatColor.BLUE,
                ChatColor.DARK_AQUA,
                ChatColor.LIGHT_PURPLE
        };

        StringBuilder rainbowTitle = new StringBuilder();
        int colorLength = colors.length;
        int titleLength = title.length();

        for (int i = 0; i < titleLength; i++) {
            int colorIndex = (colorLength + (tickCounter - i) % colorLength) % colorLength;
            rainbowTitle.append(colors[colorIndex]).append(ChatColor.BOLD).append(title.charAt(i));
        }

        return rainbowTitle.toString();
    }

}
