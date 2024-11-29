package com.minecraft.core.bukkit.accessory.list.particles;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.particles.list.ParticleCollector;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ParticlesAccessory extends Accessory {
    private final Map<Player, BukkitTask> tasks = new HashMap<>();
    private int x = 0;
    private int B = 0;

    public ParticlesAccessory(String uniqueCode, String name, String permission, Rank rank, ItemStack itemStack, String description, boolean persistent, AccessoryType type, AccessoryRarity rarity) {
        super(uniqueCode, name, permission, rank, itemStack, description, persistent, type, rarity);
    }

    @Override
    public void updateAsync(Player player, ServerHeartbeatEvent event) { }

    @Override
    public void give(Player player) {
        Account account = Account.fetch(player.getUniqueId());

        account.setAlpha(0.0D);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if(Account.fetch(player.getUniqueId()) != null) {
                    Account.fetch(player.getUniqueId()).setAlpha(Account.fetch(player.getUniqueId()).getAlpha() + (Math.PI / 16));
                    double alphaNumeric =  Account.fetch(player.getUniqueId()).getAlpha();
                    Location loc = player.getLocation();
                    Location firstLocation = loc.clone().add(Math.cos(alphaNumeric), Math.sin(alphaNumeric) + 1.0, Math.sin(alphaNumeric));
                    Location secondLocation = loc.clone().add(Math.cos(alphaNumeric + Math.PI), Math.sin(alphaNumeric) + 1.0, Math.sin(alphaNumeric + Math.PI));

                    String dataString = account.getDataStorage().getData(Columns.PARTICLE).getAsString();
                    for (ParticleCollector particle : ParticleCollector.values()) {
                        if (particle.getDisplay().toLowerCase().equals(dataString)) {
                            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(particle.getParticle(), true, (float) firstLocation.getX(), (float) firstLocation.getY(), (float) firstLocation.getZ(), 0, 0, 0, 0, 1);
                            PacketPlayOutWorldParticles packetPlayOutWorldParticles2 = new PacketPlayOutWorldParticles(particle.getParticle(), true, (float) secondLocation.getX(), (float) secondLocation.getY(), (float) secondLocation.getZ(), 0, 0, 0, 0, 1);

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
                                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles2);
                            }
                        }
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(BukkitGame.getEngine(), 0, 3);

        tasks.put(player, task);
    }

    @Override
    public void remove(Player player) { }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BukkitTask task = tasks.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
}
