/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.protocol.PacketReceiveEvent;
import com.minecraft.core.bukkit.event.protocol.PacketSendEvent;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.hungergames.util.constructor.Assistance;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Chameleon extends Kit {

    private final HashMap<UUID, Disguise> playerMap = new HashMap<>();

    public Chameleon(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WHEAT));
        setItems(new ItemFactory(Material.WHEAT).setName("§aTransformar").setDescription("§7Kit Chameleon").getStack());
        setKitCategory(KitCategory.STRATEGY);
        setCombatCooldown(true, 8);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPacketReceive(PacketReceiveEvent event) {
        sync(() -> {
            if (event.getPacket() instanceof PacketPlayInUseEntity) {
                Player player = event.getPlayer();
                int i = event.getValue("a");
                String action = event.getValue("action").toString();

                org.bukkit.entity.Entity entity = new ArrayList<>(player.getWorld().getEntities()).stream().filter(e -> e.getEntityId() == i).findFirst().orElse(null);

                if (entity == null)
                    return;

                if (isPlayer(entity)) {
                    if (!action.equals("ATTACK")) {
                        final Player attacked = (Player) entity;
                        if (isUser(attacked)) {
                            event.setCancelled(true);
                            Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(player, attacked));
                        }
                    }
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPacketSend(PacketSendEvent event) {
        sync(() -> {
            if (event.getPacket() instanceof PacketPlayOutNamedEntitySpawn) {
                Player player = event.getPlayer();
                int i = event.getValue("a");
                org.bukkit.entity.Entity entity = new ArrayList<>(player.getWorld().getLivingEntities()).stream().filter(e -> e.getEntityId() == i).findFirst().orElse(null);
                if (isPlayer(entity)) {
                    Player target = (Player) entity;

                    if (isUser(target) && playerMap.containsKey(entity.getUniqueId())) {
                        Disguise disguise = playerMap.get(entity.getUniqueId());
                        if (disguise.isDisguised())
                            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> transform(target, disguise.getCurrentEntityType()), 2L);
                    }
                }
            }
        });
    }

    private final ImmutableSet<EntityType> BAD_MOBS = Sets.immutableEnumSet(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.BLAZE, EntityType.WOLF, EntityType.SLIME, EntityType.CAVE_SPIDER, EntityType.SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.WITCH);

    @Variable(name = "hg.kit.chameleon.mobs_ignore", permission = Rank.ADMINISTRATOR)
    public boolean ignore = true;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (ignore && isPlayer(event.getTarget())) {
            Player player = (Player) event.getTarget();

            if (isUser(player)) {

                if (!playerMap.containsKey(player.getUniqueId()))
                    return;

                Disguise disguise = playerMap.get(player.getUniqueId());

                if (disguise.isDisguised() && BAD_MOBS.contains(disguise.getCurrentEntityType()))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL && isUser(event.getPlayer())) {
            if (isItem(event.getItem())) {
                Player player = event.getPlayer();
                event.setCancelled(true);
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    return;
                }
                final Disguise disguise = this.playerMap.computeIfAbsent(event.getPlayer().getUniqueId(), v -> new Disguise());
                if (disguise.getDelay() > System.currentTimeMillis()) {
                    return;
                }
                disguise.setDelay(System.currentTimeMillis() + 300L);

                if (isCombat(player)) {
                    dispatchCooldown(player);
                    return;
                }

                if (disguise.getEntityList().isEmpty()) {
                    player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("kit.chameleon.no_disguise"));
                    return;
                }

                EntityType first = null;
                boolean next = false;
                for (final EntityType entityType : disguise.getEntityList()) {
                    if (next) {
                        first = entityType;
                        break;
                    }
                    if (entityType != disguise.getCurrentEntityType()) {
                        continue;
                    }
                    next = true;
                }
                if (!next) {
                    first = disguise.getEntityList().get(0);
                }
                if (first != null) {
                    this.changeDisguise(disguise, player, first);
                } else if (disguise.isDisguised()) {
                    disguise.cancel(player);
                    player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("kit.chameleon.remove_disguise"));
                }
                player.setItemInHand(disguise.getIcon(player));
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isPlayer(event.getRightClicked()))
            return;

        if (isUser(event.getPlayer())) {
            if (isItem(event.getPlayer().getItemInHand())) {
                final EntityType type = event.getRightClicked().getType();
                event.setCancelled(true);
                if (this.getEntityLiving(type) == null) {
                    return;
                }
                final Disguise disguise = playerMap.computeIfAbsent(event.getPlayer().getUniqueId(), v -> new Disguise());
                if (!disguise.getEntityList().contains(type)) {
                    disguise.getEntityList().add(type);
                    changeDisguise(disguise, event.getPlayer(), type);
                    event.getPlayer().setItemInHand(disguise.getIcon(event.getPlayer()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            Player player = (Player) event.getEntity();

            if (!playerMap.containsKey(player.getUniqueId()))
                return;

            final Disguise disguise = playerMap.get(player.getUniqueId());

            if (disguise.isDisguised()) {
                disguise.cancel(player);
                Inventory inventory = player.getInventory();
                for (int i = 0; i < inventory.getSize(); ++i) {
                    if (isItem(inventory.getItem(i))) {
                        inventory.setItem(i, disguise.getIcon(player));
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (playerMap.containsKey(player.getUniqueId())) {
            final Disguise disguise = playerMap.get(player.getUniqueId());

            if (disguise.isDisguised()) {
                disguise.cancel(player);

                Inventory inventory = player.getInventory();
                for (int i = 0; i < inventory.getSize(); ++i) {
                    if (isItem(inventory.getItem(i))) {
                        inventory.setItem(i, disguise.getIcon(player));
                        break;
                    }
                }
            }
        }
    }

    public void transform(final Player player, final EntityType entityType) {
        try {
            final PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(player.getEntityId());
            final net.minecraft.server.v1_8_R3.Entity entity = this.getEntityLiving(entityType).getConstructor(World.class).newInstance(((CraftWorld) player.getWorld()).getHandle());
            entity.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
            entity.d(player.getEntityId());
            final EntityInsentient entityInsentient = (EntityInsentient) entity;
            entityInsentient.setCustomNameVisible(false);
            final PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving((EntityLiving) entity);
            for (final Player pls : Bukkit.getOnlinePlayers()) {

                if (pls.getEntityId() == player.getEntityId())
                    continue;

                ((CraftPlayer) pls).getHandle().playerConnection.sendPacket(destroy);
                ((CraftPlayer) pls).getHandle().playerConnection.sendPacket(spawn);
                pls.showPlayer(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeDisguise(final Disguise disguise, final Player player, final EntityType entityType) {
        this.transform(player, entityType);
        disguise.setCurrentEntityType(entityType);
        disguise.setDisguised(true);
        player.sendMessage(Account.fetch(player.getUniqueId()).getLanguage().translate("kit.chameleon.disguised", convertNameMob(entityType)));
    }

    public void removeDisguise(final Player player) {
        final PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(player.getEntityId());
        final PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle());
        for (final Player online : Bukkit.getOnlinePlayers()) {

            if (online.getEntityId() == player.getEntityId())
                continue;

            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(destroy);
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(spawn);
            online.showPlayer(player);
        }
    }

    public String convertNameMob(final EntityType entityType) {
        return StringUtils.replace(StringUtils.capitalize(entityType.name().toLowerCase()), "_", " ");
    }

    @Override
    public void removeItems(Player player) {
        super.removeItems(player);

        if (playerMap.containsKey(player.getUniqueId())) {
            Disguise disguise = playerMap.get(player.getUniqueId());

            if (!disguise.isDisguised())
                disguise.cancel(player);
        }
    }

    public Class<? extends Entity> getEntityLiving(EntityType entityType) {
        switch (entityType) {
            case BAT:
                return EntityBat.class;
            case BLAZE:
                return EntityBlaze.class;
            case CAVE_SPIDER:
                return EntityCaveSpider.class;
            case COW:
                return EntityCow.class;
            case CREEPER:
                return EntityCreeper.class;
            case ENDERMAN:
                return EntityEnderman.class;
            case IRON_GOLEM:
                return EntityIronGolem.class;
            case MUSHROOM_COW:
                return EntityMushroomCow.class;
            case OCELOT:
                return EntityOcelot.class;
            case PIG:
                return EntityPig.class;
            case PIG_ZOMBIE:
                return EntityPigZombie.class;
            case SHEEP:
                return EntitySheep.class;
            case SILVERFISH:
                return EntitySilverfish.class;
            case SKELETON:
                return EntitySkeleton.class;
            case SLIME:
                return EntitySlime.class;
            case SNOWMAN:
                return EntitySnowman.class;
            case SPIDER:
                return EntitySpider.class;
            case SQUID:
                return EntitySquid.class;
            case VILLAGER:
                return EntityVillager.class;
            case WITCH:
                return EntitySquid.class;
            case WOLF:
                return EntityWolf.class;
            case ZOMBIE:
                return EntityZombie.class;
            case CHICKEN:
                return EntityChicken.class;
            default:
                return null;
        }
    }

    public class Disguise implements Assistance {

        private final List<EntityType> entityList;
        private boolean disguised;
        private EntityType currentEntityType;
        private long delay;

        public Disguise() {
            this.entityList = new ArrayList<>();
        }

        public void cancel(final Player player) {
            this.disguised = false;
            this.currentEntityType = null;
            removeDisguise(player);
        }

        private ItemStack getIcon(Player player) {
            if (this.currentEntityType == null) {
                return addTag(Chameleon.this.getItems()[0], player.getUniqueId().toString());
            }
            return addTag(new ItemFactory(Material.MONSTER_EGG).setName("§aTransformado em " + convertNameMob(this.currentEntityType)).setDurability(this.getCurrentEntityType().getTypeId()).setDescription("§7Kit Chameleon").getStack(), "kit." + getName().toLowerCase(), player.getUniqueId().toString(), "undroppable");
        }

        public List<EntityType> getEntityList() {
            return this.entityList;
        }

        public boolean isDisguised() {
            return this.disguised;
        }

        public EntityType getCurrentEntityType() {
            return this.currentEntityType;
        }

        public long getDelay() {
            return this.delay;
        }

        public void setDisguised(final boolean disguised) {
            this.disguised = disguised;
        }

        public void setCurrentEntityType(final EntityType currentEntityType) {
            this.currentEntityType = currentEntityType;
        }

        public void setDelay(final long delay) {
            this.delay = delay;
        }
    }
}
