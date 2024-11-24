/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.constructor.Assistance;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class Phantom extends Kit {

    public Phantom(HungerGames hungerGames) {
        super(hungerGames);
        setCooldown(25);
        setIcon(Pattern.of(Material.FEATHER));
        setItems(new ItemFactory(Material.FEATHER).setName("§aVoar").setDescription("§7Kit Phantom").getStack());
        setCombatCooldown(true);
        setPrice(35000);
        setKitCategory(KitCategory.MOVEMENT);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @Variable(name = "hg.kit.phantom.fly_time", permission = Rank.ADMINISTRATOR)
    public int time = 6;

    @Variable(name = "hg.kit.phantom.combat_cooldown", permission = Rank.ADMINISTRATOR)
    public double duration = 4;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (isCooldown(player) || isCombat(player)) {
            dispatchCooldown(player);
            return;
        }

        addCooldown(player.getUniqueId());

        new PhantomSession(player, time).activate();
    }

    @Getter
    public static class PhantomSession implements Listener, Assistance {

        protected final static Color color = Color.fromRGB(255, 255, 255);
        protected final static ItemStack helmet = new ItemFactory(Material.LEATHER_HELMET).setUnbreakable().setColor(color).addTag("kit.phantom.armor");
        protected final static ItemStack chestplate = new ItemFactory(Material.LEATHER_CHESTPLATE).setUnbreakable().setColor(color).addTag("kit.phantom.armor");
        protected final static ItemStack leggings = new ItemFactory(Material.LEATHER_LEGGINGS).setUnbreakable().setColor(color).addTag("kit.phantom.armor");
        protected final static ItemStack boots = new ItemFactory(Material.LEATHER_BOOTS).setUnbreakable().setColor(color).addTag("kit.phantom.armor");
        protected final static ImmutableSet<Material> REMOVE_DROPS = Sets.immutableEnumSet(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);

        private final Player player;
        private int flyTime;
        private ItemStack[] armorContents;

        public PhantomSession(Player player, int flyTime) {
            this.player = player;
            this.flyTime = flyTime;
        }

        public void activate() {

            Bukkit.getPluginManager().registerEvents(this, getPlugin());

            player.teleport(player.getLocation().clone().add(0, 0.7, 0));
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setFallDistance(0);

            ((CraftPlayer) player).getHandle().updateAbilities();

            if (!isArmor(player.getInventory().getHelmet()))
                this.armorContents = player.getInventory().getArmorContents();

            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);

            player.playSound(player.getLocation(), "mob.wither.death", 10, 1);

            for (Entity entity : player.getNearbyEntities(15, 15, 15)) {

                if (!isPlayer(entity))
                    continue;

                ((Player) entity).playSound(entity.getLocation(), "mob.wither.death", 1, 10);
            }
        }

        public void cancel() {
            HandlerList.unregisterAll(this);
            player.setFlying(false);
            player.setAllowFlight(false);
            player.getInventory().setArmorContents(this.armorContents);
            player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 10.0F, 1.0F);
            player.setFallDistance(player.getFallDistance() / 2);
            this.armorContents = null;
        }

        @EventHandler
        public void onHeartBeat(ServerHeartbeatEvent event) {

            if (!event.isPeriodic(20))
                return;

            if (flyTime == 0)
                cancel();
            else {
                if (flyTime <= 3) {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 10, 1);
                    player.sendMessage("§c" + flyTime + "...");
                }

                --flyTime;

            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (checkPlayer(event.getPlayer()))
                cancel();
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {

            if (!checkPlayer((Player) event.getWhoClicked()))
                return;

            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onUserDeath(LivingUserDieEvent event) {
            if (checkPlayer(event.getUser().getPlayer()))
                event.getDrops().removeIf(item -> REMOVE_DROPS.contains(item.getType()));
        }

        public boolean checkPlayer(Player player) {
            return player.getEntityId() == this.player.getEntityId();
        }

        public boolean isArmor(ItemStack itemStack) {
            return itemStack != null && itemStack.equals(helmet);
        }
    }

    @Override
    public double getCombatTime() {
        return duration;
    }

}
