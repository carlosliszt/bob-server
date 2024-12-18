/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.game.handler.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.cooldown.CooldownFinishEvent;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.event.server.ServerPayloadSendEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.bossbar.Bossbar;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.scoreboard.GameScoreboard;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.server.packet.ServerPayload;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.list.Gladiator;
import com.minecraft.hungergames.user.kits.pattern.DailyKit;
import com.minecraft.hungergames.user.object.AwaySession;
import com.minecraft.hungergames.user.object.CombatTag;
import com.minecraft.hungergames.user.pattern.Condition;
import com.minecraft.hungergames.user.pattern.DieCause;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.hungergames.util.game.GameStage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;

import java.util.ArrayList;
import java.util.List;

@RecurringListener(register = GameStage.WAITING, unregister = GameStage.VICTORY)
public class RoomListener implements Listener, Assistance, BukkitInterface, VariableStorage {

    @Variable(name = "hg.combatlog_kill")
    public boolean combatlogKill = true;
    @Variable(name = "hg.late_join")
    public boolean lateJoin = true;

    public RoomListener() {
        loadVariables();
    }

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {
        if (getPlugin().getRankingFactory() != null) {

            final Account account = event.getAccount();
            final Ranking ranking = account.getRanking();
            final Ranking bronze = Ranking.BRONZE_I;

            if (account.hasCustomName())
                event.getTeam().setSuffix(" " + bronze.getColor() + bronze.getDisplay() + bronze.getSymbol());
            else
                event.getTeam().setSuffix(" " + ranking.getColor() + ranking.getDisplay() + ranking.getSymbol());

        }
        event.getTeam().setCanSeeFriendlyInvisibles(false);
    }

