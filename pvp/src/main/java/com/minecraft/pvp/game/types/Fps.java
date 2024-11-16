/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.game.types;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.pvp.event.PlayerProtectionRemoveEvent;
import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.game.GameType;
import com.minecraft.pvp.user.User;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

public class Fps extends Game {

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(Material.MUSHROOM_SOUP, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.BOWL, Material.INK_SACK, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);

    public Fps() {
        setType(GameType.FPS);
        setWorld(Bukkit.getWorld("fps"));

        setSpawn(new Location(getWorld(), 0.5, 70, 0.5, 0, 0));
        setLobby(new Location(getWorld(), 0.5, 70, 0.5, 0, 0));

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(150);

        setLimit(40);

        addColumn(Columns.PVP_FPS_KILLS, Columns.PVP_FPS_DEATHS, Columns.PVP_FPS_KILLSTREAK, Columns.PVP_FPS_MAX_KILLSTREAK, Columns.PVP_COINS);
        setValidDamages(Sets.immutableEnumSet(CONTACT, ENTITY_ATTACK, PROJECTILE, SUFFOCATION, FIRE, FIRE_TICK, MELTING, LAVA, DROWNING, BLOCK_EXPLOSION, ENTITY_EXPLOSION, VOID, LIGHTNING, SUICIDE, STARVATION, POISON, MAGIC, WITHER, FALLING_BLOCK, THORNS, CUSTOM));
    }

    @Override
    public void join(User user, boolean teleport) {
        super.join(user, teleport);

        Player player = user.getPlayer();

        player.updateInventory();
        user.handleSidebar();
    }

    @Override
    public void rejoin(User user, Rejoin rejoin) {
        super.rejoin(user, rejoin);
    }

    @Override
    public void quit(User user) {
        super.quit(user);
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        Account account = user.getAccount();
        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lPVP: FPS");
        scores.add(" ");
        scores.add("§fKills: §7" + account.getData(Columns.PVP_FPS_KILLS).getAsInteger());
        scores.add("§fDeaths: §7" + account.getData(Columns.PVP_FPS_DEATHS).getAsInteger());
        scores.add(" ");
        scores.add("§fKillstreak: §a" + account.getData(Columns.PVP_FPS_KILLSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE);

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void onLogin(User user) {

    }

    public void handleItems(User user) {
        Player player = user.getPlayer();

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.setItemOnCursor(null);
        player.getOpenInventory().getTopInventory().clear();

        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        player.getInventory().setItem(0, new ItemFactory().setType(Material.DIAMOND_SWORD).setName("§bSword").setUnbreakable().addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack());

        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));

        for (int i = 0; i < 36; i++)
            player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));

        player.updateInventory();
    }

    @EventHandler
    public void onUserDied(UserDiedEvent event) {
        if (!event.getGame().getUniqueId().equals(getUniqueId()))
            return;

        User killed = event.getKilled();
        Account killed_account = killed.getAccount();

        if (event.hasKiller()) {
            User killer = event.getKiller();
            Account killer_account = killer.getAccount();

            killed.getPlayer().sendMessage(killed_account.getLanguage().translate("pvp.arena.death_to_player", killer_account.getDisplayName()));

            killer.getPlayer().playSound(killer.getPlayer().getLocation(), Sound.ORB_PICKUP, 4F, 4F);
            killer.getPlayer().sendMessage(killer_account.getLanguage().translate("pvp.arena.kill", killed_account.getDisplayName()));

            repairArmor(killer_account.getRank(), killer.getPlayer());

            killer.restoreCombat();
            killer.giveCoins(10);

            killer_account.addInt(1, Columns.PVP_FPS_KILLS);
            killer_account.addInt(1, Columns.PVP_FPS_KILLSTREAK);

            killed_account.addInt(1, Columns.PVP_FPS_DEATHS);
            killed_account.getData(Columns.PVP_FPS_KILLSTREAK).setData(0);

            if (killer_account.getData(Columns.PVP_FPS_KILLSTREAK).getAsInt() > killer_account.getData(Columns.PVP_FPS_MAX_KILLSTREAK).getAsInt())
                killer_account.getData(Columns.PVP_FPS_MAX_KILLSTREAK).setData(killer_account.getData(Columns.PVP_FPS_KILLSTREAK).getAsInt());

            killer.handleSidebar();
        } else {
            killed.getPlayer().sendMessage(killed_account.getLanguage().translate("pvp.arena.death_to_anyone"));
            killed_account.addInt(1, Columns.PVP_FPS_DEATHS);
            killed_account.getData(Columns.PVP_FPS_KILLSTREAK).setData(0);
        }

        if (event.getReason() == UserDiedEvent.Reason.KILL)
            killed.getGame().join(killed, true);

        async(() -> {
            killed_account.getDataStorage().saveColumnsFromSameTable(Columns.PVP_FPS_DEATHS, Columns.PVP_FPS_KILLSTREAK);
            if (event.hasKiller())
                event.getKiller().getAccount().getDataStorage().saveColumnsFromSameTable(Columns.PVP_FPS_KILLS, Columns.PVP_FPS_KILLSTREAK, Columns.PVP_FPS_MAX_KILLSTREAK, Columns.PVP_COINS);
        });
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        User user = User.fetch(event.getEntity().getUniqueId());
        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;
        if (user.isKept())
            event.setCancelled(true);
        else
            event.setCancelled(!getValidDamages().contains(event.getCause()));
    }

    @EventHandler
    public void onLostProtection(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());
        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;
        if (!user.isKept())
            return;
        if (player.getLocation().getY() > 66)
            return;
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;
        user.setKept(false);
    }

    @EventHandler
    public void onLostProtection(PlayerProtectionRemoveEvent event) {
        Player player = event.getPlayer();

        if (player == null)
            return;

        User user = User.fetch(player.getUniqueId());

        if (player.getWorld().getUID().equals(Bukkit.getWorlds().get(0).getUID()) || !user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        handleItems(user);
        handleSidebar(user);

        player.sendMessage(user.getAccount().getLanguage().translate("pvp.lost_protection"));
    }

    @EventHandler
    public void onReceiveItems(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (player == null)
            return;

        User user = User.fetch(player.getUniqueId());

        if (player.getWorld().getUID().equals(Bukkit.getWorlds().get(0).getUID()) || !user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (isBlockedMaterial(event.getItemDrop().getItemStack().getType()))
            event.setCancelled(true);
        else
            event.getItemDrop().remove();
    }

    private void repairArmor(Rank rank, Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        if (helmet == null || helmet.getType() == Material.AIR) {
            inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
        } else {
            helmet.setDurability((byte) 0);
        }

        if (chestplate == null || chestplate.getType() == Material.AIR) {
            inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        } else {
            chestplate.setDurability((byte) 0);
        }

        if (leggings == null || leggings.getType() == Material.AIR) {
            inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        } else {
            leggings.setDurability((byte) 0);
        }

        if (boots == null || boots.getType() == Material.AIR) {
            inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
        } else {
            boots.setDurability((byte) 0);
        }
    }

    public boolean isBlockedMaterial(final Material material) {
        return !ACCEPTABLE_MATERIALS.contains(material);
    }

}