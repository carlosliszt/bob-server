package com.minecraft.arcade.tiogerson.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.config.MapConfiguration;
import com.minecraft.arcade.tiogerson.mode.Mode;
import com.minecraft.arcade.tiogerson.player.UserDeathEvent;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.room.team.Team;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.util.FireworkAPI;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import com.minecraft.arcade.tiogerson.util.visibility.Visibility;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Tables;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.github.paperspigot.Title;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServerListener implements Listener, BukkitInterface {

    private final ArcadeMain instance;

    public ServerListener(ArcadeMain arcadeMain) {
        this.instance = arcadeMain;
    }

    @EventHandler
    public void onDeath(UserDeathEvent event) {
        User user = event.getUser();

        Player player = user.getPlayer();

        player.closeInventory();

        Room room = user.getRoom();
        Mode mode = room.getMode();

        Team team = room.getEnzo().getMembers().contains(user) ? room.getEnzo() : room.getTioGerson();

        team.getMembers().remove(user);
        room.getAlivePlayers().remove(user);

        Account account = user.getAccount();

        if (!event.isDefinitelyLeft()) {
            room.getSpectators().add(user);
            mode.join(user, PlayMode.VANISH);

            if(event.getKiller() != null) {
                User killer = event.getKiller();
                Team killerTeam = room.getEnzo().getMembers().contains(killer) ? room.getEnzo() : room.getTioGerson();
                room.getWorld().getPlayers().forEach(players -> players.sendMessage(team.getChatColor() + user.getName() + "§e foi morto por " + killerTeam.getChatColor() + killer.getPlayer().getName() + "."));
            } else {
                room.getWorld().getPlayers().forEach(players -> players.sendMessage(team.getChatColor() + user.getName() + "§e morreu."));
            }

            for (int i = 0; i < 4; i++)
                FireworkAPI.random(player.getLocation());

            event.getUser().getPlayer().sendTitle(new Title("§c§lVOCÊ MORREU!", "§eVocê não conseguiu escapar do TIO GERSON!", 1, 40, 10));
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 3F, 1F);

            if (room.isCountStats()) {
                account.addInt(1, mode.getLoses());
                account.getData(mode.getWinstreak()).setData(0);
            }
        }

        Visibility.refresh(player);

        if (room.isCountStats()) {
            async(() -> {
                user.getAccount().getDataStorage().saveTable(Tables.TIOGERSON);
            });
        }
    }

    private final ImmutableSet<Material> CHECK_MATERIALS = Sets.immutableEnumSet(Material.CHEST, Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.FURNACE, Material.JUKEBOX, Material.ENDER_CHEST, Material.HOPPER, Material.HOPPER_MINECART, Material.DROPPER, Material.DISPENSER);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;

        Block block = event.getClickedBlock();
        World world = block.getWorld();
        Room room = instance.getRoomStorage().getRoom(world);

        if (room == null || room.getRollback().contains(block)) {
            return;
        }

        if (CHECK_MATERIALS.contains(block.getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStopDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.isCancelled())
                return;

            Player p = (Player) event.getEntity();

            if (p.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true);
                p.setHealth(20.0D);

                User user = User.fetch(p.getUniqueId());
                new UserDeathEvent(user, new ArrayList<>(user.getRoom().getTioGerson().getMembers()).get(0), false).fire();

            }
        }
    }

    @EventHandler
    public void ifDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.getDrops().clear();
    }

    @EventHandler
    public void onHeartbeat(ServerHeartbeatEvent event) {
        if (event.isPeriodic(20)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = User.fetch(player.getUniqueId());

                if (user == null)
                    continue;

                if (Vanish.getInstance().isVanished(player.getUniqueId()))
                    continue;

                Room room = user.getRoom();

                if (room == null)
                    continue;

                MapConfiguration mapConfiguration = user.getRoom().getMapConfiguration();

                double x = player.getLocation().getX();
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ();

                if (y >= mapConfiguration.getHeight()) {
                    player.teleport(mapConfiguration.getSpawnPoint());
                }
            }
        }
    }

}