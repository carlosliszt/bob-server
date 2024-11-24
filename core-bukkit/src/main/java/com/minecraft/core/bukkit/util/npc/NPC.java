/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.npc;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SerializableAs("NPC-Human")
@AllArgsConstructor
@Builder
public class NPC implements Cloneable {

    protected transient final String NPC_NAME_PREFIX = "§8[NPC] ";

    @Getter
    private String name;
    @Getter
    private Player viewer;
    @Getter
    private final Location location;

    @Getter
    @Setter
    private boolean hidden;

    private int humanEntityId, batEntityId;
    private GameProfile gameProfile;

    @Getter
    @Setter
    private Interact interactExecutor;
    @Setter
    private Property property;
    @Getter
    @Setter
    private long lastInteract;

    @Getter
    private final Equipment equipment;

    public NPC(Player viewer, Location location, Property property) {
        this.viewer = viewer;
        this.location = location;
        this.property = property;
        this.equipment = new Equipment();
    }

    public NPC(Player viewer, Location location, Property property, Interact interact) {
        this.viewer = viewer;
        this.location = location;
        this.property = property;
        this.interactExecutor = interact;
        this.equipment = new Equipment();
    }

    public void spawn(boolean register) {
        setup();
        sendPackets(getSpawnPackets());
        if (hasEquipment())
            getEquipment().send(this);
        setHidden(false);

        if (register)
            BukkitGame.getEngine().getNPCProvider().register(this);

        Bukkit.getScheduler().runTaskLater(BukkitGame.getEngine(), () -> sendPacket(getHidePacket()), 30L);

    }

    public void destroy(boolean unregister) {
        sendPackets(getDestroyPackets());
        setHidden(true);

        if (unregister)
            BukkitGame.getEngine().getNPCProvider().unregister(this);
    }

    public void setup() {
        this.name = NPC_NAME_PREFIX + Constants.KEY(16 - NPC_NAME_PREFIX.length(), false);
        this.gameProfile = (new GameProfile(UUID.randomUUID(), this.name));
        this.humanEntityId = get();
        this.batEntityId = get();

        if (this.property != null)
            this.gameProfile.getProperties().put("textures", this.property);
    }

    public void setSneaking(boolean bool) {
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(0, bool ? (byte) 0x02 : (byte) 0);

        sendPacket(new PacketPlayOutEntityMetadata(this.humanEntityId, dataWatcher, true));
    }

    private List<Packet> getSpawnPackets() {

        PacketPlayOutPlayerInfo humanInfoPacket = new PacketPlayOutPlayerInfo();
        setField(humanInfoPacket, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);

        List<PacketPlayOutPlayerInfo.PlayerInfoData> dataList = getField(humanInfoPacket.getClass(), humanInfoPacket, "b");
        IChatBaseComponent nameComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + this.gameProfile.getName() + "\"}");
        dataList.add(humanInfoPacket.new PlayerInfoData(this.gameProfile, 1, WorldSettings.EnumGamemode.NOT_SET, nameComponent));

        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(10, (byte) 127);
        dataWatcher.a(6, (float) 20);

        Location loc = getLocation();

        PacketPlayOutNamedEntitySpawn humanSpawnPacket = new PacketPlayOutNamedEntitySpawn();
        setField(humanSpawnPacket, "a", this.humanEntityId); // Entity ID
        setField(humanSpawnPacket, "b", this.gameProfile.getId()); // Profile Unique ID
        setField(humanSpawnPacket, "c", floor(loc.getX() * 32));// X Position
        setField(humanSpawnPacket, "d", floor(loc.getY() * 32));// Y Position
        setField(humanSpawnPacket, "e", floor(loc.getZ() * 32));// Z Position
        setField(humanSpawnPacket, "f", getAngle(loc.getYaw()));//yaw
        setField(humanSpawnPacket, "g", getAngle(loc.getPitch()));//pitch
        setField(humanSpawnPacket, "h", 0); // item hand
        setField(humanSpawnPacket, "i", dataWatcher);

        dataWatcher = new DataWatcher(null);
        dataWatcher.a(0, (byte) (1 << 5));

        PacketPlayOutEntityHeadRotation headRotationPacket = new PacketPlayOutEntityHeadRotation();
        setField(headRotationPacket, "a", this.humanEntityId);
        setField(headRotationPacket, "b", getAngle(getLocation().getYaw()));

        PacketPlayOutSpawnEntityLiving batSpawnPacket = new PacketPlayOutSpawnEntityLiving();
        setField(batSpawnPacket, "a", this.batEntityId);
        setField(batSpawnPacket, "b", 65);
        setField(batSpawnPacket, "c", floor(loc.getX() * 32D));
        setField(batSpawnPacket, "d", floor(loc.getY() * 32D));
        setField(batSpawnPacket, "e", floor(loc.getZ() * 32D));
        setField(batSpawnPacket, "l", dataWatcher);

