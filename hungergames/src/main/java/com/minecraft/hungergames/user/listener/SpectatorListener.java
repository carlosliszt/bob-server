/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.listener;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerMassiveTeleportExecuteEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import com.minecraft.core.translation.Language;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import java.util.Iterator;
import java.util.Set;

@RecurringListener(register = GameStage.INVINCIBILITY, unregister = GameStage.NONE)
public class SpectatorListener implements Listener, BukkitInterface, Assistance {

    private static final Location limbo = new Location(Bukkit.getWorlds().get(0), 100, 3, 100);

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSpectator(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMassiveTeleportExecute(PlayerMassiveTeleportExecuteEvent event) {
        event.getRecipients().removeIf(player -> {
            User user = getUser(player.getUniqueId());
            return !user.isAlive() && !user.getAccount().hasPermission(Rank.VIP);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isSpectator(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isSpectator(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isSpectator(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        Iterator<LivingEntity> livingEntityIterator = event.getAffectedEntities().iterator();

        if (event.getEntity().getShooter() == null || !(event.getEntity().getShooter() instanceof Entity))
            return;

        Entity entity = (Entity) event.getEntity().getShooter();

        while (livingEntityIterator.hasNext()) {

            LivingEntity livingEntity = livingEntityIterator.next();

            if (isPlayer(livingEntity)) {

                User user = getUser(livingEntity.getUniqueId());

                if (!user.isAlive() && entity.getUniqueId() != livingEntity.getUniqueId())
                    livingEntityIterator.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (isPlayer(event.getEntity())) {
            if (isSpectator((Player) event.getEntity()))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityFood(FoodLevelChangeEvent event) {
        User user = getUser(event.getEntity().getUniqueId());
        if (!user.isAlive())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        User user = getUser(player.getUniqueId());

        if (user.isAlive())
            return;

        if (user.getAccount().hasPermission(Rank.PARTNER_PLUS))
            return;

        event.setCancelled(true);

        Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "chat.cooldown");

        Set<Player> recipients = event.getRecipients();

        recipients.removeIf(p -> !p.getWorld().getUID().equals(player.getWorld().getUID()) || User.fetch(p.getUniqueId()).isAlive());

        Account account = Account.fetch(player.getUniqueId());

        if (cooldown != null && !cooldown.expired()) {
            event.getPlayer().sendMessage(account.getLanguage().translate("wait_to_chat", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining())));
            return;
        } else {
            CooldownProvider.getGenericInstance().addCooldown(event.getPlayer().getUniqueId(), "chat.cooldown", 3, false);
        }

        Tag tag = account.getProperty("account_tag").getAs(Tag.class);

        recipients.forEach(recipient -> {
            Account account_recipient = Account.fetch(recipient.getUniqueId());
            PrefixType prefixType = account_recipient.getProperty("account_prefix_type").getAs(PrefixType.class);
            String prefix = account_recipient.getLanguage() == Language.PORTUGUESE ? "§7[ESPECTADOR] " : "§7[SPECTATOR] ";
            recipient.sendRawMessage(prefix + (tag == Tag.MEMBER ? tag.getMemberSetting(prefixType) : prefixType.getFormatter().format(tag).replace("#", account.getProperty("account_pluscolor").getAs(PlusColor.class).getColor() + "+")) + account.getDisplayName() + " §7»§r " + event.getMessage());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isSpectator(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        User user = getUser(event.getPlayer().getUniqueId());

        if (user == null)
            return;

        if (!user.isAlive())
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {

        if (!isPlayer(event.getEntity()))
            return;

        User user = getUser(event.getTarget().getUniqueId());
        if (!user.isAlive())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (isSpectator(event.getPlayer()))
            event.setAmount(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerExpChange(EntityDamageByEntityEvent event) {
        if (isPlayer(event.getDamager())) {
            if (isSpectator((Player) event.getDamager()))
                event.setCancelled(true);
        }
    }

    protected boolean isSpectator(Player player) {
        User user = getUser(player.getUniqueId());
        return !user.isAlive() && !user.isVanish();
    }
}
