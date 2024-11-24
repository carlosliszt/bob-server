/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.disguise;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.reflection.FieldHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class PlayerDisguise {

    public static void changeSkin(Player player, Property property, boolean respawn) {
        if (property == null)
            return;

        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        GameProfile gameProfile = nmsPlayer.getProfile();
        gameProfile.getProperties().clear();

        gameProfile.getProperties().put("textures", property);
        if (respawn)
            respawn(nmsPlayer);
    }

    public static void changeSkin(Player player, Property property) {
        changeSkin(player, property, true);
    }

    public static void disguise(Player player, String nickname, Property property, boolean respawn) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        GameProfile gameProfile = nmsPlayer.getProfile();

        if (property != null) {
            gameProfile.getProperties().clear();
            gameProfile.getProperties().put("textures", property);
        }

        PlayerList list = ((CraftServer) Bukkit.getServer()).getHandle();
        Map<String, EntityPlayer> playersByName = FieldHelper.getValue(PlayerList.class, list, "playersByName");

        String oldNickname = player.getName();
        playersByName.remove(oldNickname);

        try {
            Field field = GameProfile.class.getDeclaredField("name");
            FieldHelper.makeNonFinal(field);
            field.set(gameProfile, nickname);
        } catch (Exception e) {
            e.printStackTrace();
        }

        playersByName.put(player.getName(), nmsPlayer);

        if (respawn)
            respawn(nmsPlayer);
    }

    public static void changeNickname(Player player, String nickname) {
        changeNickname(player, nickname, true);
    }

    public static void changeNickname(Player player, String nickname, boolean respawn) {
        try {
            EntityPlayer ep = ((CraftPlayer) player).getHandle();

            PlayerList list = ((CraftServer) Bukkit.getServer()).getHandle();
            Map<String, EntityPlayer> playersByName = FieldHelper.getValue(PlayerList.class, list, "playersByName");

            String oldNickname = player.getName();
            playersByName.remove(oldNickname);

            GameProfile gameProfile = ep.getProfile();

            Field field = GameProfile.class.getDeclaredField("name");
            FieldHelper.makeNonFinal(field);
            field.set(gameProfile, nickname);

            playersByName.put(player.getName(), ep);
            if (respawn)
                respawn(ep);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void respawn(EntityPlayer ep) {

        double x = ep.locX;
        double y = ep.locY;
        double z = ep.locZ;

        WorldServer worldserver = (WorldServer) ep.getWorld();
        DedicatedPlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();

        PacketPlayOutEntityDestroy destroyEntity = new PacketPlayOutEntityDestroy(ep.getId());
        PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ep);
        PacketPlayOutNamedEntitySpawn spawnEntity = new PacketPlayOutNamedEntitySpawn(ep);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(ep.getId(), ep.getDataWatcher(), true);
        PacketPlayOutHeldItemSlot heldItemSlot = new PacketPlayOutHeldItemSlot(ep.inventory.itemInHandIndex);
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(ep, (byte) 28);
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(worldserver.worldProvider.getDimension(), worldserver.getDifficulty(), worldserver.getWorldData().getType(), ep.playerInteractManager.getGameMode());
        PacketPlayOutPosition position = new PacketPlayOutPosition(ep.locX, ep.locY, ep.locZ, ep.yaw, ep.pitch, Collections.emptySet());
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(ep, (byte) MathHelper.d(ep.getHeadRotation() * 256.0F / 360.0F));

        Bukkit.getScheduler().runTask(BukkitGame.getEngine(), () -> {
            for (int i = 0; i < playerList.players.size(); i++) {
                EntityPlayer ep1 = playerList.players.get(i);

                if (ep1.getBukkitEntity().canSee(ep.getBukkitEntity())) {
                    PlayerConnection playerConnection = ep1.playerConnection;
                    playerConnection.sendPacket(removePlayer);
                    playerConnection.sendPacket(addPlayer);

                    if (ep1.getId() != ep.getId()) {
                        playerConnection.sendPacket(destroyEntity);
                        playerConnection.sendPacket(spawnEntity);
                    }

                    playerConnection.sendPacket(headRotation);
                }
            }

            PlayerConnection con = ep.playerConnection;
            con.sendPacket(metadata);
            con.sendPacket(respawn);
            con.sendPacket(position);
            con.sendPacket(heldItemSlot);
            con.sendPacket(status);
            ep.updateAbilities();
            ep.triggerHealthUpdate();
            ep.updateInventory(ep.activeContainer);
            ep.updateInventory(ep.defaultContainer);
        });

        CraftPlayer player = ep.getBukkitEntity();

        player.getInventory().setArmorContents(player.getInventory().getArmorContents());
        player.setExp(player.getExp());
        player.setHealth(player.getHealth());
        player.setSneaking(player.isSneaking());
        if (player.getPassenger() != null)
            player.setPassenger(player.getPassenger());
        if (player.isInsideVehicle())
            player.getVehicle().setPassenger(player);
        ep.locX = x;
        ep.locY = y;
        ep.locZ = z;
        ep.lastX = x;
        ep.lastY = y;
        ep.lastZ = z;
        ep.setPosition(x, y, z);
    }

}