    @EventHandler
    public void onTournamentTimeEventAwayPlayers(ServerHeartbeatEvent event) {

        if (!event.isPeriodic(20))
            return;

        if (!hasStarted())
            return;

        if (getGame().getRecoveryMode().isEnabled())
            return;

        for (User user : getPlugin().getUserStorage().getAwayUsers()) {
            AwaySession awaySession = user.getAwaySession();

            if (user.isAlive()) {
                awaySession.decrease();

                if (user.isOnline()) {
                    awaySession.invalidate();
                    user.setAwaySession(null);
                } else if (awaySession.expired()) {
                    LivingUserDieEvent livingUserDieEvent = new LivingUserDieEvent(user, null, false, DieCause.TIMEOUT, user.getInventoryContents(), user.getPlayer().getLocation());
                    livingUserDieEvent.fire();
                    awaySession.invalidate();
                    user.setAwaySession(null);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (!hasStarted())
            return;

        if (event.hasItem()) {
            if (event.getItem().getType() == Material.COMPASS) {

                User user = getUser(event.getPlayer().getUniqueId());
                Player consumer = user.getPlayer();

                if (user.isAlive()) {
                    getGame().pointCompass(user, event.getAction());
                }
            } else if (event.getItem().getType() == Material.IRON_SWORD) {

                User user = getUser(event.getPlayer().getUniqueId());

                if (!user.isAlive()) {
                    if (event.getAction().toString().contains("RIGHT")) {
                        List<User> players = getPlugin().getUserStorage().getAliveUsers();

                        if (players.isEmpty())
                            return;

                        Player player = players.get(Constants.RANDOM.nextInt(players.size()) - 1).getPlayer();
                        event.getPlayer().teleport(player);
                        event.getPlayer().sendMessage(user.getAccount().getLanguage().translate("command.teleport.successful_teleport", user.getName(), player.getName()));
                    }
                }
            } else if (event.getItem().getType() == Material.DIAMOND_SWORD) {
                User user = getUser(event.getPlayer().getUniqueId());

                if (!user.isAlive()) {
                    if (event.getAction().toString().contains("RIGHT")) {
                        List<User> players = new ArrayList<>(getPlugin().getUserStorage().getAliveUsers());

                        players.removeIf(c -> !c.getCombatTag().isTagged());

                        if (players.isEmpty())
                            return;

                        Player player = players.get(Constants.RANDOM.nextInt(players.size()) - 1).getPlayer();

                        event.getPlayer().teleport(player);
                        event.getPlayer().sendMessage(user.getAccount().getLanguage().translate("command.teleport.successful_teleport", user.getName(), player.getName()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        User user = getUser(event.getPlayer().getUniqueId());
        user.setScoreboard(new GameScoreboard(event.getPlayer()));
        getGame().handleSidebar(user);
        user.loadKits();

        if (user.getAccount().getData(Columns.HG_DAILY_KITS).getAsJsonObject() != Columns.HG_DAILY_KITS.getDefaultValue()) {
            user.setDailyKit(Constants.GSON.fromJson(user.getAccount().getData(Columns.HG_DAILY_KITS).getAsJsonObject(), DailyKit.class));
        }

        Bukkit.getOnlinePlayers().forEach(this::refreshVisibility);

        Bossbar bossbar = user.getBossbar();

        bossbar.setMessage("§b§l" + getGame().getDisplay().toUpperCase() + " NO BOBMC.COM.BR");
        bossbar.setPercentage(1F);

        if (hasStarted()) {
            if (user.getCondition() == Condition.SPECTATOR) {
                if (user.getAccount().hasPermission(Rank.PARTNER_PLUS) && !user.isVanish())
                    Vanish.getInstance().setVanished(event.getPlayer(), user.getAccount().getRank());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(c -> c.hasMetadata("unbreakable") || c.getLocation().getY() > 128);
    }

    private final ImmutableSet<EntityType> ACCEPTABLES_TYPES = Sets.immutableEnumSet(EntityType.IRON_GOLEM, EntityType.SNOWMAN, EntityType.HORSE, EntityType.CHICKEN, EntityType.COW, EntityType.PIG, EntityType.ENDERMAN, EntityType.CREEPER, EntityType.SPIDER, EntityType.OCELOT, EntityType.SHEEP, EntityType.WOLF, EntityType.WITCH, EntityType.VILLAGER);
    private final ImmutableSet<CreatureSpawnEvent.SpawnReason> ACCEPTABLES_REASON = Sets.immutableEnumSet(CreatureSpawnEvent.SpawnReason.NATURAL, CreatureSpawnEvent.SpawnReason.SPAWNER, CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM, CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN);

    @org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        EntityType entityType = event.getEntityType();

        if (entityType == EntityType.DROPPED_ITEM)
            return;

        if (entityType == EntityType.GUARDIAN || entityType == EntityType.RABBIT)
            event.setCancelled(true);
        else if (ACCEPTABLES_REASON.contains(event.getSpawnReason())) {
            if (ACCEPTABLES_TYPES.contains(event.getEntityType())) {
                if (Constants.RANDOM.nextInt(4) <= 2)
                    event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        User user = getUser(event.getPlayer().getUniqueId());
        CombatTag combatTag = user.getCombatTag();
        if (user.isAlive()) {
            if (getStage() == GameStage.PLAYING && user.getCombatTag().isTagged() && combatlogKill) {
                new LivingUserDieEvent(user, combatTag.getLastHit(), true, DieCause.COMBAT, user.getInventoryContents(), user.getPlayer().getLocation()).fire();
            } else if (hasStarted()) {
                broadcast("hg.game.user.left_the_server", user.getName());
                AwaySession awaySession = new AwaySession(getGame().getVariables().getTimeout(), getGame().getRecoveryMode().isEnabled());
                user.setAwaySession(awaySession);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null)
            return;

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack cursor = event.getCurrentItem();
            if (hasKey(cursor, "kit")) {
                if (!event.getWhoClicked().getOpenInventory().getTopInventory().getType().equals(InventoryType.CRAFTING))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onServerPayloadSendEvent(ServerPayloadSendEvent event) {
        ServerPayload payload = event.getPayload();

        payload.overrideOnlineCount(getPlugin().getUserStorage().getAliveUsers().size());
        payload.write("time", getGame().getTime());
        payload.write("stage", getGame().getStage().name());
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.RED_MUSHROOM || event.getBlock().getType() == Material.BROWN_MUSHROOM)
            event.setCancelled(true);
    }

    @Variable(name = "hg.advantages.minimum_rank", permission = Rank.EVENT_MOD)
    public Rank rank = Rank.VIP;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        User user = User.fetch(event.getPlayer().getUniqueId());
        if (hasStarted()) {
            if (user.getAccount().hasPermission(rank)) {
                if (isLateLimit()) {
                    if (user.getCondition() == Condition.LOADING) {
                        if (user.getAccount().hasPermission(Rank.PARTNER_PLUS)) {
                            user.setCondition(Condition.SPECTATOR);
                            run(() -> {
                                Vanish.getInstance().setVanished(user.getPlayer(), user.getAccount().getRank());
                                getGame().die(user);
                                event.getPlayer().teleport(getGame().getVariables().getSpawnpoint());
                            }, 1L);
                        } else if (lateJoin) {
                            user.setCondition(Condition.ALIVE);
                            run(() -> {
                                getGame().die(user);
                                teleport(event.getPlayer());
                            }, 1L);
                        } else {
                            user.setCondition(Condition.SPECTATOR);
                            run(() -> {
                                getGame().die(user);
                                event.getPlayer().teleport(getGame().getVariables().getSpawnpoint());
                            }, 1L);
                        }
                    } else if (!user.isAlive()) {
                        user.setCondition(Condition.SPECTATOR);
                        run(() -> {
                            getGame().die(user);
                            event.getPlayer().teleport(getGame().getVariables().getSpawnpoint());
                        }, 1L);
                    }
                } else if (!user.isAlive()) {
                    user.setCondition(Condition.SPECTATOR);
                    run(() -> {
                        getGame().die(user);
                        event.getPlayer().teleport(getGame().getVariables().getSpawnpoint());
                    }, 1L);
                }
            } else if (!user.isAlive()) {
                event.setKickMessage(user.getAccount().getLanguage().translate("hg.game.already_started"));
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            }

            if (user.isAlive()) {

                if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL)
                    event.allow();

                broadcast("hg.game.user.joined_the_server", user.getAccount().getDisplayName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVanishEnable(PlayerVanishEnableEvent event) {
        if (!event.isCancelled()) {

            if (!hasStarted())
                return;

            User user = User.fetch(event.getAccount().getUniqueId());

            if (user.isAlive())
                new LivingUserDieEvent(user, null, false, DieCause.SURRENDER, user.getInventoryContents(), user.getPlayer().getLocation()).fire();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitAdminMode(PlayerVanishDisableEvent event) {
        if (!event.isCancelled()) {

            if (!hasStarted())
                return;

            User user = User.fetch(event.getAccount().getUniqueId());
            Account account = event.getAccount();

            if (account.hasPermission(Rank.ADMINISTRATOR)) {
                user.setCondition(Condition.ALIVE);
                getGame().prepare(user.getPlayer());
            } else if (isLateLimit()) {
                user.setCondition(Condition.ALIVE);
                getGame().die(user);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack() != null) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
            if (nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("undroppable") && nmsStack.getTag().hasKey(event.getPlayer().getUniqueId().toString()))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItemEvent(ItemSpawnEvent event) {

        if (event.getEntity().getItemStack().getType() == Material.SNOW_BALL) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity().getItemStack() != null) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getEntity().getItemStack());
            if (nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("undroppable") && !nmsStack.getTag().hasKey("kit"))
                event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (item != null) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            if (nmsStack != null && nmsStack.hasTag() && nmsStack.getTag().hasKey("kit")) {
                if (!nmsStack.getTag().hasKey(event.getPlayer().getUniqueId().toString()))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            if (hasKey(item, "kit")) {
                item.setDurability(item.getDurability());
                event.setDamage(0);
                event.setCancelled(true);
            }
        }
    }

    protected void teleport(Player player) {
        Location location;
        int range;
        if (getGame().getStage() == GameStage.INVINCIBILITY) {
            range = HungerGames.getInstance().getGame().getVariables().getSpawnRange();
            location = HungerGames.getInstance().getGame().getVariables().getSpawnpoint().clone();
        } else {
            range = HungerGames.getInstance().getGame().getVariables().getWorldSize() - 40;
            int x = randomize(range), z = randomize(range);
            location = new Location(getWorld(), x, getWorld().getHighestBlockYAt(x, z), z);
            range = 0;
        }
        player.teleport(location.add(randomize(range), 0, randomize(range)));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlaceOutOfRange(BlockPlaceEvent event) {
        Gladiator gladiator = (Gladiator) getKit("Gladiator");
        if (event.getBlock().getY() >= getGame().getVariables().getWorldHeight() && !gladiator.isGladiator(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCooldownExpireEvent(CooldownFinishEvent event) {
        Cooldown cooldown = event.getCooldown();

        if (cooldown.getKey().contains("kit.cooldown.")) {
            Kit kit = getPlugin().getKitStorage().getKit(cooldown.getKey().split("\\.")[2]);
            event.getPlayer().sendMessage(Account.fetch(event.getPlayer().getUniqueId()).getLanguage().translate("kit.cooldown_expire", kit.getDisplayName()));
        }
    }

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(Material.STONE, Material.SNOW_BLOCK, Material.COBBLESTONE, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.LOG, Material.LOG_2);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakDrop(BlockBreakEvent event) {

        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (player.getGameMode() != GameMode.SURVIVAL)
            return;

        if (ACCEPTABLE_MATERIALS.contains(block.getType())) {
            block.getDrops(event.getPlayer().getItemInHand()).forEach(item -> giveItem(player, item, block.getLocation()));
            event.setDropItems(false);
        } else if (block.getType() == Material.COCOA) {

            CocoaPlant cp = (CocoaPlant) event.getBlock().getState().getData();
            int count = cp.getSize() == CocoaPlant.CocoaPlantSize.LARGE ? 3 : cp.getSize() == CocoaPlant.CocoaPlantSize.MEDIUM ? 2 : 1;

            event.getBlock().setType(Material.AIR, false);

            for (int i = 0; i < count; i++)
                giveItem(player, new ItemStack(Material.INK_SACK, 1, (byte) 3), event.getBlock().getLocation());

            event.setDropItems(false);
        } else if (block.getType() == Material.CACTUS) {

            Block iterator = block;

            event.setDropItems(false);
            ItemStack itemStack = new ItemFactory(Material.CACTUS).getStack();

            iterator.setType(Material.AIR, false);
            giveItem(player, itemStack, block.getLocation());

            while (iterator.getRelative(BlockFace.UP).getType() == Material.CACTUS) {
                iterator = iterator.getRelative(BlockFace.UP);
                iterator.setType(Material.AIR, false);
                giveItem(player, itemStack, iterator.getLocation());
            }
        }
    }
}