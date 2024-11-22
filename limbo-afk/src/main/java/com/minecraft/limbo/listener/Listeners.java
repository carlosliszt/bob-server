package com.minecraft.limbo.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.ServerCategory;
import com.minecraft.core.server.ServerType;
import com.minecraft.limbo.Limbo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.github.paperspigot.Title;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Listeners implements Listener {

    private List<UUID> sentPacket = new ArrayList<>();

    public Listeners() {

        Command command = new Command("forcelobby") {
            @Override
            public boolean execute(CommandSender commandSender, String s, String[] strings) {
                if (commandSender.isOp()) {
                    List<Server> serverList = Constants.getServerStorage().getServers(ServerType.MAIN_LOBBY);
                    serverList.removeIf(Server::isDead);

                    if (serverList.isEmpty())
                        return false;

                    for (Player player : Bukkit.getOnlinePlayers())
                        connect(player, serverList.get(Constants.RANDOM.nextInt(serverList.size())));
                }
                return true;
            }
        };

        command.setAliases(Collections.emptyList());

        Bukkit.getCommandMap().register("forcelobby", command);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.getPlayer().getTicksLived() < 5)
            return;

        UUID uuid = event.getPlayer().getUniqueId();

        if (sentPacket.contains(uuid))
            return;

        Server server = ServerCategory.LOBBY.getServerFinder().getBestServer(ServerType.MAIN_LOBBY);

        System.out.println(event.getPlayer().getName() + " is not away anymore... (Toal: " + Bukkit.getOnlinePlayers().size() + ")");

        if (server == null)
            event.getPlayer().kickPlayer("&cNão há servidores disponíveis de lobby, tente novamente mais tarde!");
        else {
            event.getPlayer().hideTitle();
            sentPacket.add(uuid);
            connect(event.getPlayer(), server);
            Bukkit.getScheduler().runTaskLater(Limbo.getEngine(), () -> sentPacket.remove(uuid), 20L);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setTicksLived(1);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setLevel(0);
        player.setExp(0);
        player.setGameMode(GameMode.SURVIVAL);

        player.sendTitle(
                new Title("§c§lVOCê ESTÁ AFK!",
                        "§eSe mova para voltar ao lobby.",
                        5,
                        Integer.MAX_VALUE,
                        99999));


        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        Bukkit.getOnlinePlayers().forEach(other -> {
            player.hidePlayer(other);
            other.hidePlayer(player);
        });
    }

    @EventHandler
    public void onPlayerInitialSpawnEvent(PlayerInitialSpawnEvent event) {
        event.setSpawnLocation(new Location(Bukkit.getWorlds().get(0), 0, 28, 0));
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onBreakBlocks(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlaceBlocks(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    protected void connect(Player player, Server server) {
        connect(player, new ServerRedirect(player.getUniqueId(), new ServerRedirect.Route(server, null)));
    }

    protected void connect(Player player, ServerRedirect route) {
        try {
            String message = Constants.GSON.toJson(route);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF(message);
            player.sendPluginMessage(Limbo.getEngine(), "Redirection", b.toByteArray());
            b.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
