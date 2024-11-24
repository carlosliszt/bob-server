/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.constructor;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.game.GameStage;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.imanity.imanityspigot.chunk.AsyncPriority;

import java.util.*;

public interface Assistance {

    default boolean hasStarted() {
        return getStage() != GameStage.WAITING;
    }

    default GameStage getStage() {
        return HungerGames.getInstance().getGame().getStage();
    }

    default int getTime() {
        return HungerGames.getInstance().getGame().getTime();
    }

    default World getWorld() {
        return HungerGames.getInstance().getGame().getWorld();
    }

    default HungerGames getPlugin() {
        return HungerGames.getInstance();
    }

    default User getUser(UUID uuid) {
        return User.fetch(uuid);
    }

    default ItemStack addTag(ItemStack stack, String... tag) {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound nbtTagCompound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();
        for (String str : tag)
            nbtTagCompound.set(str, new NBTTagByte((byte) 0));
        nmsCopy.setTag(nbtTagCompound);
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }

    default boolean hasKey(ItemStack itemStack, String key) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey(key);
    }

    default boolean isPlayer(Entity entity) {
        return entity instanceof Player;
    }

    default void refreshVisibility(Player player) {
        User user = getUser(player.getUniqueId());

        for (Player other : Bukkit.getOnlinePlayers()) {

            if (player.getEntityId() == other.getEntityId())
                continue;

            User userOther = getUser(other.getUniqueId());
            if (!userOther.isAlive() && !user.isAlive()) {
                if (!user.isSpecs()) {
                    player.hidePlayer(other);
                } else {
                    if (userOther.isVanish()) {
                        if (user.getAccount().getRank().getCategory().getImportance() >= Vanish.getInstance().getRank(other.getUniqueId()).getCategory().getImportance()) {
                            player.showPlayer(other);
                        }
                    } else {
                        player.showPlayer(other);
                    }
                }
            } else if (!userOther.isAlive() && user.isAlive()) {
                if (userOther.isVanish() && hasStarted()) {
                    if (Vanish.getInstance().getRank(other.getUniqueId()) == userOther.getAccount().getRank()) {
                        player.hidePlayer(other);
                    }
                } else {
                    player.hidePlayer(other);
                }
            }
        }
    }

    default Collection<User> getUsers(String splitter, String raw) {
        List<User> list = new ArrayList<>();

        if (raw.equalsIgnoreCase("all"))
            return new ArrayList<>(getPlugin().getUserStorage().getAliveUsers());

        String[] names = raw.split(splitter);

        for (String name : names) {
            User user = User.getUser(name);
            if (user == null || !user.isAlive())
                continue;
            list.add(user);
        }
        return list;
    }

    default Kit getKit(String s) {
        return getPlugin().getKitStorage().getKit(s);
    }

    default boolean isLateLimit() {
        return !hasStarted() || getStage() == GameStage.INVINCIBILITY || getTime() <= 300;
    }

    default Game getGame() {
        return HungerGames.getInstance().getGame();
    }

    default void giveItem(Player player, ItemStack itemStack, Location location) {
        if (player.getInventory().firstEmpty() != -1 || firstPartial(player.getInventory(), itemStack.getType()) != -1)
            player.getInventory().addItem(itemStack);
        else
            location.getWorld().dropItemNaturally(location.clone().add(0, 0.5, 0), itemStack);
    }

    default int firstPartial(Inventory inv, Material material) {
        ItemStack[] inventory = inv.getContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material && item.getAmount() < item.getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }

    default void teleport(Location location, int delay) {
        location.getWorld().imanity().getChunkAtAsynchronously(location, AsyncPriority.HIGHER).thenRun(() -> new BukkitRunnable() {
            final Iterator<? extends Player> playerIterator = Bukkit.getOnlinePlayers().stream().iterator();

            public void run() {
                for (int i = 0; i < 11; i++) {
                    if (playerIterator.hasNext()) {
                        Player player = playerIterator.next();

                        int range = HungerGames.getInstance().getGame().getVariables().getSpawnRange();

                        Location loc = location.clone().add(r(range), 0, r(range));
                        ((CraftPlayer) player).getHandle().playerConnection.teleport(loc);
                    } else {
                        cancel();
                        break;
                    }
                }
            }
        }.runTaskTimerAsynchronously(BukkitGame.getEngine(), 0, delay));
    }

    default int r(int x) {
        if (x <= 0)
            return 0;
        int y = Constants.RANDOM.nextInt(x);
        return Constants.RANDOM.nextBoolean() ? -y : y;
    }

    default Collection<Kit> getKits(char sppliter, String raw) {
        List<Kit> list = new ArrayList<>();

        if (raw.equalsIgnoreCase("all")) {
            list.addAll(getPlugin().getKitStorage().getKits());
            return list;
        }

        String[] names = raw.split(String.valueOf(sppliter));

        for (String name : names) {
            Kit kit = getKit(name);

            if (kit == null)
                continue;

            list.add(kit);
        }
        return list;
    }

}
