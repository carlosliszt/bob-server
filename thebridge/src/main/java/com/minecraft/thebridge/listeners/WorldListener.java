package com.minecraft.thebridge.listeners;

import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.thebridge.TheBridge;
import com.minecraft.thebridge.game.Game;
import com.minecraft.thebridge.game.enums.GameStage;
import com.minecraft.thebridge.user.User;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Set;

public class WorldListener implements Listener {

    private final TheBridge theBridge;

    public WorldListener(final TheBridge theBridge) {
        this.theBridge = theBridge;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.PLAYER && event.getEntityType() != EntityType.DROPPED_ITEM)
            event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMapBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());
        Block block = event.getBlock();

        Game room = user.getGame();

        if (room == null || room.getStage() != GameStage.PLAYING || user.isInCage()) {
            event.setCancelled(true);
            return;
        }

        if (!user.isPlaying() && !Vanish.getInstance().isVanished(user.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (room.getConfiguration().getInvincibleBlocks().contains(block)) {
            event.setCancelled(true);
            return;
        }

        if (room.getRollback().contains(block))
            room.getRollback().remove(block);
        else {
            if (block.getType() == Material.STAINED_CLAY && block.getLocation().getY() <= room.getConfiguration().getSpawnPoint().getY()) {
                if (block.getState().getData().getData() == 14 || block.getState().getData().getData() == 11 || block.getState().getData().getData() == 0)
                    room.getBridgeBlocks().put(block, block.getData());
            } else {
                player.sendMessage(user.getAccount().getLanguage().translate("duels.not_placed_block"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());
        Block block = event.getBlock();

        Game room = user.getGame();

        if (room == null || room.getStage() != GameStage.PLAYING || user.isInCage()) {
            event.setCancelled(true);
            return;
        }

        if (room.getConfiguration().getInvincibleBlocks().contains(block)) {
            event.setCancelled(true);
            return;
        }

        if (block.getLocation().getY() >= room.getConfiguration().getMax_y()) {
            event.setCancelled(true);
            return;
        }

        if (!user.isPlaying() && !Vanish.getInstance().isVanished(user.getUniqueId())) {
            event.setCancelled(true);
        } else {
            room.getRollback().add(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        World world = event.getToBlock().getWorld();

        Game room = theBridge.getGameStorage().getGame(world);

        if (room == null || room.getStage() != GameStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        Set<Block> rollbackList = room.getRollback();

        rollbackList.add(event.getToBlock());
        rollbackList.add(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Game room = user.getGame();

        if (room == null || room.getStage() != GameStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (!user.isPlaying() && !Vanish.getInstance().isVanished(user.getUniqueId())) {
            event.setCancelled(true);
        } else {

            Block block = event.getBlockClicked().getRelative(event.getBlockFace());

            if (block.getType() != Material.AIR)
                return;

            room.getRollback().add(block);
        }
    }
}