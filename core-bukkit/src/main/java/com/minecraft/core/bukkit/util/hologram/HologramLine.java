/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.hologram;

import com.minecraft.core.bukkit.util.hologram.helper.CustomBoundingBox;
import com.minecraft.core.bukkit.util.reflection.FieldHelper;
import com.viaversion.viaversion.ViaVersionPlugin;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@Getter
@Setter
public class HologramLine {

    private EntitySlime slime;
    private EntityArmorStand armorStand;
    private Location location;
    private String text;
    private Player viewer;

    public HologramLine(Player viewer, Location textLocation, String text) {
        this.viewer = viewer;
        this.location = textLocation;
        this.text = text;
    }

    public void build() {
        this.armorStand = new EntityArmorStand(((CraftWorld) this.location.getWorld()).getHandle(), this.location.getX(), this.location.getY(), this.location.getZ());
        armorStand.setCustomName(getText());
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setSmall(true);
        armorStand.setArms(false);
        armorStand.n(true);
        armorStand.a(new CustomBoundingBox());

        int size = getText() == null ? 1 : getText().length() / 2 / 3;
        Location slimeLocation = this.location.clone().add(0.0D, 0.10, 0.0D);
        this.slime = new EntitySlime(((CraftWorld) this.location.getWorld()).getHandle());
        this.slime.setLocation(slimeLocation.getX(), slimeLocation.getY(), slimeLocation.getZ(), slimeLocation.getYaw(), slimeLocation.getPitch());
        slime.getDataWatcher().watch(0, (byte) 32);
        slime.getDataWatcher().watch(16, (byte) (size < 1 ? 1 : Math.min(size, 100)));
    }

    public void show() {
        PlayerConnection conn = ((CraftPlayer) getViewer()).getHandle().playerConnection;
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
        conn.sendPacket(packet);

        PacketPlayOutSpawnEntityLiving packet2 = new PacketPlayOutSpawnEntityLiving(slime);
        conn.sendPacket(packet2);

        if (ViaVersionPlugin.getInstance().getApi().getPlayerVersion(viewer) >= 47) {
            PacketPlayOutAttachEntity packet3 = new PacketPlayOutAttachEntity();
            FieldHelper.setValue(packet3, "a", 0);
            FieldHelper.setValue(packet3, "b", slime.getId());
            FieldHelper.setValue(packet3, "c", armorStand.getId());
            conn.sendPacket(packet3);
        }
    }

    public void hide() {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(getSlime().getId(), getArmorStand().getId());
        ((CraftPlayer) getViewer()).getHandle().playerConnection.sendPacket(packet);
    }

    public void update(String t) {

        if (t.equals(text))
            return;

        this.text = t;
        EntityArmorStand entityArmorStand = getArmorStand();
        entityArmorStand.setCustomName(t);
        final PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
        ((CraftPlayer) getViewer()).getHandle().playerConnection.sendPacket(metadata);

        int size = getText() == null ? 1 : getText().length() / 2 / 3;
        slime.getDataWatcher().watch(16, (byte) (size < 1 ? 1 : Math.min(size, 100)));

        final PacketPlayOutEntityMetadata mt = new PacketPlayOutEntityMetadata(getSlime().getId(), getSlime().getDataWatcher(), true);
        ((CraftPlayer) getViewer()).getHandle().playerConnection.sendPacket(mt);

    }

}