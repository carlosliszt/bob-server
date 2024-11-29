package com.minecraft.thebridge.listeners;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.cooldown.CooldownFinishEvent;
import com.minecraft.core.bukkit.event.player.PlayerShowEvent;
import com.minecraft.core.bukkit.event.player.PlayerTeamAssignEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.event.protocol.PacketReceiveEvent;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.event.server.ServerPayloadSendEvent;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.bukkit.server.thebridge.GameType;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.enums.Ranking;
import com.minecraft.thebridge.TheBridge;
import com.minecraft.thebridge.event.UserDiedEvent;
import com.minecraft.thebridge.event.UserScorePointEvent;
import com.minecraft.thebridge.game.Game;
import com.minecraft.thebridge.game.enums.GameStage;
import com.minecraft.thebridge.team.Team;
import com.minecraft.thebridge.user.User;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class RoomListeners implements Listener, BukkitInterface {

    private final TheBridge theBridge;
    private final CooldownProvider cooldownProvider;

    public RoomListeners(final TheBridge theBridge) {
        this.theBridge = theBridge;
        this.cooldownProvider = CooldownProvider.getGenericInstance();
    }

    @EventHandler
    public void onServerPayloadSendEvent(final ServerPayloadSendEvent event) {
        for (GameType gameType : GameType.getValues()) {
            int count = 0;

            for (Game game : theBridge.getGameStorage().getGames(gameType)) {
                count += game.getAliveUsers().size();
            }

            event.getPayload().write(gameType.name(), count);
        }
    }

    @EventHandler
    public void onProjectileLaunchEvent(final ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow))
            return;

        final Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player))
            return;

        cooldownProvider.addCooldown(((Player) arrow.getShooter()).getUniqueId(), "Flecha", arrow.getWorld().getUID().toString(), 3, true);
    }

    @EventHandler
    public void onFinishArrowCooldown(final CooldownFinishEvent event) {
        final Cooldown cooldown = event.getCooldown();
        final Player player = event.getPlayer();

        if (!cooldown.getKey().equals(player.getWorld().getUID().toString()))
            return;

        final PlayerInventory inventory = player.getInventory();

        if (inventory.contains(Material.ARROW))
            return;

        final ItemStack ARROW = new ItemStack(Material.ARROW);

        final ItemStack itemStack = inventory.getItem(8);

        if (itemStack == null || itemStack.getType() == Material.AIR)
            inventory.setItem(8, ARROW);
        else
            inventory.addItem(ARROW);

        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 3F, 1F);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            player.setHealth(player.getMaxHealth());
            player.removePotionEffect(PotionEffectType.ABSORPTION);
        }
    }

    @EventHandler
    public void playerPickUpEvent(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCallDeath(PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        final User user = User.fetch(player.getUniqueId());

        if (user == null)
            return;

        final Game game = user.getGame();

        if (game == null)
            return;

        if (!user.isPlaying())
            return;

        final Location location = player.getLocation();
        final Team team = user.getTeam();
        final boolean playing = game.getStage() == GameStage.PLAYING;

        if (game.getConfiguration().getBlueBlockPortals().contains(location.getBlock()) && playing) {
            if (team == game.getRed())
                new UserScorePointEvent(user, game, team, game.getBlue()).fire();
            else new UserDiedEvent(user, null, user.getGame()).fire();
        } else if (game.getConfiguration().getRedBlockPortals().contains(location.getBlock()) && playing) {
            if (team == game.getBlue())
                new UserScorePointEvent(user, game, team, game.getRed()).fire();
            else new UserDiedEvent(user, null, user.getGame()).fire();
        } else if (location.getY() <= game.getConfiguration().getMin_y()) {
            if (playing) {
                final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

                final UUID lastCombatUUID = user.getLastCombat();
                Player killer = null;

                if (lastCombatUUID != null && (killer = Bukkit.getPlayer(lastCombatUUID)) != null) {
                    entityPlayer.killer = ((CraftPlayer) killer).getHandle();
                } else {
                    entityPlayer.killer = null;
                }

                new UserDiedEvent(user, killer == null ? null : User.fetch(killer.getUniqueId()), user.getGame()).fire();
            } else player.teleport(game.getConfiguration().getSpawnPoint());
        }
    }



    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!event.isBothPlayers())
            return;

        final Game game = TheBridge.getInstance().getGameStorage().getGame(event.getEntity().getWorld());

        final User damaged = User.fetch(event.getEntity().getUniqueId());

        if (game == null || game.getStage() != GameStage.PLAYING) {
            event.setCancelled(true);
            return;
        }

        final User damager = User.fetch(event.getDamager().getUniqueId());

        if (!damaged.isPlaying() || !damager.isPlaying() || game.isSameTeam(damaged, damager)) {
            event.setCancelled(true);
        } else {
            damaged.addCombat(damager.getUniqueId());
            damager.addCombat(damaged.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityArrow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow))
            return;
        if (event.getEntity() instanceof Player) {
            final Arrow arrow = (Arrow) event.getDamager();

            if (!(arrow.getShooter() instanceof Player))
                return;

            final Game game = TheBridge.getInstance().getGameStorage().getGame(event.getEntity().getWorld());

            if (game == null)
                return;

            final User shooter = User.fetch(((Player) arrow.getShooter()).getUniqueId()), damaged = User.fetch(event.getEntity().getUniqueId());

            if (game.isSameTeam(shooter, damaged)) {
                event.setCancelled(true);
            } else {
                damaged.addCombat(shooter.getUniqueId());
                shooter.addCombat(damaged.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            User user = User.fetch(player.getUniqueId());

            Game game = user.getGame();

            if (game == null || event.getCause() == EntityDamageEvent.DamageCause.FALL || game.getStage() != GameStage.PLAYING || !user.isPlaying())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRefreshVanish(ServerHeartbeatEvent event) {
        if (!event.isPeriodic(36000))
            return;
        async(() -> TheBridge.getInstance().getRankingFactory().query());
    }

    @EventHandler
    public void onVanishEnable(PlayerVanishEnableEvent event) {

        Player player = Bukkit.getPlayer(event.getAccount().getUniqueId());
        User user = User.fetch(event.getAccount().getUniqueId());
        Game game = user.getGame();

        if (game != null) {
            if (user.isPlaying()) {
                run(() -> {
                    player.sendMessage("§aReentrando como espectador.");
                    game.join(user, PlayMode.VANISH, true);
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onVanishEnable(PlayerVanishDisableEvent event) {

        Player player = Bukkit.getPlayer(event.getAccount().getUniqueId());
        User user = User.fetch(event.getAccount().getUniqueId());
        Game game = user.getGame();

        if (game != null) {
            if (!user.isPlaying()) {
                run(() -> {
                    player.sendMessage("§aReentrando como jogador.");
                    game.join(user, PlayMode.PLAYER, true);
                }, 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());
        Game game = user.getGame();

        if (game == null || game.getStage() == GameStage.WAITING || game.getStage() == GameStage.STARTING) {
            player.sendMessage(user.getAccount().getLanguage().translate("duels.cant_pregame_chat"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof PacketPlayInTabComplete) {
            Player player = event.getPlayer();
            User user = User.fetch(player.getUniqueId());
            Game game = user.getGame();

            if (game == null || game.getStage() == GameStage.WAITING || game.getStage() == GameStage.STARTING)
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStopDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.isCancelled())
                return;

            final Player player = (Player) event.getEntity();
            final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            if (player.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true);
                player.setHealth(20.0D);

                final User user = User.fetch(player.getUniqueId());
                final UUID lastCombatUUID = user.getLastCombat();
                Player killer = null;

                if (lastCombatUUID != null && (killer = Bukkit.getPlayer(lastCombatUUID)) != null) {
                    entityPlayer.killer = ((CraftPlayer) killer).getHandle();
                } else {
                    entityPlayer.killer = null;
                }

                new UserDiedEvent(user, killer == null ? null : User.fetch(killer.getUniqueId()), user.getGame()).fire();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler
    public void onTeamAssign(PlayerTeamAssignEvent event) {

        final Account account = event.getAccount();
        final User user = User.fetch(account.getUniqueId());

        if (user.isPlaying()) {
            Game game = user.getGame();
            if (game == null || game.getStage() == GameStage.WAITING || game.getStage() == GameStage.STARTING)
                event.getTeam().setPrefix("§7§k");
            else {
                final Ranking ranking = account.getRanking();

                event.getTeam().setPrefix(user.getTeam().getChatColor().toString());
                event.getTeam().setSuffix(" " + ranking.getColor() + ranking.getDisplay() + ranking.getSymbol());
            }
        }
    }

    @EventHandler
    public void onArmor(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) event.setCancelled(true);
    }

    @EventHandler
    public void ArmorStand(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void ArmorStand(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() != null && event.getRightClicked() instanceof ArmorStand)
            event.setCancelled(true);
    }

    @EventHandler
    public void ArmorStand(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() != null && event.getRightClicked() instanceof ArmorStand)
            event.setCancelled(true);
    }

    @EventHandler
    public void onShow(PlayerShowEvent event) {
        if (!event.getReceiver().getWorld().getUID().equals(event.getTohide().getWorld().getUID()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onAwardAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

}