        PacketPlayOutAttachEntity attachEntityPacket = new PacketPlayOutAttachEntity();
        setField(attachEntityPacket, "a", 0);
        setField(attachEntityPacket, "b", this.batEntityId);
        setField(attachEntityPacket, "c", this.humanEntityId);

        return Arrays.asList(humanInfoPacket, humanSpawnPacket, headRotationPacket, batSpawnPacket, attachEntityPacket);
    }

    private List<Packet> getDestroyPackets() {

        PacketPlayOutPlayerInfo humanInfoPacket = new PacketPlayOutPlayerInfo();
        setField(humanInfoPacket, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);

        List<PacketPlayOutPlayerInfo.PlayerInfoData> dataList = getField(humanInfoPacket.getClass(), humanInfoPacket, "b");
        IChatBaseComponent nameComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + this.gameProfile.getName() + "\"}");
        dataList.add(humanInfoPacket.new PlayerInfoData(this.gameProfile, 1, WorldSettings.EnumGamemode.NOT_SET, nameComponent));

        PacketPlayOutEntityDestroy humanDestroyPacket = new PacketPlayOutEntityDestroy(this.humanEntityId);

        return Arrays.asList(humanInfoPacket, humanDestroyPacket);
    }

    private Packet getHidePacket() {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        setField(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);

        List<PacketPlayOutPlayerInfo.PlayerInfoData> dataList = getField(packet.getClass(), packet, "b");
        IChatBaseComponent nameComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + this.gameProfile.getName() + "\"}");
        dataList.add(packet.new PlayerInfoData(this.gameProfile, 1, WorldSettings.EnumGamemode.NOT_SET, nameComponent));
        return packet;
    }


    public NPC clone(Player player) {
        try {
            NPC npc = (NPC) super.clone();
            npc.viewer = player;
            return npc;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Setter
    @Builder
    @AllArgsConstructor
    public static class Equipment {

        protected final ItemStack DEFAULT_ITEM = new ItemStack(Material.AIR);

        private ItemStack hand, helmet, chestplate, leggings, boots;

        public Equipment() {
            this.hand = DEFAULT_ITEM;
            this.helmet = DEFAULT_ITEM;
            this.chestplate = DEFAULT_ITEM;
            this.leggings = DEFAULT_ITEM;
            this.boots = DEFAULT_ITEM;
        }

        public void send(NPC npc) {

            if (npc.getViewer() == null || npc.isHidden())
                return;

            int entityId = npc.humanEntityId;

            PacketPlayOutEntityEquipment hand = new PacketPlayOutEntityEquipment(entityId, 0, CraftItemStack.asNMSCopy(this.hand));
            PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entityId, 1, CraftItemStack.asNMSCopy(this.helmet));
            PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(entityId, 2, CraftItemStack.asNMSCopy(this.chestplate));
            PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(entityId, 3, CraftItemStack.asNMSCopy(this.leggings));
            PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entityId, 4, CraftItemStack.asNMSCopy(this.boots));

            npc.sendPackets(Arrays.asList(hand, helmet, chestplate, leggings, boots));
        }
    }

    public interface Interact {

        void handle(Player player, NPC npc, ClickType type);

        enum ClickType {
            RIGHT, LEFT;
        }
    }

    private boolean sendPackets(List<Packet> packets) {
        PlayerConnection conn = ((CraftPlayer) getViewer()).getHandle().playerConnection;
        if (conn == null || conn.isDisconnected())
            return false;
        for (Packet packet : packets)
            conn.sendPacket(packet);
        return true;
    }

    private boolean sendPacket(Packet packet) {
        PlayerConnection conn = ((CraftPlayer) getViewer()).getHandle().playerConnection;
        if (conn == null || conn.isDisconnected())
            return false;
        conn.sendPacket(packet);
        return true;
    }

    public boolean hasEquipment() {
        return this.equipment != null;
    }

    public int getId() {
        return this.humanEntityId;
    }

    private int floor(double var0) {
        int var2 = (int) var0;
        return var0 < (double) var2 ? var2 - 1 : var2;
    }

    protected byte getAngle(float value) {
        return (byte) ((int) value * 256.0F / 360.0F);
    }

    private synchronized int get() {
        try {
            Class<?> clazz = Entity.class;
            Field field = clazz.getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            field.set(null, id + 1);
            return id;
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean inRangeOf(Player player) {
        if (player == null)
            return false;
        if (!player.getWorld().getUID().equals(location.getWorld().getUID())) {
            return false;
        }
        double distanceSquared = player.getLocation().distanceSquared(location);
        double bukkitRange = player.spigot().getViewDistance() << 4;
        return distanceSquared <= square(70) && distanceSquared <= square(bukkitRange);
    }

    public boolean inViewOf(Player player) {
        Vector dir = location.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getEyeLocation().getDirection()) >= 0.5000000000000001;
    }

    public void setField(Object instance, String fieldName, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getField(Class<?> clazz, Object instance, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double square(double val) {
        return val * val;
    }
}