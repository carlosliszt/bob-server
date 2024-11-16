/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.game.Game;
import com.minecraft.hungergames.game.list.events.Scrim;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.object.CombatTag;
import com.minecraft.hungergames.user.pattern.Condition;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.user.pattern.ItemType;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import com.minecraft.hungergames.util.stats.StatsApplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@RecurringListener(register = GameStage.INVINCIBILITY, unregister = GameStage.VICTORY)
public class UserDeath implements Listener, BukkitInterface, Assistance, VariableStorage {

    public UserDeath() {
        loadVariables();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {

        event.setDeathMessage(null);

        User user = getUser(event.getEntity().getUniqueId());

        user.getPlayer().setHealth(20);

        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        event.getDrops().clear();

        CombatTag combatTag = user.getCombatTag();

        User killer = (event.getEntity().getKiller() != null ? getUser(event.getEntity().getKiller().getUniqueId()) : combatTag.isTagged() ? combatTag.getLastHit() : null);

        DieCause dieCause = (killer == null ? DieCause.DIE : (user == killer ? DieCause.SUICIDE : DieCause.KILLED));

        LivingUserDieEvent livingUserDieEvent = new LivingUserDieEvent(user, killer, dieCause != DieCause.SUICIDE, dieCause, drops, user.getPlayer().getLocation());
        livingUserDieEvent.fire();
    }

    @Variable(name = "hg.respawn", announce = true)
    private boolean respawn = true;

    @EventHandler
    public void onLivingUserDie(LivingUserDieEvent event) {

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        drops.removeIf(drop -> {

            if (drop == null || drop.getType() == Material.AIR)
                return true;

            return hasKey(drop, "undroppable");
        });

        event.setLocationToDrop(event.getLocationToDrop().add(0, 0.2, 0));

        drops.forEach(item -> event.getUser().getPlayer().getWorld().dropItemNaturally(event.getLocationToDrop(), item));
        drops.clear();

        User user = event.getUser();
        User killer = event.getKiller();

        Account account = user.getAccount();

        final boolean hasKiller = killer != null;
        final boolean countStatus = !hasKiller || event.isCountStats() && getGame().getVariables().isCountStats() && !killer.getVictims().contains(user.getUniqueId());

        if (hasKiller)
            killer.addKill();

        if (countStatus) { // Giving statistics.

            if (hasKiller) {
                StatsApplier.KILL.apply(killer);

                for (Kit kit : killer.getKits())
                    kit.appreciate(event);
            }

            StatsApplier.DEATH.apply(user);
        }

        DieCause dieCause = event.getDieCause();
        Game game = getGame();

        if (hasKiller) {
            killer.getPlayer().playSound(killer.getPlayer().getLocation(), Sound.ORB_PICKUP, 4F, 4F);
            killer.addVictim(user.getUniqueId());

            if (game instanceof Scrim) {
                killer.getScrimSettings().setCleanTime(System.currentTimeMillis() + 40000L);
            }

        }

        user.getPlayer().getOpenInventory().getTopInventory().clear();

        if (account.hasPermission(Rank.VIP) && dieCause.isRespawnable() && user.isOnline()) {
            if (isLateLimit() && respawn) {
                user.setCondition(Condition.ALIVE);
                teleport(user.getPlayer());
                game.die(user);
            } else {
                user.setCondition(Condition.SPECTATOR);
                game.die(user);
                broadcast(event);
            }
        } else {
            if (account.hasPermission(Rank.VIP)) {
                user.setCondition(Condition.SPECTATOR);
                game.die(user);
                broadcast(event);
            } else {
                user.setCondition(Condition.DEAD);
                broadcast(event);
                if (user.isOnline()) {
                    user.setCondition(Condition.SPECTATOR);
                    game.die(user);
                }
            }
        }
    }

    public void teleport(Player player) {
        int range = HungerGames.getInstance().getGame().getVariables().getWorldSize() - 40;
        int x = randomize(range - 150, range), z = randomize(range - 150, range);

        Location location = new Location(getWorld(), x, getWorld().getHighestBlockYAt(x, z), z);
        player.teleport(location);
    }

    public void broadcast(LivingUserDieEvent event) {

        String victim = event.getUser().getName() + (event.getDieCause().isNeedKit() ? event.getUser().getKitContainer(true) : "");
        String killer = "";
        Material material = Material.AIR;

        if (event.getKiller() != null) {
            killer = event.getKiller().getName() + (event.getDieCause().isNeedKit() ? event.getKiller().getKitContainer(true) : "");
            material = (event.getKiller().getPlayer().getItemInHand() == null ? Material.AIR : event.getKiller().getPlayer().getItemInHand().getType());
        }

        String brazilian = ItemType.getString(Language.PORTUGUESE, material), english = ItemType.getString(Language.ENGLISH, material);

        int count = (int) getPlugin().getUserStorage().getUsers().stream().filter(User::isAlive).count();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Account account = Account.fetch(p.getUniqueId());
            if (account == null)
                continue;
            Language language = account.getLanguage();
            p.sendMessage(language.translate(event.getDieCause().getMessage(), killer, victim, (language == Language.PORTUGUESE ? brazilian : english)));
            p.sendMessage(language.translate("hg.game.user_death.remaining_players", count));
        }
    }
}
