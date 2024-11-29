package com.minecraft.lobby.feature.parkour;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.user.User;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Parkour implements Listener, BukkitInterface {

    private final Location startLocation, endLocation;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private Columns recordColumn;

    InteractableItem resetParkour = new InteractableItem(new ItemFactory().setType(Material.RECORD_7).setName("§aReiniciar o Parkour").getStack(), new InteractableItem.Interact() {
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            User user = User.fetch(player.getUniqueId());
            if (user.isParkourMode()) {
                user.resetCheckPoints();
                user.setParkourTime(0);
                user.getPlayer().teleport(startLocation);
                return true;
            }
            return false;
        }
    });

    InteractableItem lastCheckpoint = new InteractableItem(new ItemFactory().setType(Material.GOLD_PLATE).setName("§aÚltimo checkpoint").getStack(), new InteractableItem.Interact() {
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            User user = User.fetch(player.getUniqueId());
            if (user.isParkourMode()) {
                if (user.getHighestCheckpoint() != null) {
                    player.teleport(user.getHighestCheckpoint().getLocation());
                    player.sendMessage("§b§lPARKOUR§a Teleportado para o último checkpoint!");
                    return true;
                } else {
                    player.teleport(startLocation);
                    player.sendMessage("§b§lPARKOUR§a Teleportado para o início do parkour!");
                    return true;
                }
            }
            return false;
        }
    });

    InteractableItem stopParkour = new InteractableItem(new ItemFactory().setType(Material.REDSTONE).setName("§aParar o Parkour").getStack(), new InteractableItem.Interact() {
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            User user = User.fetch(player.getUniqueId());
            if (user.isParkourMode()) {
                resetParkour(player, user);

                player.sendMessage("§b§lPARKOUR§c Parkour parado!");
                return true;
            }
            return false;
        }
    });

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
                        player.sendMessage("§b§lPARKOUR§a Você completou o §echeckpoint #" + checkpoint.getId() + "§a em §7" + formatSeconds(user.getParkourTime()) + "§a!");
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
                user.getPlayer().setAllowFlight(false);
                user.getPlayer().setFlying(false);

                player.getInventory().setItem(3, resetParkour.getItemStack());
                player.getInventory().setItem(4, lastCheckpoint.getItemStack());
                player.getInventory().setItem(5, stopParkour.getItemStack());

            } else {
                player.sendMessage("§b§lPARKOUR§a Temporizador reiniciado!");

                user.resetCheckPoints();
                user.setParkourTime(0);
                user.getPlayer().setAllowFlight(false);
                user.getPlayer().setFlying(false);
            }
        }

        if (player.getLocation().distance(endLocation) < 3) {
            if (user.isParkourMode()) {
                if (user.getCheckpoints().values().containsAll(checkpoints)) {
                    player.sendMessage("§b§lPARKOUR§a Parabéns, você completou o parkour em " + formatSeconds(user.getParkourTime()) + "!");
                    Hall hall = user.getHall();
                    if (user.getAccount().getData(recordColumn).getAsLong() > user.getParkourTime() || user.getAccount().getData(recordColumn).getAsLong() == 0) {
                        player.sendMessage("§b§lPARKOUR§a Você bateu o recorde pessoal por " + (user.getAccount().getData(recordColumn).getAsLong() - user.getParkourTime()) + " segundos!");
                        hall.getBestTime().updateText(0, "§eRecorde pessoal: §7" + Parkour.formatSeconds(user.getParkourTime()));
                        user.getAccount().getData(recordColumn).setData(user.getParkourTime());
                        async(() -> {
                            user.getAccount().getDataStorage().saveTable(Tables.LOBBY_PARKOUR);
                        });
                    } else {
                        player.sendMessage("§b§lPARKOUR§a Seu tempo foi de §c" + (user.getParkourTime() - user.getAccount().getData(recordColumn).getAsLong()) + "§a segundos a mais que o recorde pessoal!");
                    }
                    resetParkour(player, user);

                } else {
                    player.sendMessage("§b§lPARKOUR§c Você não completou o parkour corretamente!");
                    user.setParkourMode(false);
                    user.resetCheckPoints();
                    user.setParkourTime(0);
                }
            }
        }
    }

    private void resetParkour(Player player, User user) {
        user.setParkourMode(false);
        user.resetCheckPoints();
        user.setParkourTime(0);

        InteractableItem collectibles = new InteractableItem(new ItemFactory().setType(Material.CHEST).setName("§aColetáveis").getStack(), new InteractableItem.Interact() {
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.sendMessage("§cEm breve!");
                return false;
            }
        });

        player.getInventory().setItem(3, new ItemStack(Material.AIR));
        player.getInventory().setItem(4, collectibles.getItemStack());
        player.getInventory().setItem(5, new ItemStack(Material.AIR));

        if (user.getAccount().getProperty("lobby.fly", false).getAsBoolean()) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onCheck(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        if (event.getPlayer().getLocation().getBlock().getType() == Material.GOLD_PLATE) {
            if (!User.fetch(event.getPlayer().getUniqueId()).isPlateDelay() && !User.fetch(event.getPlayer().getUniqueId()).isPlateKillSwitch()) {
                User.fetch(event.getPlayer().getUniqueId()).setPlateDelay(true);
                //User.fetch(event.getPlayer().getUniqueId()).setPlateKillSwitch(true);
                verifyStart(event.getPlayer());
                verifyCheckpoint(event.getPlayer());
            }
        }
    }

}