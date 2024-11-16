/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.protocol;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.event.player.PlayerCommandTabCompleteEvent;
import com.minecraft.core.bukkit.event.protocol.PacketReceiveEvent;
import com.minecraft.core.bukkit.event.protocol.PacketSendEvent;
import com.minecraft.core.bukkit.util.hologram.Hologram;
import com.minecraft.core.bukkit.util.hologram.HologramLine;
import com.minecraft.core.bukkit.util.hologram.HologramListener;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.bukkit.util.npc.NPCListener;
import com.minecraft.core.bukkit.util.violation.AntiCrash;
import com.minecraft.core.bukkit.util.violation.ViolationFeedback;
import com.viaversion.viaversion.ViaVersionPlugin;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PacketListener extends ChannelDuplexHandler {

    private final Player player;
    private static NPCListener npcListener;
    private static HologramListener hologramListener;
    private final AntiCrash antiCrash;

    public PacketListener(final Player player) {
        this.player = player;
        this.antiCrash = new AntiCrash(player);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {

        ViolationFeedback violationFeedback = antiCrash.isSuspicious(packet);

        if (violationFeedback != null) {
            if (!violationFeedback.isCancelOnly()) {
                context.close();
            }
            /*if (violationFeedback.isSurelyExploit())*/
            System.out.println("Violation: " + player.getName() + " for " + violationFeedback + " (PROTOCOL=" + ViaVersionPlugin.getInstance().getApi().getPlayerVersion(player) + ")");
            return;
        }

        if (packet instanceof PacketPlayInTabComplete) {
            String msg = ((PacketPlayInTabComplete) packet).a();
            if (!msg.contains(" ") && msg.contains("/")) {

                List<String> arraylist = new ArrayList<>();

                PlayerCommandTabCompleteEvent playerCommandTabCompleteEvent = new PlayerCommandTabCompleteEvent(Account.fetch(getPlayer().getUniqueId()), msg, true);
                Bukkit.getPluginManager().callEvent(playerCommandTabCompleteEvent);

                playerCommandTabCompleteEvent.getCompleterList().sort(String.CASE_INSENSITIVE_ORDER);

                if (!playerCommandTabCompleteEvent.isCancelled())
                    arraylist.addAll(playerCommandTabCompleteEvent.getCompleterList());

                PlayerConnection playerConnection = ((CraftPlayer) this.player).getHandle().playerConnection;
                playerConnection.sendPacket(new PacketPlayOutTabComplete(arraylist.toArray(new String[0])));
                playerCommandTabCompleteEvent.getCompleterList().clear();
                arraylist.clear();
                return;
            }
        } else if (packet instanceof PacketPlayInUseEntity) {

            int entityId = Integer.parseInt(getFieldValue(packet, "a").toString());
            String action = getFieldValue(packet, "action").toString();

            if (npcListener.isLoaded()) {

                for (NPC npc : BukkitGame.getEngine().getNPCProvider().getPlayerHumans(player)) {
                    if (npc.getId() == entityId && !npc.isHidden()) {

                        if (npc.getLastInteract() + 150 > System.currentTimeMillis())
                            break;

                        NPC.Interact onClickListener = npc.getInteractExecutor();

                        if (onClickListener != null) {
                            NPC.Interact.ClickType type = action.equals("ATTACK") ? NPC.Interact.ClickType.LEFT : NPC.Interact.ClickType.RIGHT;
                            npc.setLastInteract(System.currentTimeMillis());
                            onClickListener.handle(player, npc, type);
                        }
                        break;
                    }
                }
            }

            if (hologramListener.isLoaded()) {
                boolean found = false;

                List<Hologram> holograms = new ArrayList<>(BukkitGame.getEngine().getHologramProvider().getPlayerHolograms(player));
                holograms.sort(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(player.getLocation())));

                for (Hologram hologram : holograms) {
                    if (found)
                        break;
                    if (hologram.isHidden())
                        continue;
                    if (hologram.getInteract() == null)
                        continue;
                    if (!hologram.inViewOf(player))
                        continue;
                    for (int i = 0; i < hologram.getHologramLines().size(); i++) {
                        HologramLine e = hologram.getHologramLines().get(i);
                        if (e.getSlime().getId() == entityId) {
                            found = true;

                            if (hologram.getLastInteract() + 180 > System.currentTimeMillis())
                                break;

                            Hologram.Interact onClickListener = hologram.getInteract();

                            if (onClickListener != null && getPlayer().getLocation().distanceSquared(e.getLocation()) < 16) {
                                Hologram.Interact.ClickType type = action.equals("ATTACK") ? Hologram.Interact.ClickType.LEFT : Hologram.Interact.ClickType.RIGHT;
                                hologram.setLastInteract(System.currentTimeMillis());
                                onClickListener.handle(player, hologram, i, type);
                            }
                            break;
                        }
                    }
                }
                holograms.clear();
            }
        }

        PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(getPlayer(), (Packet) packet, false);
        packetReceiveEvent.fire();
        if (!packetReceiveEvent.isCancelled())
            super.channelRead(context, packet);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {

        PacketSendEvent packetSendEvent = new PacketSendEvent(getPlayer(), (Packet) packet, false);
        packetSendEvent.fire();
        if (!packetSendEvent.isCancelled())
            super.write(ctx, packetSendEvent.getPacket(), promise);
    }

    public Player getPlayer() {
        return player;
    }

    private Object getFieldValue(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
    }

    public static void setHologramListener(HologramListener hologramListener) {
        PacketListener.hologramListener = hologramListener;
    }

    public static void setNPCListener(NPCListener npcListener) {
        PacketListener.npcListener = npcListener;
    }
}