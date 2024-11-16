/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.CooldownType;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ninja extends Kit {

    private final Map<UUID, Player> targettedPlayers = new HashMap<>();

    public Ninja(HungerGames hungerGames) {
        super(hungerGames);
        setKitCategory(KitCategory.COMBAT);
        setIcon(Pattern.of(Material.NETHER_STAR));
        setCooldown(7);
        setPrice(50000);
        setCombatCooldown(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            Player attacker = (Player) event.getDamager();
            User user = User.fetch(attacker.getUniqueId());
            if (isUser(user)) {
                targettedPlayers.put(attacker.getUniqueId(), ((Player) event.getEntity()));
                user.getAccount().setProperty(PROPERTY_KEY, (System.currentTimeMillis() + 20000L));
            }
        }
    }

    final String PROPERTY_KEY = "kit.ninja.last_teleport";

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {

            Player player = event.getPlayer();

            if (!targettedPlayers.containsKey(player.getUniqueId()))
                return;

            if (isCooldown(player) || isCombat(player)) {
                dispatchCooldown(player);
                return;
            }

            User user = getUser(player.getUniqueId());

            if (user.getAccount().hasProperty(PROPERTY_KEY) && System.currentTimeMillis() > user.getAccount().getProperty(PROPERTY_KEY).getAsLong()) {
                user.getAccount().removeProperty(PROPERTY_KEY);
                targettedPlayers.remove(player.getUniqueId());
                return;
            }

            Player target = targettedPlayers.get(player.getUniqueId());
            User targetUser = getUser(target.getUniqueId());

            if (!targetUser.isOnline() || !targetUser.isAlive()) {
                player.sendMessage(user.getAccount().getLanguage().translate("kit.ninja.target_not_found"));
                return;
            }

            if (getKit("Neo").isUser(targetUser)) {
                player.sendMessage(user.getAccount().getLanguage().translate("kit.neo.not_affected"));
                return;
            }

            if (player.getLocation().distanceSquared(target.getLocation()) > 3025) { /* 55 blocks */
                player.sendMessage(user.getAccount().getLanguage().translate("kit.ninja.too_far_away"));
                return;
            }

            Gladiator gladiator = (Gladiator) getKit("Gladiator");

            if (gladiator.isGladiator(target) && !gladiator.isGladiator(player) || gladiator.isGladiator(target) && gladiator.isGladiator(player) && gladiator.getTarget(user) != targetUser)
                return;

            targettedPlayers.remove(player.getUniqueId());
            addCooldown(player.getUniqueId());
            player.teleport(target);
            user.getAccount().setProperty(PROPERTY_KEY, (System.currentTimeMillis() + 20000L));
        }
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {

        UUID killerUUID = event.getKiller().getUniqueId();

        if (targettedPlayers.containsKey(killerUUID) && targettedPlayers.get(killerUUID).equals(event.getUser().getPlayer()))
            targettedPlayers.remove(killerUUID);

        targettedPlayers.remove(event.getUser().getUniqueId());
    }

    @Variable(name = "hg.kit.ninja.combat_cooldown_time", permission = Rank.ADMINISTRATOR)
    @Setter
    public double duration = 0.5;

    @Override
    public double getCombatTime() {
        return duration;
    }

    @Override
    public void addCooldown(UUID uuid, CooldownType cooldownType, double duration) {

        boolean isCombat = cooldownType == CooldownType.COMBAT;

        if (getHungerGames().getKitStorage().isCooldown())
            CooldownProvider.getGenericInstance().addCooldown(uuid, cooldownType.getWord() + getDisplayName(), isCombat ? COMBAT : COOLDOWN, duration, !isCombat);
    }
}