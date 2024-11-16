package com.minecraft.core.bukkit.util.actionbar;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBar {

    public static void sendActionBar(Player player, String message) {
        IChatBaseComponent chatComponent = new ChatComponentText(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(chatComponent, (byte) 2);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
