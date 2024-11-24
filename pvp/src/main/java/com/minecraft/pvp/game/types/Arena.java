/*
 * Copyright (C) BlazeMC, All Rights Reserved
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
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.pvp.PvP;
import com.minecraft.pvp.event.PlayerProtectionRemoveEvent;
import com.minecraft.pvp.event.UserDiedEvent;
import com.minecraft.pvp.game.Game;
import com.minecraft.pvp.game.GameType;
import com.minecraft.pvp.game.structures.Feast;
import com.minecraft.pvp.game.structures.FinalBattle;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.user.User;
import com.minecraft.pvp.util.Items;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

public class Arena extends Game {

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(Material.BOWL, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.MUSHROOM_SOUP, Material.INK_SACK);

    public Arena() {
        setType(GameType.ARENA);
        setWorld(Bukkit.getWorld("arena"));

        setSpawn(new Location(getWorld(), 0.5, 130, 0.5, 0, 0));
        setLobby(new Location(getWorld(), 0.5, 130, 0.5, -90, 0));

        WorldBorder worldBorder = getWorld().getWorldBorder();
        worldBorder.setCenter(getSpawn());
        worldBorder.setSize(500);

        addColumn(Columns.PVP_ARENA_KILLS, Columns.PVP_ARENA_DEATHS, Columns.PVP_ARENA_KILLSTREAK, Columns.PVP_ARENA_MAX_KILLSTREAK, Columns.PVP_KITS, Columns.PVP_COINS, Columns.PVP_RANK, Columns.PVP_RANK_EXP);
        setValidDamages(Sets.immutableEnumSet(CONTACT, ENTITY_ATTACK, PROJECTILE, SUFFOCATION, FALL, FIRE, FIRE_TICK, MELTING, LAVA, DROWNING, BLOCK_EXPLOSION, ENTITY_EXPLOSION, VOID, LIGHTNING, SUICIDE, STARVATION, POISON, MAGIC, WITHER, FALLING_BLOCK, THORNS, CUSTOM));

        setLimit(80);

        new Feast(this).start(getPlugin());
        new FinalBattle(this).start(getPlugin());
    }

    @Override
    public void join(User user, boolean teleport) {
        super.join(user, teleport);

        Player player = user.getPlayer();

        Items.find(user.getAccount().getLanguage()).build(player);
        player.updateInventory();

        Bukkit.getScheduler().runTask(getPlugin(), () -> {
            player.setFireTicks(0);
            player.setVelocity(new Vector());
        });

        user.handleSidebar();
    }

    @Override
    public void rejoin(User user, Rejoin rejoin) {
        super.rejoin(user, rejoin);
    }

    @EventHandler
    public void onUserDied(UserDiedEvent event) {
        if (!event.getGame().getUniqueId().equals(getUniqueId()))
            return;

        RankingFactory rankingFactory = getPlugin().getRankingFactory();

        User killed = event.getKilled();
        Player killed_player = killed.getPlayer();
        Account killed_account = killed.getAccount();

        if (event.hasKiller()) {
            User killer = event.getKiller();
            Player killer_player = killer.getPlayer();
            Account killer_account = killer.getAccount();

            killed_player.sendMessage(killed_account.getLanguage().translate("pvp.arena.death_to_player", killer_account.getDisplayName()));

            killer_player.playSound(killer_player.getLocation(), Sound.ORB_PICKUP, 4F, 4F);
            killer_player.sendMessage(killer_account.getLanguage().translate("pvp.arena.kill", killed_account.getDisplayName()));

            List<ItemStack> drops = new ArrayList<>(event.getDrops());
            drops.removeIf(drop -> drop == null || !getValidDrops().contains(drop.getType()));

            event.getDrops().clear();

            if (event.inSameGame())
                drops.forEach(drop -> killer_player.getWorld().dropItemNaturally(event.getLocation(), drop));

            verifyLostKillstreak(killed, killer, killed_account.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInt());

            killer.restoreCombat();
            killer.giveCoins(10);

            killer_account.addInt(1, Columns.PVP_ARENA_KILLS);
            killer_account.addInt(1, Columns.PVP_ARENA_KILLSTREAK);

            killed_account.addInt(1, Columns.PVP_ARENA_DEATHS);
            killed_account.getData(Columns.PVP_ARENA_KILLSTREAK).setData(0);

            if (killer_account.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInt() > killer_account.getData(Columns.PVP_ARENA_MAX_KILLSTREAK).getAsInt())
                killer_account.getData(Columns.PVP_ARENA_MAX_KILLSTREAK).setData(killer_account.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInt());

            int experience = killer.isUsing(PvP.getPvP().getKitStorage().getKit("Stomper")) ? 2 : 8;

            killer_account.addInt(experience, rankingFactory.getTarget().getExperience());
            rankingFactory.verify(killer_account);

            killed_account.removeInt(6, rankingFactory.getTarget().getExperience());
            rankingFactory.verify(killed_account);

            killer_player.sendMessage("§b+" + experience + " XP");
            killed_player.sendMessage("§4-6 XP");

            verifyReceiveKillstreak(killer, killer_account.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInt());

            if (killer.isUsing(PvP.getPvP().getKitStorage().getKit("Specialist")))
                killer_player.giveExpLevels(1);

            killer.handleSidebar();
        } else {
            killed_player.sendMessage(killed_account.getLanguage().translate("pvp.arena.death_to_anyone"));
            verifyLostKillstreak(killed, null, killed_account.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInt());

            killed_account.addInt(1, Columns.PVP_ARENA_DEATHS);
            killed_account.getData(Columns.PVP_ARENA_KILLSTREAK).setData(0);

            killed_account.removeInt(6, rankingFactory.getTarget().getExperience());
            rankingFactory.verify(killed_account);

            killed_player.sendMessage("§4-6 XP");
        }

        if (event.getReason() == UserDiedEvent.Reason.KILL)
            killed.getGame().join(killed, true);

        async(() -> {
            killed_account.getDataStorage().saveTable(Tables.PVP);
            if (event.hasKiller())
                event.getKiller().getAccount().getDataStorage().saveTable(Tables.PVP);
        });
    }

    @Override
    public void handleSidebar(User user) {
        GameScoreboard gameScoreboard = user.getScoreboard();

        if (gameScoreboard == null)
            return;

        Account account = user.getAccount();
        List<String> scores = new ArrayList<>();

        gameScoreboard.updateTitle("§b§lPVP: ARENA");
        scores.add(" ");
        scores.add("§fKills: §7" + account.getData(Columns.PVP_ARENA_KILLS).getAsInteger());
        scores.add("§fDeaths: §7" + account.getData(Columns.PVP_ARENA_DEATHS).getAsInteger());
        scores.add(" ");
        if (!user.getKit1().isNone() || !user.getKit2().isNone()) {
            if (!user.getKit1().isNone())
                scores.add((user.getKit2().isNone() ? "§fKit: §a" + user.getKit1().getName() : "§fKit 1: §a" + user.getKit1().getName()));
            if (!user.getKit2().isNone())
                scores.add((user.getKit1().isNone() ? "§fKit: §a" + user.getKit2().getName() : "§fKit 2: §a" + user.getKit2().getName()));
        }
        scores.add("§fKillstreak: §a" + account.getData(Columns.PVP_ARENA_KILLSTREAK).getAsInteger());
        scores.add(" ");
        scores.add("§e" + Constants.SERVER_WEBSITE.replace("www.", ""));

        gameScoreboard.updateLines(scores);
    }

    @Override
    public void onLogin(User user) {

    }

    @Override
    public void quit(User user) {
        super.quit(user);
    }

    private void handleKitItems(User user) {
        Player player = user.getPlayer();

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.setItemOnCursor(null);
        player.getOpenInventory().getTopInventory().clear();

        Kit kit1 = user.getKit1(), kit2 = user.getKit2();

        ItemFactory sword = new ItemFactory(Material.STONE_SWORD).setUnbreakable();

        if (kit1.isNone() && kit2.isNone()) {
            sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        }

        player.getInventory().setItem(0, sword.getStack());

        Items.COMPASS.build(player);

        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 25));
        player.getInventory().setItem(14, new ItemStack(Material.RED_MUSHROOM, 25));
        player.getInventory().setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 25));

        if (kit1.getItems() != null)
            player.getInventory().addItem(kit1.getItems());

        if (kit2.getItems() != null)
            player.getInventory().addItem(kit2.getItems());

        for (int i = 0; i < 36; i++)
            player.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));

        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void jumpFrom(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.EMERALD_BLOCK) {
            Vector v = player.getLocation().getDirection().multiply(4.5).setY(-0.6);
            player.setVelocity(v);
        }
    }

    @EventHandler
    public void onLostProtection(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());
        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;
        if (!user.isKept())
            return;
        if (Vanish.getInstance().isVanished(player.getUniqueId()))
            return;
        if (player.getLocation().getY() > 115)
            return;
        user.setKept(false);
    }

    @EventHandler
    public void onLostProtection(PlayerProtectionRemoveEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        handleKitItems(user);
        handleSidebar(user);

        player.sendMessage(user.getAccount().getLanguage().translate("pvp.lost_protection"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageA(EntityDamageEvent localEntityDamageEvent) {
        if (!(localEntityDamageEvent.getEntity() instanceof Player))
            return;

        Player localEntity = (Player) localEntityDamageEvent.getEntity();
        User user = User.fetch(localEntity.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (localEntityDamageEvent.getCause() == EntityDamageEvent.DamageCause.FALL)
            return;

        if (user.isKept())
            localEntityDamageEvent.setCancelled(true);
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent localEntityDamageEvent) {
        if (!(localEntityDamageEvent.getEntity() instanceof Player))
            return;

        Player localEntity = (Player) localEntityDamageEvent.getEntity();
        User user = User.fetch(localEntity.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (localEntityDamageEvent.getCause() != EntityDamageEvent.DamageCause.VOID)
            return;

        localEntity.damage(20);
    }

    @EventHandler
    public void send(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Player p = (Player) e.getEntity();
            User user = User.fetch(p.getUniqueId());
            if (!user.getGame().getUniqueId().equals(getUniqueId()))
                return;

            if (!p.getInventory().contains(Material.COMPASS)) {
                e.setCancelled(true);
                return;
            }

            if (user.isCanFirstFall()) {
                e.setCancelled(true);
                user.setCanFirstFall(false);
            }
        }
    }

    @EventHandler
    public void onReceiveItems(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (!user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (user.isKept()) {
            event.setCancelled(true);
            return;
        }

        if (isBlockedMaterial(event.getItem().getItemStack().getType()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        if (user == null) {
            event.setCancelled(true);
            return;
        }

        if (player.getWorld().getUID().equals(Bukkit.getWorlds().get(0).getUID()) || !user.getGame().getUniqueId().equals(getUniqueId()))
            return;

        if (isBlockedMaterial(event.getItemDrop().getItemStack().getType()))
            event.setCancelled(true);
    }

    public boolean isBlockedMaterial(final Material material) {
        return !ACCEPTABLE_MATERIALS.contains(material);
    }

    private void verifyReceiveKillstreak(User user, int killstreak) {
        if (killstreak < 9 || killstreak % 10 != 0)
            return;
        user.getGame().getUsers().forEach(users -> users.getPlayer().sendMessage("§b" + user.getAccount().getDisplayName() + "§e atingiu um killstreak de §6" + killstreak + "§e!"));
    }

    private void verifyLostKillstreak(User user, User killer, int killstreak) {
        if (killstreak < 9)
            return;
        user.getGame().getUsers().forEach(users -> users.getPlayer().sendMessage("§b" + user.getAccount().getDisplayName() + "§e perdeu um killstreak de §6" + killstreak + (killer == null ? "§e!" : " §epara §b" + killer.getAccount().getDisplayName() + "§e!")));
    }

}