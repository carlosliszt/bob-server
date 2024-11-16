/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.handler.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.bossbar.Bossbar;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.variable.object.VariableValidation;
import com.minecraft.hungergames.event.game.GameTimeEvent;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.structure.Feast;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.Gladiator;
import com.minecraft.hungergames.user.kits.pattern.CooldownType;
import com.minecraft.hungergames.user.object.CombatTag;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

@RecurringListener(register = GameStage.PLAYING, unregister = GameStage.VICTORY)
public class PlayingListener implements Listener, Assistance, BukkitInterface, VariableStorage {

    @Variable(name = "hg.border.damage")
    public int damage = 3;

    @Variable(name = "hg.border.damageable")
    public boolean damageable = true;

    @Variable(name = "hg.food_level_change")
    public boolean food_level_change = true;

    @Variable(name = "hg.regain_health")
    public boolean regain_health = true;

    public PlayingListener() {
        loadVariables();
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        if (!food_level_change) {
            event.setCancelled(true);
            return;
        }

        if (event.getFoodLevel() < ((Player) event.getEntity()).getFoodLevel()) {
            if (Constants.RANDOM.nextInt(3) < 2)
                event.setCancelled(true);
        }
    }

    @Variable(name = "hg.display_kits")
    public boolean displayKits = true;

    @EventHandler
    public void onGameTime(GameTimeEvent event) {
        getPlugin().getUserStorage().getUsers().forEach(user -> {
            try {
                if (!user.isOnline())
                    return;

                Bossbar bossbar = user.getBossbar();
                CombatTag combatTag = user.getCombatTag();

                if (bossbar == null)
                    user.setBossbar(bossbar = getPlugin().getBossbarProvider().getBossbar(user.getPlayer()));

                if (displayKits && user.getCombatTag().isTagged() && System.currentTimeMillis() + 8000L < combatTag.getTagTime()) {
                    User target = user.getCombatTag().getLastHit();
                    String kitContainer = target.getKitContainer(false);
                    if (kitContainer.isEmpty())
                        displayBossbar(bossbar);
                    else
                        bossbar.setMessage("§b" + target.getPlayer().getName() + " - " + target.getKitContainer(false));
                } else displayBossbar(bossbar);

                bossbar.setPercentage(1F);
                getPlugin().getBossbarProvider().updateBossbar(user.getPlayer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    protected void displayBossbar(Bossbar bossbar) {
        Game game = getGame();
        Feast feast = game.getFeast();

        if (feast != null && feast.isSpawned() && !feast.isFilled())
            bossbar.setMessage("§e§lFEAST EM §b§l" + format(feast.getTime()));
        else if (game.getVariables().isFinalArenaSpawn()) {
            int remaining = game.getVariables().getFinalArena() - game.getTime();

            if (remaining <= 300 && remaining > 0)
                bossbar.setMessage("§e§lARENA FINAL EM §b§l" + format(remaining));
            else
                bossbar.setMessage("§b§l" + getGame().getDisplay().toUpperCase() + " NO BOBMC.COM");
        } else
            bossbar.setMessage("§b§l" + getGame().getDisplay().toUpperCase() + " NO BOBMC.COM");
    }

    @EventHandler
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        if (!regain_health) {
            event.setCancelled(true);
            return;
        }

        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN)
            event.setCancelled(Constants.RANDOM.nextInt(4) <= 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            User user = getUser(event.getEntity().getUniqueId());
            User attacker = getUser(event.getDamager().getUniqueId());
            user.getCombatTag().addTag(attacker, 15);
        } else if (isPlayer(event.getEntity()) && event.getDamager() instanceof Projectile) {

            Entity entity = event.getEntity();
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {

                Player shooter = (Player) projectile.getShooter();

                if (entity.getEntityId() != shooter.getEntityId()) {
                    User attacker = getUser(shooter.getUniqueId());
                    User user = getUser(entity.getUniqueId());
                    user.getCombatTag().addTag(attacker, 15);
                }
            }
        }
    }

    @EventHandler
    public void onGameTime(ServerHeartbeatEvent event) {

        if (!event.isPeriodic(20))
            return;

        final int range = (getGame().getVariables().getWorldSize() - 5);
        final int height = getGame().getVariables().getWorldHeight();

        Gladiator gladiator = (Gladiator) getKit("Gladiator");

        for (User user : getPlugin().getUserStorage().getAliveUsers()) {

            Player player = user.getPlayer();

            if (Account.fetch(player.getUniqueId()).hasProperty("hg.kit.launcher.no_fall_damage"))
                continue;

            if (player.getGameMode() == GameMode.CREATIVE)
                continue;

            if (gladiator.isGladiator(player))
                continue;

            Location location = player.getLocation();

            if (damageable && absolute(location.getX()) >= range || location.getY() >= height || absolute(location.getZ()) >= range) {
                if (user.isAlive()) {
                    player.sendMessage(user.getAccount().getLanguage().translate("hg.game.out_of_world"));
                    player.damage(damage);
                } else
                    player.teleport(getGame().getVariables().getSpawnpoint());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            User user = getUser(event.getEntity().getUniqueId());

            for(Kit kit : user.getKits()) {
                if (kit.isCombatCooldown()) {
                    kit.addCooldown(user.getUniqueId(), CooldownType.COMBAT, kit.getCombatTime());
                }
            }

        } else if (isPlayer(event.getEntity()) && event.getDamager() instanceof Projectile) {

            Entity entity = event.getEntity();
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {

                Player shooter = (Player) projectile.getShooter();

                if (entity.getEntityId() != shooter.getEntityId()) {

                    User user = getUser(entity.getUniqueId());

                    for(Kit kit : user.getKits()) {
                        if (kit.isCombatCooldown()) {
                            kit.addCooldown(user.getUniqueId(), CooldownType.COMBAT, kit.getCombatTime());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        if (getTime() <= 300)
            event.setCancelled(true);
    }

    @VariableValidation(value = {"hg.border.damage", "hg.soup.heal_amount"})
    public boolean validate(int x) {
        return x > 0 && x <= 20;
    }

}
