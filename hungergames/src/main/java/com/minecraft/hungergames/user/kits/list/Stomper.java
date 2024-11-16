/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.list.ClanxClan;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.user.pattern.DieCause;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public class Stomper extends Kit {

    public Stomper(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.IRON_BOOTS));
        setKitCategory(KitCategory.STRATEGY);
        setPrice(50000);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && isPlayer(event.getEntity())) {

            Player player = (Player) event.getEntity();

            if (player.getLocation().getBlockY() > 128)
                return;

            if (!isUser(player))
                return;

            double dmg = event.getDamage();

            if (dmg <= 2)
                return;

            event.setDamage(Math.min(dmg, 4));

            if (Account.fetch(player.getUniqueId()).hasProperty("hg.kit.launcher.no_fall_damage"))
                return;

            for (Entity entity : player.getNearbyEntities(5, 3, 5)) {

                double damage = dmg;

                if (!isPlayer(entity))
                    continue;

                if (entity.getEntityId() == player.getEntityId())
                    continue;

                Player target = (Player) entity;

                if (target.getNoDamageTicks() > 20)
                    return;

                if (target.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
                    continue;

                if (target.isSneaking())
                    damage = 4;

                User user = getUser(target.getUniqueId());

                if (getGame() instanceof ClanxClan) {
                    ClanxClan clanxClan = (ClanxClan) getGame();
                    if (!clanxClan.isFriendlyFire() && clanxClan.isFriendly(user, User.fetch(player.getUniqueId())))
                        continue;
                }

                if (!user.isAlive())
                    continue;

                if (getKit("AntiTower").isUser(user))
                    damage = 7;

                if ((target.getHealth() - damage) <= 0) {
                    new LivingUserDieEvent(user, getUser(player.getUniqueId()), true, DieCause.KILLED, user.getInventoryContents(), target.getLocation()).fire();
                } else {
                    target.damage(damage, player);
                }
            }
        }
    }
}
