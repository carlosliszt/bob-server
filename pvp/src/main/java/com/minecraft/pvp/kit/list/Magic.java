/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.pvp.kit.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.pvp.kit.Kit;
import com.minecraft.pvp.kit.KitCategory;
import com.minecraft.pvp.user.User;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Magic extends Kit {

    private final MagicPotionEffect[] goodPotion;
    private final MagicPotionEffect[] badPotion;

    public Magic() {
        setIcon(new ItemFactory(Material.DIAMOND_HOE).glow().getStack());
        setItems(new ItemFactory(Material.DIAMOND_HOE).setName("§aLançar efeito").setDescription("§7Kit Magic").glow().getStack());
        setPrice(30000);
        setCategory(KitCategory.STRATEGY);
        setCooldown(10);
        this.goodPotion = new MagicPotionEffect[]{new MagicPotionEffect(true, 16385, buildPotion(MobEffectList.REGENERATION, 400, 0)), new MagicPotionEffect(true, 16386, buildPotion(MobEffectList.FASTER_MOVEMENT, 400, 0)), new MagicPotionEffect(true, 16419, buildPotion(MobEffectList.FIRE_RESISTANCE, 400, 0)), new MagicPotionEffect(true, 16453)};
        this.badPotion = new MagicPotionEffect[]{new MagicPotionEffect(false, 16426, buildPotion(MobEffectList.SLOWER_DIG, 300, 0)), new MagicPotionEffect(false, 16426, buildPotion(MobEffectList.SLOWER_MOVEMENT, 400, 0)), new MagicPotionEffect(false, 16388, buildPotion(MobEffectList.POISON, 400, 0)), new MagicPotionEffect(false, 16460)};
    }

    @Override
    public void resetAttributes(User user) {

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL && isUser(event.getPlayer())) {
            if (event.hasItem()) {
                Player player = event.getPlayer();
                if (isItem(player)) {
                    event.setCancelled(true);

                    if (isCooldown(player)) {
                        dispatchCooldown(player);
                        return;
                    }

                    addCooldown(player.getUniqueId(), CooldownType.DEFAULT, 10);

                    MagicPotionEffect potionEffect;

                    if (event.getAction().name().contains("LEFT"))
                        potionEffect = this.badPotion[Constants.RANDOM.nextInt(this.badPotion.length)];
                    else
                        potionEffect = this.goodPotion[Constants.RANDOM.nextInt(this.goodPotion.length)];

                    launchPotion(player, potionEffect);
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            Player attacker = (Player) event.getPotion().getShooter();
            for (final LivingEntity affectedEntity : event.getAffectedEntities()) {
                if (affectedEntity instanceof Player) {
                    if (affectedEntity.getEntityId() == attacker.getEntityId())
                        return;
                    User user = User.fetch(affectedEntity.getUniqueId());
                    user.addCombat(attacker.getUniqueId());
                }
            }
        }
    }

    private MobEffect buildPotion(final MobEffectList effect, final int durationTicks, final int amplification) {
        return new MobEffect(effect.id, durationTicks, amplification, true, true);
    }

    private void launchPotion(final Player player, final MagicPotionEffect valuePotion) {
        final WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final MagicEntityPotion entityPotion = new MagicEntityPotion(worldServer, entityPlayer, valuePotion);
        final Vector multiply = player.getLocation().getDirection().multiply(1.62);
        entityPotion.shoot(multiply.getX(), multiply.getY() + 0.2, multiply.getZ(), 0.75f, 8.0f);
        worldServer.addEntity(entityPotion);
    }

    private static class MagicPotionEffect {
        private final boolean onlyShooter;
        private final int value;
        private final MobEffect[] mobEffects;

        public MagicPotionEffect(final boolean onlyShooter, final int value, final MobEffect... mobEffects) {
            this.onlyShooter = onlyShooter;
            this.value = value;
            this.mobEffects = mobEffects;
        }
    }

    private static class MagicEntityPotion extends EntityPotion {
        private final MagicPotionEffect magicPotionEffect;

        public MagicEntityPotion(final World world, final EntityLiving entityliving, final MagicPotionEffect effect) {
            super(world, entityliving, effect.value);
            this.magicPotionEffect = effect;
        }

        public void shoot(double d0, double d1, double d2, final float f, final float f1) {
            if (this.item != null) {
                this.item.setData(this.magicPotionEffect.value);
            }
            final float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d0 /= f2;
            d1 /= f2;
            d2 /= f2;
            d0 += 0.007499999832361937 * f1;
            d1 += 0.007499999832361937 * f1;
            d2 += 0.007499999832361937 * f1;
            d0 *= f;
            d1 *= f;
            d2 *= f;
            this.motX = d0;
            this.motY = d1;
            this.motZ = d2;
            final float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            final float n = (float) (MathHelper.b(d0, d2) * 180.0 / 3.1415927410125732);
            this.yaw = n;
            this.lastYaw = n;
            final float n2 = (float) (MathHelper.b(d1, (double) f3) * 180.0 / 3.1415927410125732);
            this.pitch = n2;
            this.lastPitch = n2;
        }

        protected void a(final MovingObjectPosition movingobjectposition) {
            if (!this.world.isClientSide) {
                List<MobEffect> list;
                if (this.magicPotionEffect.mobEffects == null || this.magicPotionEffect.mobEffects.length == 0) {
                    list = Items.POTION.h(this.item);
                } else {
                    list = Arrays.asList(this.magicPotionEffect.mobEffects);
                }
                final AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(4.0, 2.0, 4.0);
                final List<EntityLiving> list2 = (List<EntityLiving>) this.world.a((Class) EntityLiving.class, axisalignedbb);
                final Iterator<EntityLiving> iterator = list2.iterator();
                final HashMap<LivingEntity, Double> affected = new HashMap<>();
                while (iterator.hasNext()) {
                    final EntityLiving entityliving = iterator.next();
                    final double d0 = this.h(entityliving);
                    if (d0 < 16.0) {
                        double d2 = 1.0 - Math.sqrt(d0) / 4.0;
                        if (entityliving == movingobjectposition.entity) {
                            d2 = 1.0;
                        }
                        affected.put((LivingEntity) entityliving.getBukkitEntity(), d2);
                    }
                }
                final PotionSplashEvent event = CraftEventFactory.callPotionSplashEvent(this, affected);
                if (!event.isCancelled() && list != null && !list.isEmpty()) {
                    for (final LivingEntity victim : event.getAffectedEntities()) {
                        if (!(victim instanceof CraftLivingEntity)) {
                            continue;
                        }
                        final EntityLiving entityliving2 = ((CraftLivingEntity) victim).getHandle();
                        if (this.magicPotionEffect.onlyShooter) {
                            if (this.shooter != entityliving2) {
                                continue;
                            }
                        } else if (this.shooter == entityliving2) {
                            continue;
                        }
                        final double d3 = event.getIntensity(victim);
                        for (final MobEffect mobeffect : list) {
                            final int i = mobeffect.getEffectId();
                            if (!this.world.pvpMode && this.getShooter() instanceof EntityPlayer && entityliving2 instanceof EntityPlayer && entityliving2 != this.getShooter()) {
                                if (i == 2 || i == 4 || i == 7 || i == 15 || i == 17 || i == 18) {
                                    continue;
                                }
                                if (i == 19) {
                                    continue;
                                }
                            }
                            if (MobEffectList.byId[i].isInstant()) {
                                MobEffectList.byId[i].applyInstantEffect(this, this.getShooter(), entityliving2, mobeffect.getAmplifier(), d3);
                            } else {
                                final int j = (int) (d3 * mobeffect.getDuration() + 0.5);
                                if (j <= 20) {
                                    continue;
                                }
                                entityliving2.addEffect(new MobEffect(i, j, mobeffect.getAmplifier()));
                            }
                        }
                    }
                }
                this.world.triggerEffect(2002, new BlockPosition((Entity) this), this.getPotionValue());
                this.die();
            }
        }
    }
}
