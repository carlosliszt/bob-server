/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.metadata.GameMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Archer extends Kit {

    private final HashMap<UUID, Skill> playerMap = new HashMap<>();

    private Skill DEFAULT = Skill.FIRE;

    public Archer(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.BOW));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.BOW).setDescription("§7Kit Archer").setName(ChatColor.YELLOW + DEFAULT.getName()).getStack());
        setPrice(35000);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        ItemStack itemStack = player.getItemInHand();

        if (!isItem(itemStack))
            return;

        if (CooldownProvider.getGenericInstance().hasCooldown(player, "kit.archer.switch"))
            return;

        Skill skill = playerMap.computeIfAbsent(player.getUniqueId(), s -> DEFAULT);

        if (skill == Skill.KNOCKBACK)
            itemStack.removeEnchantment(Enchantment.ARROW_KNOCKBACK);

        playerMap.put(player.getUniqueId(), skill = skill.next());

        if (skill == Skill.KNOCKBACK)
            itemStack.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + skill.getName());
        itemStack.setItemMeta(itemMeta);

        player.sendMessage("§aSkill alterada para " + skill.getName());
        player.updateInventory();
        CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "kit.archer.switch", 0.5, false);
    }

    @Override
    public void grant(Player player) {
        super.grant(player);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 20));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) arrow.getShooter();

        if (!isItem(player.getItemInHand())) {
            return;
        }

        if (!isUser(player))
            return;

        Skill skill = playerMap.computeIfAbsent(player.getUniqueId(), s -> DEFAULT);

        /*if (skill == Skill.FREEZE) {
            CooldownProvider cooldownProvider = CooldownProvider.getGenericInstance();
            Cooldown cooldown = cooldownProvider.getCooldown(player.getUniqueId(), "hg.kit.archer.freeze");

            if (cooldown != null) {
                event.setCancelled(true);
                player.sendMessage("§cAguarde " + Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining()) + "s para usar o congelamento novamente.");
                return;
            }
        }*/

        arrow.setMetadata("skill", new GameMetadata(skill));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow))
            return;
        Arrow arrow = (Arrow) event.getDamager();

        if (!arrow.hasMetadata("skill"))
            return;

        Skill skill = (Skill) arrow.getMetadata("skill").get(0).value();
        skill.getApplier().apply(event);

        if (event.getEntity() instanceof Player) {
            if (!(arrow.getShooter() instanceof Player))
                return;

            Player shooter = (Player) arrow.getShooter();

            if (isUser(shooter)) {
                if (shooter.getEntityId() != event.getEntity().getEntityId()) {
                    shooter.getInventory().addItem(new ItemStack(Material.ARROW));

                    if (Constants.RANDOM.nextBoolean())
                        shooter.getInventory().addItem(new ItemStack(Material.ARROW));
                }

                event.setDamage(event.getDamage() * 0.75);
            }
        }
    }

    @AllArgsConstructor
    @Getter
    public enum Skill {

        FIRE(0, "Fogo", event -> {
            event.getEntity().setFireTicks(event.getEntity().getFireTicks() + 80);
        }),

        /*FREEZE(1, "Congelar", event -> {

            Player shooter = (Player) ((Arrow) event.getDamager()).getShooter();

            CooldownProvider cooldownProvider = CooldownProvider.getGenericInstance();
            cooldownProvider.addCooldown(shooter.getUniqueId(), "hg.kit.archer.freeze", 12, false);

            if (!event.getEntity().hasMetadata("freeze"))
                event.getEntity().setMetadata("freeze", new GameMetadata(true));
        }),*/

        POISON(1, "Veneno", event -> {
            Player player = (Player) event.getEntity();
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 2));
        }),

        SLOWNESS(2, "Lentidão", event -> {
            Player player = (Player) event.getEntity();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
        }),

        KNOCKBACK(3, "Knockback", event -> {
            //Nothing
        }),

        DAMAGE(4, "Dano", event -> {
            event.setDamage(event.getDamage() + 2);
        });

        private int id;
        private final String name;
        private Applier applier;

        interface Applier {
            void apply(EntityDamageByEntityEvent event);
        }

        public Skill next() {
            if (this == DAMAGE)
                return FIRE;
            return fromId(this.id + 1);
        }

        public static Skill fromId(int id) {
            return Arrays.stream(values()).filter(c -> c.getId() == id).findFirst().orElse(null);
        }
    }
}
