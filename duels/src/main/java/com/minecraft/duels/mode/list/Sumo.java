package com.minecraft.duels.mode.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.duels.event.player.UserDeathEvent;
import com.minecraft.duels.mode.Mode;
import com.minecraft.duels.room.Room;
import com.minecraft.duels.user.User;
import com.minecraft.duels.util.enums.RoomStage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Sumo extends Mode {

    public Sumo() {
        super(10, DuelType.SUMO_1V1, DuelType.SUMO_2V2);
        setWins(Columns.DUELS_SUMO_WINS);
        setLoses(Columns.DUELS_SUMO_LOSSES);
        setWinstreak(Columns.DUELS_SUMO_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_SUMO_MAX_WINSTREAK);
        setGames(Columns.DUELS_SUMO_GAMES);
        setRating(Columns.DUELS_SUMO_RATING);
    }

    @Override
    public void start(Room room) {
        super.start(room);

        room.getAlivePlayers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255), true);
            player.updateInventory();
        });
    }

    private final ImmutableSet<Material> MATERIALS = Sets.immutableEnumSet(Material.WATER, Material.STATIONARY_WATER);

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null)
            return;

        if (room.getMode() != this)
            return;

        if (room.getStage() != RoomStage.PLAYING)
            return;

        if (!user.isPlaying())
            return;

        if (!MATERIALS.contains(player.getLocation().getBlock().getType()))
            return;

        new UserDeathEvent(user, false).fire();
    }

}
