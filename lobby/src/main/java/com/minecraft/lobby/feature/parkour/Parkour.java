package com.minecraft.lobby.feature.parkour;

import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.user.User;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Parkour implements Listener {

    private final Location startLocation, endLocation;
    private final List<Checkpoint> checkpoints = new ArrayList<>();

    public Parkour(Lobby lobby, final Location startLocation, final Location endLocation, Checkpoint... checkpoints) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.checkpoints.addAll(Arrays.asList(checkpoints));
        lobby.getServer().getPluginManager().registerEvents(this, lobby);
    }

    public static String formatSeconds(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%dm %ds", minutes, seconds);
    }

    protected void verifyCheckpoint(final Player player) {
        User user = User.fetch(player.getUniqueId());

        for (Checkpoint checkpoint : checkpoints) {
            if (player.getLocation().distance(checkpoint.getLocation()) < 3) {
                if (user.isParkourMode()) {

                    if (user.getCheckpoints().containsValue(checkpoint)) {
                        player.sendMessage("§b§lPARKOUR§c Você já passou por este checkpoint!");
                        return;
                    }

                    if (user.getCheckpoints().size() == checkpoint.getId() - 1) {
                        user.addCheckpoint(checkpoint, checkpoint.getId());
                        player.sendMessage("§b§lPARKOUR§a Checkpoint " + checkpoint.getId() + " alcançado!");
                    } else {
                        player.sendMessage("§b§lPARKOUR§c Você não passou pelo checkpoint anterior!");
                    }
                }
            }
        }
    }

    protected void verifyStart(final Player player) {
        User user = User.fetch(player.getUniqueId());
        if (player.getLocation().distance(startLocation) < 3) {
            if (!user.isParkourMode()) {
                player.sendMessage("§b§lPARKOUR§a Iniciado!");
                user.setParkourMode(true);
            } else {
                player.sendMessage("§b§lPARKOUR§a Temporizador reiniciado!");
                user.resetCheckPoints();
                user.setParkourTime(0);
            }
        }

        if (player.getLocation().distance(endLocation) < 3) {
            if (user.isParkourMode()) {
                if (user.getCheckpoints().values().containsAll(checkpoints)) {
                    player.sendMessage("§b§lPARKOUR§a Parabéns, você completou o parkour em " + formatSeconds(user.getParkourTime()) + "!");
                    user.setParkourMode(false);
                    user.resetCheckPoints();
                    user.setParkourTime(0);
                } else {
                    player.sendMessage("§b§lPARKOUR§c Você não completou o parkour corretamente!");
                    user.setParkourMode(false);
                    user.resetCheckPoints();
                    user.setParkourTime(0);
                }
            }
        }
    }

    @EventHandler
    public void onCheck(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        if (event.getPlayer().getLocation().getBlock().getType() == Material.GOLD_PLATE) {
            if (!User.fetch(event.getPlayer().getUniqueId()).isPlateDelay()) {
                User.fetch(event.getPlayer().getUniqueId()).setPlateDelay(true);
                verifyStart(event.getPlayer());
            }
        } else if (event.getPlayer().getLocation().getBlock().getType() == Material.IRON_PLATE) {
            if (!User.fetch(event.getPlayer().getUniqueId()).isPlateDelay()) {
                User.fetch(event.getPlayer().getUniqueId()).setPlateDelay(true);
                verifyCheckpoint(event.getPlayer());
            }
        }
    }

}