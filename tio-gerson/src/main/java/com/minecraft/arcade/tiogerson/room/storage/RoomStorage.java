package com.minecraft.arcade.tiogerson.room.storage;

import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class RoomStorage {

    private final Map<UUID, Room> rooms = new HashMap<>();
    @Getter
    private final ArcadeMain arcadeMain;

    public Room getRoom(World world) {
        return rooms.get(world.getUID());
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(arcadeMain, () -> rooms.values().forEach(c -> c.getMode().tick(c)), 20, 20);
    }

    public void end() {
        delete(new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata"));
        rooms.forEach((c, a) -> delete(a.getWorld().getWorldFolder()));
        rooms.clear();
    }

    public void register(Room room) {
        rooms.put(room.getWorld().getUID(), room);
    }

    public void delete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                delete(new File(file, child));
            }
        }
        if (file.exists())
            file.delete();
    }

    public Room get() {
        return getRooms().values().stream().filter(room -> {

            if (room.isLock())
                return false;

            if (room.getStage() == RoomStage.WAITING) {
                return !room.isFull();
            }

            return false;
        }).min((a, b) -> Integer.compare(b.getAlivePlayers().size(), a.getAlivePlayers().size())).orElse(null);
    }


    public List<Room> getBusy() {
        return getRooms().values().stream().filter(room -> room.getStage() != RoomStage.WAITING && room.getStage() != RoomStage.STARTING).collect(Collectors.toList());
    }

    public Room getRoomById(String id) {
        return getRooms().values().stream().filter(room -> room.getCode().equals(id)).findFirst().orElse(null);
    }

}
