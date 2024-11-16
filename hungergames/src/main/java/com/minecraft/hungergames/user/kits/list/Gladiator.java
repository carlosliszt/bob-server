/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.util.DateUtils;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.list.events.Scrim;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Gladiator extends Kit {

    private final List<GladiatorSession> gladiatorSessions = new ArrayList<>();

    public Gladiator(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.IRON_FENCE));
        setItems(new ItemFactory(Material.IRON_FENCE).setName("§aDesafiar").setDescription("§7Kit Gladiator").getStack());
        setKitCategory(KitCategory.COMBAT);
        setCooldown(12);
        setPrice(50000);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isPlayer(event.getRightClicked())) {

            Player player = event.getPlayer();
            Player clicked = (Player) event.getRightClicked();

            if (isUser(player) && isItem(player.getItemInHand())) {

                event.setCancelled(true);

                if (checkInvincibility(player))
                    return;

                if (isCooldown(player)) {
                    dispatchCooldown(player);
                    return;
                }

                User user = User.fetch(clicked.getUniqueId());

                if (!user.isAlive())
                    return;

                if (getKit("Neo").isUser(user)) {
                    player.sendMessage(user.getAccount().getLanguage().translate("kit.neo.not_affected"));
                    return;
                }

                if (isGladiator(player) || isGladiator(clicked))
                    return;

                if (clicked.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    player.sendMessage("§cAguarde para desafiar " + clicked.getName() + " novamente.");
                    return;
                }

                if (getGame() instanceof Scrim && user.getScrimSettings().isCleanTime()) {
                    player.sendMessage("§cAguarde " + DateUtils.formatDifference(user.getScrimSettings().getCleanTime(), Account.fetch(player.getUniqueId()).getLanguage(), DateUtils.Style.NORMAL) + " para desafiar " + clicked.getName() + " para um duelo.");
                    return;
                }

                final Location current = player.getLocation().clone();

                int radius = getGame().getVariables().getWorldSize() - 30;

                if (absolute(current.getX()) > radius || absolute(current.getZ()) > radius)
                    return;

                Location location = current;
                location.setY(140);

                while (!isFree(location)) {
                    location = new Location(getWorld(), randomize(radius), 140, randomize(radius));
                }

                gladiatorSessions.add(new GladiatorSession(getUser(player.getUniqueId()), getUser(clicked.getUniqueId()), location, player.getLocation().clone(), clicked.getLocation().clone(), 210).build().start());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {

            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (isGladiator(victim) && !isGladiator(attacker))
                event.setCancelled(true);
            else if (isGladiator(attacker) && !isGladiator(victim))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL && isUser(event.getPlayer())) {
            if (event.hasItem()) {
                if (isItem(event.getItem())) {
                    event.setCancelled(true);
                    event.getPlayer().updateInventory();
                }
            }
        }
    }

    @Variable(name = "hg.kit.gladiator.disable_obsidian", permission = Rank.ADMINISTRATOR)
    private boolean disableObsidian = false;

    @EventHandler
    public void onGladiatorObsidian(BlockFormEvent event) {
        if (!disableObsidian) {
            return;
        }

        if (event.getBlock().getY() > 128 && event.getNewState().getType() == Material.OBSIDIAN) {
            event.setCancelled(true);
            event.getBlock().setType(Material.COBBLESTONE);
        }
    }

    public boolean isGladiator(Player user) {
        for (GladiatorSession gladiatorSession : gladiatorSessions) {
            if (gladiatorSession.getChallenger().getPlayer().getEntityId() == user.getPlayer().getEntityId() || gladiatorSession.getChallenged().getPlayer().getEntityId() == user.getEntityId())
                return true;
        }
        return false;
    }

    public User getTarget(User user) {
        User response = null;

        for (GladiatorSession gladiatorSession : gladiatorSessions) {

            if (user.getPlayer().getEntityId() == gladiatorSession.getChallenged().getPlayer().getEntityId())
                response = gladiatorSession.getChallenger();

            if (user.getPlayer().getEntityId() == gladiatorSession.getChallenger().getPlayer().getEntityId())
                response = gladiatorSession.getChallenged();
        }

        return response;
    }

    public boolean isFree(Location location) {
        for (GladiatorSession gladiatorSession : this.gladiatorSessions) {
            if (gladiatorSession.getLocation().distanceSquared(location) <= 484)
                return false;
        }
        return true;
    }

    public void unregister(GladiatorSession gladiatorSession) {
        gladiatorSessions.remove(gladiatorSession);
    }

    @Getter
    public class GladiatorSession implements BukkitInterface, Assistance, Listener {

        private final User challenger, challenged;
        private final Location location;
        private Location challengerOldLocation, challengedOldLocation;
        private int time;
        private boolean appliedWither;

        public GladiatorSession(User challenger, User challenged, Location location, Location challengerOldLocation, Location challengedOldLocation, int time) {
            this.challenger = challenger;
            this.challenged = challenged;
            this.location = location;
            this.challengerOldLocation = challengerOldLocation;
            this.challengedOldLocation = challengedOldLocation;
            this.time = time;
        }

        public GladiatorSession build() {
            for (int y = 0; y <= 13; y++) {
                for (int x = -8; x <= 7; x++) {
                    for (int z = -8; z <= 7; z++) {
                        if (y == 0) {
                            Block floor = location.clone().add(x, 0.0d, z).getBlock();
                            floor.setType(Material.GLASS);
                            floor.setMetadata("unbreakable", new GameMetadata(true));
                        }
                        if (x == -8 || x == 7) {
                            Block walls_x = location.clone().add(x, y, z).getBlock();
                            walls_x.setType(Material.GLASS);
                            walls_x.setMetadata("unbreakable", new GameMetadata(true));
                        }

                        if (z == -8 || z == 7) {
                            Block walls_z = location.clone().add(x, y, z).getBlock();
                            walls_z.setType(Material.GLASS);
                            walls_z.setMetadata("unbreakable", new GameMetadata(true));
                        }
                    }
                }
            }
            return this;
        }

        public GladiatorSession start() {

            Bukkit.getPluginManager().registerEvents(this, getPlugin());

            Player player1 = getChallenger().getPlayer();
            Player player2 = getChallenged().getPlayer();

            player1.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), false);
            player2.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), false);

            Location first = location.clone().add(-5, 1.2, -5), second = location.clone().add(5, 1.2, 5);

            first.setYaw(-45);
            first.setPitch(0);
            second.setYaw(130);
            second.setPitch(0);

            player1.teleport(first);
            player2.teleport(second);

            return this;
        }

        @EventHandler
        public void onGameTime(ServerHeartbeatEvent event) {

            if (!event.isPeriodic(20))
                return;

            this.time--;

            if (this.time == 60) {
                this.appliedWither = true;
                getChallenger().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1200, 5));
                getChallenged().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1200, 5));
            } else if (this.time == 0) {
                cancel().undo();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onLivingUserDieEvent(LivingUserDieEvent event) {
            if (getChallenged().getUniqueId() == event.getUser().getUniqueId()) {
                event.setLocationToDrop(getChallengedOldLocation());
                cancel().undo();
            } else if (getChallenger().getUniqueId() == event.getUser().getUniqueId()) {
                event.setLocationToDrop(getChallengerOldLocation());
                cancel().undo();
            }
        }

        @EventHandler
        public void onMassiveTeleport(PlayerMassiveTeleportExecuteEvent event) { // Fixing massive teleport old Gladiator location.
            this.challengedOldLocation = event.getLocation();
            this.challengerOldLocation = event.getLocation();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (event.getPlayer().getUniqueId() == getChallenged().getUniqueId()) {
                if (getChallenged().isAlive() && !getChallenged().getCombatTag().isTagged())
                    new LivingUserDieEvent(getChallenged(), getChallenger(), true, DieCause.COMBAT, getChallenged().getInventoryContents(), getChallengedOldLocation()).fire();
            } else if (event.getPlayer().getUniqueId() == getChallenger().getUniqueId()) {
                if (getChallenger().isAlive() && !getChallenger().getCombatTag().isTagged())
                    new LivingUserDieEvent(getChallenger(), getChallenged(), true, DieCause.COMBAT, getChallenger().getInventoryContents(), getChallengerOldLocation()).fire();
            }
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (event.getPlayer().getUniqueId() == getChallenged().getUniqueId() || event.getPlayer().getUniqueId() == getChallenger().getUniqueId()) {
                if (event.getTo().getY() > 153.3 || event.getTo().getY() < 140.5)
                    cancel().undo();
            }
        }

        public void undo() {
            for (int x = -8; x <= 7; x++) {
                for (int z = -8; z <= 7; z++) {
                    for (int y = 0; y <= 13; y++) {
                        Location location = this.location.clone().add(x, y, z);
                        Block block = location.getBlock();
                        block.setType(Material.AIR);
                        block.removeMetadata("unbreakable", getPlugin());
                    }
                }
            }
        }

        public GladiatorSession cancel() {

            HandlerList.unregisterAll(this);
            unregister(this);

            Player player1 = getChallenger().getPlayer();
            Player player2 = getChallenged().getPlayer();

            player1.teleport(getChallengerOldLocation());
            player2.teleport(getChallengedOldLocation());

            if (getChallenger().isOnline())
                player1.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), true);
            if (getChallenged().isOnline())
                player2.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 255), true);

            addCooldown(player1.getUniqueId());
            if (isUser(getChallenged()))
                addCooldown(player2.getUniqueId());

            if (appliedWither) {
                player1.removePotionEffect(PotionEffectType.WITHER);
                player2.removePotionEffect(PotionEffectType.WITHER);
            }
            return this;
        }
    }
}
