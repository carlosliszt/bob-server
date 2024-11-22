package com.minecraft.arcade.tiogerson.mode.list;

import com.minecraft.arcade.tiogerson.mode.Mode;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.database.enums.Columns;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Normal extends Mode {

    public Normal() {
        super(10);
        setGames(Columns.TIOGERSON_GAMES);
        setLoses(Columns.TIOGERSON_LOSSES);
        setWins(Columns.TIOGERSON_WINS);
        setWinstreak(Columns.TIOGERSON_WINSTREAK);
        setWinstreakRecord(Columns.TIOGERSON_MAX_WINSTREAK);
        setMaxPlayers(8);
        setMaxTioGerson(1);
        setMinTioGerson(1);
    }

    ItemStack kbStick = new ItemFactory(Material.STICK).setName("§aSAII FORA!").setDescription("§7SAI TITIO!!!").addEnchantment(Enchantment.KNOCKBACK, 4).setUnbreakable().addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack();
    ItemStack compass = new ItemFactory(Material.COMPASS).setName("§aLocalizar").setDescription("§7Localize o §9ENZO§7 mais próximo").getStack();

    ItemStack magicSugar = new ItemFactory(Material.SUGAR).setAmount(3).setName("§aPózinho Magico").setDescription("§7Aumenta a velocidade por 10 segundos").getStack();

    @Override
    public void start(Room room) {
        super.start(room);

        room.getEnzo().getMembers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.clear();
            user.setPreviousTeam(room.getEnzo());

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);

            player.getInventory().setItem(0, kbStick);
            player.updateInventory();

        });

        room.getTioGerson().getMembers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            user.setPreviousTeam(room.getTioGerson());

            playerInventory.clear();

            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 5), true);
            player.getInventory().setItem(0, new ItemFactory(Material.STICK).setName("§aPau").setDescription("§7PAU NELES!!!").addEnchantment(Enchantment.DAMAGE_ALL, 5).setUnbreakable().addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());
            player.getInventory().setItem(4, magicSugar);
            player.getInventory().setItem(8, compass);

            player.updateInventory();
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        User user = User.fetch(event.getPlayer().getUniqueId());

        if (item == null || item.getType() == Material.AIR)
            return;

        if (item.getType() == Material.COMPASS) {
            pointCompass(user);
        }

        if (item.isSimilar(magicSugar)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (CooldownProvider.getGenericInstance().hasCooldown(user.getUniqueId(), "magicSugar")) {
                    user.getPlayer().sendMessage(user.getAccount().getLanguage().translate("wait_generic", Constants.SIMPLE_DECIMAL_FORMAT.format(CooldownProvider.getGenericInstance().getCooldown(player.getUniqueId(), "magicSugar").getRemaining())));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                    if (player.getItemInHand().getAmount() > 1) {
                        player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                    } else {
                        player.setItemInHand(new ItemStack(Material.AIR));
                    }
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 3F, 1F);
                    CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "Pózinho de Pirlimpimpim", "magicSugar", 20, true);
                }
            }
        }

    }

    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent e) {
        Player comparator = null;
        Player consumer = e.getPlayer();
        User consumerUser = User.fetch(consumer.getUniqueId());

        if (consumerUser.getRoom().getTioGerson().getMembers().contains(consumerUser)) {
            for (User other : consumerUser.getRoom().getTioGerson().getMembers()) {
                if (other.getUniqueId().equals(consumerUser.getUniqueId())) {
                    continue;
                }

                Player p = other.getPlayer();
                if (p != null && p.getLocation().distanceSquared(consumer.getLocation()) >= 100) {
                    if (comparator == null ||
                            comparator.getLocation().distanceSquared(consumer.getLocation()) > p.getLocation().distanceSquared(consumer.getLocation())) {
                        comparator = p;
                    }
                }
            }

            if (comparator != null && comparator.isOnline()) {
                consumer.playSound(comparator.getLocation(), Sound.NOTE_PLING, 2F, 2F);
            }
        }
    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isBothPlayers()) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            User user = User.fetch(player.getUniqueId());
            User damagerUser = User.fetch(damager.getUniqueId());

            Room room = user.getRoom();

            if (room == null || room.getStage() != RoomStage.PLAYING || !user.isPlaying() && !Vanish.getInstance().isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            } else {

                if (room.getTioGerson().getMembers().contains(user) && !damager.getItemInHand().isSimilar(kbStick)) {
                    event.setCancelled(true);
                } else {
                    event.setCancelled(false);
                    damager.getInventory().remove(kbStick);
                }

                if (room.getEnzo().getMembers().contains(user) && room.getEnzo().getMembers().contains(damagerUser))
                    event.setCancelled(true);
            }

        }

    }


}
