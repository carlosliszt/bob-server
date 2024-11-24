/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.CooldownType;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.HorseInventory;

import java.util.HashMap;
import java.util.UUID;

public class Rider extends Kit {

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);
    private final HashMap<UUID, Entity> horses = new HashMap<>();
    @Variable(name = "hg.kit.rider.combat_cooldown", permission = Rank.ADMINISTRATOR)
    public double duration = 6;

    public Rider(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.SADDLE));
        setItems(new ItemFactory(Material.SADDLE).setName("§aMontar").setDescription("§7Kit Rider").getStack());
        setKitCategory(KitCategory.MOVEMENT);
        setCooldown(5);
        setCombatCooldown(true);
        setPrice(35000);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && ACCEPTABLES_INTERACT.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            if (isCooldown(player) || isCombat(player)) {
                dispatchCooldown(player);
                return;
            }

            if (horses.containsKey(player.getUniqueId())) {

                Horse horse = (Horse) horses.get(player.getUniqueId());

                if (horse.getTicksLived() < 3)
                    return;

                horse.remove();
                horses.remove(player.getUniqueId());
                addCooldown(player.getUniqueId());
            } else {

                if (((CraftPlayer) player).getHandle().inBlock()) {
                    player.sendMessage("§cVocê não pode usar o kit Rider agora.");
                    return;
                }

                Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
                horses.put(player.getUniqueId(), horse);
                horse.setCustomName("§fCavalo de §7" + player.getName());
                horse.setCustomNameVisible(true);
                horse.setAdult();
                horse.setStyle(Horse.Style.WHITE);
                horse.setColor(Horse.Color.WHITE);
                horse.setAgeLock(true);
                horse.setBreed(false);
                horse.setVariant(Horse.Variant.HORSE);
                horse.setMaxHealth(40);
                horse.setHealth(40);
                horse.setOwner(event.getPlayer());
                horse.setJumpStrength(1.25);
                horse.getInventory().setSaddle(new ItemFactory(Material.SADDLE).setName("§aSela").setDescription("§7Kit Rider").getStack());
                horse.setPassenger(event.getPlayer());
                AttributeInstance attributes = (((CraftLivingEntity) horse).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
                attributes.setValue(0.345);
                horse.setMetadata("kit.rider", new GameMetadata(true));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Horse) {
            Horse horse = (Horse) event.getEntity();

            if (!horse.hasMetadata("kit.rider"))
                return;

            User user = getUser(horse.getOwner().getUniqueId());

            if (user == null)
                return;

            if (isUser(user)) {
                addCooldown(user.getUniqueId(), CooldownType.COMBAT, duration);
                horses.remove(user.getUniqueId());
                event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            if (event.getClickedInventory() instanceof HorseInventory && horses.containsKey(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUserDieEvent(LivingUserDieEvent event) {

        Player player = event.getUser().getPlayer();

        if (player.getVehicle() != null) {
            player.getVehicle().eject();
        }

        if (horses.containsKey(player.getUniqueId())) {
            horses.get(player.getUniqueId()).remove();
            horses.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getPassenger() != null)
            return;
        if (horses.containsValue(e.getRightClicked())) {
            if (!horses.containsKey(e.getPlayer().getUniqueId()) || horses.get(e.getPlayer().getUniqueId()).getUniqueId() != e.getRightClicked().getUniqueId()) {
                e.getPlayer().sendMessage("§cVocê não pode montar neste cavalo.");
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void removeItems(Player player) { // Improving - Kit Rider bug fix
        if(horses.containsKey(player.getUniqueId()))
            horses.get(player.getUniqueId()).remove();
        super.removeItems(player);
    }

    @Override
    public double getCombatTime() {
        return duration;
    }
}
