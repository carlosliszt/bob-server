package com.minecraft.lobby.feature.chair;

import com.minecraft.lobby.Lobby;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class ChairStairs extends BukkitRunnable implements Listener {

    private final Map<UUID, ArmorStand> seats;

    public ChairStairs(final Lobby hub) {
        this.seats = new HashMap<>();
        this.runTaskTimerAsynchronously(hub, 0L, 1L);
        hub.getServer().getPluginManager().registerEvents(this, hub);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();

        if (block == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();

        if (player.isInsideVehicle()) return;

        if (player.getItemInHand().getType() != Material.AIR) return;

        if (!block.getType().name().contains("STAIR")) return;

        if (isSitting(player)) return;

        final Location location = block.getLocation();

        final ArmorStand armorStand = location.getWorld().spawn(location.add(0.5D, -1.2D, 0.5D), ArmorStand.class);

        armorStand.setCustomName(player.getName() + "' chair");

        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomNameVisible(false);

        armorStand.setPassenger(player);
        getSeats().put(player.getUniqueId(), armorStand);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (isSitting(player))
            rise(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        if (isSitting(player))
            rise(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (isSitting(player))
            rise(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDismountEvent(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        final Player player = (Player) event.getEntity();

        if (isSitting(player))
            rise(player);
    }

    public void rise(final Player player) {
        if (!isSitting(player)) return;

        final ArmorStand armorStand = getSeats().get(player.getUniqueId());

        if (armorStand == null) return;

        getSeats().remove(player.getUniqueId());

        player.eject();

        armorStand.remove();
    }

    public boolean isSitting(final Player player) {
        return getSeats().containsKey(player.getUniqueId());
    }

    @Override
    public void run() {
        for (ArmorStand armorstand : getSeats().values())
            ((CraftArmorStand) armorstand).getHandle().yaw = armorstand.getPassenger().getLocation().getYaw();
    }

}