/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.violation;

import com.minecraft.core.bukkit.util.BukkitInterface;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AntiCrash implements BukkitInterface {

    private final Player player;
    @Setter
    private long lastOpen;
    private long lastTabComplete;

    private static final Map<UUID, Long> lastTeleportTime = new HashMap<>();
    private static final long TELEPORT_COOLDOWN = 2000; // 2 seconds


    public AntiCrash(Player player) {
        this.player = player;
    }

    public ViolationFeedback isSuspicious(final Object packet0) {
        final Player player = this.player;
        final String packetName = packet0.getClass().getSimpleName();
        if (packet0 instanceof PacketPlayInWindowClick) {
            final PacketPlayInWindowClick packet = (PacketPlayInWindowClick) packet0;
            try {
                final int slot = packet.b();
                if (slot > 127 || slot < -999) {
                    return new ViolationFeedback(packetName, "Invalid slot " + slot, false, true);
                }
            } catch (Exception ex2) {
                return new ViolationFeedback(packetName, "exception " + ex2.getMessage(), false, false);
            }
        } else if (packet0 instanceof PacketPlayInSetCreativeSlot) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack packetItem2 = null;
                try {
                    final Field itemField2 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                    itemField2.setAccessible(true);
                    packetItem2 = (ItemStack) itemField2.get(packet0);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
                if (packetItem2 != null) {
                    if (packetItem2.getName().toLowerCase().contains("book") && packetItem2.getTag() != null) {
                        packetItem2.getTag().remove("pages");
                        packetItem2.getTag().remove("author");
                        packetItem2.getTag().remove("title");
                    }
                    packetItem2.setTag(new NBTTagCompound());
                }
                return new ViolationFeedback(packetName, "clicking in creative inventory without gamemode 1", false, true);
            }
            ItemStack packetItem2 = null;
            try {
                final Field itemField2 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                itemField2.setAccessible(true);
                packetItem2 = (ItemStack) itemField2.get(packet0);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            if (packetItem2 != null) {
                final ViolationFeedback nbtTags2 = this.checkNbtTags(packetName, packetItem2);
                if (nbtTags2 != null) {
                    return nbtTags2;
                }
                final org.bukkit.inventory.ItemStack bukkitItem2 = CraftItemStack.asBukkitCopy(packetItem2);
                if ((bukkitItem2.getType() == Material.CHEST || bukkitItem2.getType() == Material.HOPPER) && bukkitItem2.hasItemMeta() && bukkitItem2.getItemMeta().toString().getBytes().length > 262144) {
                    return new ViolationFeedback(packetName, "too big chest data", false, false);
                }
            }
        } else if (packet0 instanceof PacketPlayInTabComplete) {
            if (this.lastTabComplete > System.currentTimeMillis()) {
                return new ViolationFeedback(packetName, "Too fast tab complete", true, false);
            }
            this.lastTabComplete = System.currentTimeMillis() + 120;
        } else if (packet0 instanceof PacketPlayInCustomPayload) {
            final PacketPlayInCustomPayload payload = (PacketPlayInCustomPayload) packet0;
            final String channel = payload.a();
            final ByteBuf data = payload.b();

            if (data.capacity() > 4800) {
                return new ViolationFeedback(packetName, "invalid bytebuf capacity", false, true);
            }
            if (channel.equalsIgnoreCase("MC|BEdit") || channel.equalsIgnoreCase("MC|BSign") || channel.equalsIgnoreCase("minecraft:bedit") || channel.equalsIgnoreCase("minecraft:bsign")) {
                try {
                    final PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.wrappedBuffer(data));
                    final ItemStack item4 = serializer.i();
                    if (item4 != null) {
                        if (System.currentTimeMillis() - this.lastOpen > 60000L) {
                            return new ViolationFeedback(packetName, "book sign, but no book used", false, false);
                        }
                        if (!player.getInventory().contains(Material.valueOf("BOOK_AND_QUILL")) && !player.getInventory().contains(Material.WRITTEN_BOOK)) {
                            return new ViolationFeedback(packetName, "book interact, but no book exists in player's inventory", false, true);
                        }
                        final ViolationFeedback nbtTags4 = this.checkNbtTags(packetName, item4);
                        if (nbtTags4 != null) {
                            return nbtTags4;
                        }
                    }
                } catch (Exception ex3) {
                    return new ViolationFeedback(packetName, "exception: " + ex3.getMessage(), false, false);
                }
            } else if (channel.equals("REGISTER") || channel.equalsIgnoreCase("UNREGISTER") || channel.toLowerCase().contains("fml")) {
                ByteBuf buffer = null;
                try {
                    buffer = data.copy();
                    if (buffer.toString(StandardCharsets.UTF_8).split("\u0000").length > 124) {
                        return new ViolationFeedback(packetName, "too many channels", false, false);
                    }
                } catch (Exception ignored) {
                } finally {
                    if (buffer != null) {
                        buffer.release();
                    }
                }
            } else if (channel.equals("MC|ItemName") && player.getInventory() != null && player.getOpenInventory().getType() != InventoryType.ANVIL) {
                return new ViolationFeedback(packetName, "trying to use MC|ItemName but no anvil exists", false, false);
            }
        } else if (packet0 instanceof PacketPlayInFlying.PacketPlayInPosition) {
            final PacketPlayInFlying.PacketPlayInPosition packet2 = (PacketPlayInFlying.PacketPlayInPosition) packet0;
            final double x = packet2.a();
            final double y = packet2.b();
            final double z = packet2.c();

            Location location = player.getLocation();

            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
                sync(() -> player.teleport(location));
                return new ViolationFeedback(packetName, "Client is trying to send NaN position packet", false, true);
            }

            if (!lastTeleportTime.containsKey(player.getUniqueId()) ||
                    System.currentTimeMillis() - lastTeleportTime.get(player.getUniqueId()) > TELEPORT_COOLDOWN) {

                if (location.distance(new Location(location.getWorld(), x, y, z)) > 50) {
                    sync(() -> {
                        player.teleport(location);
                        lastTeleportTime.put(player.getUniqueId(), System.currentTimeMillis());
                    });
                    return new ViolationFeedback(packetName, "Client is trying to move between far locations", true, false);
                }
            }

            if (x >= 2.147483647E9 || y > 2.147483647E9 || (z >= 2.147483647E9)) {
                sync(() -> player.teleport(location));
                return new ViolationFeedback(packetName, "Too high position", false, true);
            }

            if (x > 1000 || z > 1000) {
                sync(() -> player.teleport(location));
                return new ViolationFeedback(packetName, "Coordinates exceed", true, false);
            }

        } else if (packet0 instanceof PacketPlayInFlying.PacketPlayInPositionLook) {

            final PacketPlayInFlying.PacketPlayInPositionLook packet2 = (PacketPlayInFlying.PacketPlayInPositionLook) packet0;

            final double x = packet2.a();
            final double y = packet2.b();
            final double z = packet2.c();

            Location location = player.getLocation();

            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
                sync(() -> player.teleport(location));
                return new ViolationFeedback(packetName, "Client is trying to send NaN position packet", false, true);
            }

            if (x >= 2.147483647E9 || y > 2.147483647E9 || (z >= 2.147483647E9)) {
                sync(() -> player.teleport(location));
                return new ViolationFeedback(packetName, "Too high position", false, true);
            }

            if (!lastTeleportTime.containsKey(player.getUniqueId()) ||
                    System.currentTimeMillis() - lastTeleportTime.get(player.getUniqueId()) > TELEPORT_COOLDOWN) {

                if (location.distance(new Location(location.getWorld(), x, y, z)) > 50) {
                    sync(() -> {
                        player.teleport(location);
                        lastTeleportTime.put(player.getUniqueId(), System.currentTimeMillis()); // Update last teleport time
                    });
                    return new ViolationFeedback(packetName, "Client is trying to move between far locations", true, false);
                }
            }

            if (x > 1000 || z > 1000) {
                sync(() -> player.teleport(location));
                return new ViolationFeedback(packetName, "Coordinates exceed", true, false);
            }

        } else if (packet0 instanceof PacketPlayInFlying) {
            final PacketPlayInFlying packet3 = (PacketPlayInFlying) packet0;
            final double x = packet3.a();
            final double y = packet3.b();
            final double z = packet3.c();

            Location location = player.getLocation();

            if ((x >= Double.MAX_VALUE || y >= Double.MAX_VALUE || z >= Double.MAX_VALUE)) {
                return new ViolationFeedback(packetName, "Double.MAX_VALUE position", false, true);
            }

            if (x >= 2.147483647E9 || y > 2.147483647E9 || (z >= 2.147483647E9)) {
                return new ViolationFeedback(packetName, "Integer.MAX_VALUE position", false, true);
            }

            final float yaw = packet3.d();
            final float pitch = packet3.e();
            if (yaw == Float.NEGATIVE_INFINITY || pitch == Float.NEGATIVE_INFINITY || yaw >= Float.MAX_VALUE || pitch >= Float.MAX_VALUE) {
                return new ViolationFeedback(packetName, "invalid float position", false, true);
            }
        } else if (packet0 instanceof PacketPlayInSteerVehicle) {
            final PacketPlayInSteerVehicle p = (PacketPlayInSteerVehicle) packet0;
            if (p.b() >= Float.MAX_VALUE || p.a() >= Float.MAX_VALUE) {
                return new ViolationFeedback(packetName, "invalid vehicle movement", false, true);
            }
        }
        return null;
    }

    private ViolationFeedback checkNbtTags(final String packet, final ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        final NBTTagCompound tagCompound = itemStack.getTag();
        final Item item = itemStack.getItem();
        if (tagCompound == null) {
            return null;
        }
        if (item instanceof ItemFireworks && tagCompound.toString().length() > 300) {
            return new ViolationFeedback(packet, "too big firework data", false, true);
        }
        if (item instanceof ItemFireworksCharge && tagCompound.toString().length() > 800) {
            return new ViolationFeedback(packet, "too big firework_charge data", false, false);
        }
        final Set<String> keys = tagCompound.c();
        if (keys.size() > 20) {
            return new ViolationFeedback(packet, "too many keys (" + keys.size() + ")", false, true);
        }
        if (tagCompound.hasKey("pages")) {
            final NBTTagList pages = tagCompound.getList("pages", 8);
            if (pages.size() > 50) {
                tagCompound.remove("pages");
                tagCompound.remove("author");
                tagCompound.remove("title");
                itemStack.setTag(new NBTTagCompound());
                return new ViolationFeedback(packet, "too many pages (" + pages.size() + ")", false, true);
            }
            String lastPage = "";
            int similarPages = 0;
            for (int i = 0; i < pages.size(); ++i) {
                final String page = pages.getString(i);
                if (page.contains("wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5") || page.equalsIgnoreCase("wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5")) {
                    itemStack.setTag(new NBTTagCompound());
                    return new ViolationFeedback(packet, "crash client detected (invalid book page data)", false, true);
                }
                if (page.length() > 900) {
                    tagCompound.remove("pages");
                    tagCompound.remove("author");
                    tagCompound.remove("title");
                    itemStack.setTag(new NBTTagCompound());
                    return new ViolationFeedback(packet, "too large page content (" + page.length() + ")", false, false);
                }
                if (page.split("extra").length > 8) {
                    return new ViolationFeedback(packet, "too many extra words", false, true);
                }
                if (lastPage.equals(page)) {
                    ++similarPages;
                }
                lastPage = page;
                if (similarPages > 4) {
                    tagCompound.remove("pages");
                    tagCompound.remove("author");
                    tagCompound.remove("title");
                    itemStack.setTag(new NBTTagCompound());
                    return new ViolationFeedback(packet, "too many similar pages", false, true);
                }
                final String strippedPage = ChatColor.stripColor(page.replaceAll("\\+", ""));
                if (strippedPage == null || strippedPage.equals("null")) {
                    return new ViolationFeedback(packet, "null stripped page", false, true);
                }
                if (strippedPage.length() > 256) {
                    tagCompound.remove("pages");
                    tagCompound.remove("author");
                    tagCompound.remove("title");
                    itemStack.setTag(new NBTTagCompound());
                    return new ViolationFeedback(packet, "too large stripped page content (" + page.length() + ")", false, true);
                }
                int tooBigChars = 0;
                for (int charI = 0; charI < page.length(); ++charI) {
                    final char current = page.charAt(charI);
                    if (String.valueOf(current).getBytes().length > 1 && ++tooBigChars > 15) {
                        return new ViolationFeedback(packet, "", false, true);
                    }
                }
                final String noSpaces = page.replace(" ", "");
                if (noSpaces.startsWith("{\"translate\"")) {
                    for (final String crashTranslation : MojangCrashTranslations.MOJANG_CRASH_TRANSLATIONS) {
                        final String translationJson = String.format("{\"translate\":\"%s\"}", crashTranslation);
                        if (page.equalsIgnoreCase(translationJson)) {
                            itemStack.setTag(new NBTTagCompound());
                            return new ViolationFeedback(packet, "Crash book! TranslationJson: " + translationJson, false, true);
                        }
                    }
                }
            }
        }
        final String name = item.getName().toLowerCase();
        if (!name.contains("chest") && !name.contains("hopper") && !name.contains("shulker")) {
            final int length = String.valueOf(tagCompound).getBytes(StandardCharsets.UTF_8).length;
            if (length > 10000) {
                return new ViolationFeedback(packet, "Too big NBT data! (" + length + ")", false, true);
            }
        }
        int listsAmount = 0;
        for (final String key : keys) {
            if (tagCompound.hasKeyOfType(key, 9)) {
                if (++listsAmount > 10) {
                    return new ViolationFeedback(packet, "too many NBTLists (" + listsAmount + ")", false, true);
                }
                final NBTTagList list = tagCompound.getList(key, 8);
                final int size = list.size();
                if (size > 50) {
                    tagCompound.remove(key);
                    return new ViolationFeedback(packet, "too big NBTList (" + size + ")", false, true);
                }
                for (int j = 0; j < list.size(); ++j) {
                    final String content = list.getString(j);
                    if (content == null || content.equals("null")) {
                        return new ViolationFeedback(packet, "null list content", false, true);
                    }
                    if (content.length() > 900) {
                        return new ViolationFeedback(packet, "too big list content (" + content.length() + ")", false, false);
                    }
                }
            }
            if (tagCompound.hasKeyOfType(key, 11)) {
                final int[] intArray = tagCompound.getIntArray(key);
                if (intArray.length > 50) {
                    return new ViolationFeedback(packet, "too large int array", false, true);
                }
            }
        }
        return null;
    }
}
