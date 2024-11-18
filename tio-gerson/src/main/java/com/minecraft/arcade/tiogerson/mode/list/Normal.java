package com.minecraft.arcade.tiogerson.mode.list;

import com.minecraft.arcade.tiogerson.mode.Mode;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Normal extends Mode {

    public Normal() {
        super(10);
    }

    ItemStack kbStick = new ItemFactory(Material.STICK).setName("§aSAII FORA!").setDescription("§7SAI TITIO!!!").addEnchantment(Enchantment.KNOCKBACK, 2).setUnbreakable().addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack();
    ItemStack compass = new ItemFactory(Material.COMPASS).setName("§aLocalizar").setDescription("§7Localize o §9ENZO§7 mais próximo").getStack();

    Cooldown sugarCooldown = new Cooldown("Pózinho Mágico", "magicSugar", 30, true);
    ItemStack magicSugar = new ItemFactory(Material.SUGAR).setAmount(3).setName("§aPózinho Magico").setDescription("§7Aumenta a velocidade por 10 segundos").getStack();

    @Override
    public void start(Room room) {
        super.start(room);

        room.getEnzo().getMembers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.clear();
            user.setPreviousTeam(room.getEnzo());

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);

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
            player.getInventory().setItem(1, compass);
            player.getInventory().setItem(2, magicSugar);

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

        if (item.isSimilar(compass)) {
            pointCompass(user, user.getRoom(), event.getAction());
        }

        if (item.isSimilar(magicSugar)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (CooldownProvider.getGenericInstance().hasCooldown(user.getUniqueId(), "magicSugar")) {
                    user.getPlayer().sendMessage(user.getAccount().getLanguage().translate("wait_generic", Constants.SIMPLE_DECIMAL_FORMAT.format(sugarCooldown.getRemaining())));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 5));
                    player.getInventory().remove(magicSugar);
                    CooldownProvider.getGenericInstance().addCooldown(player, sugarCooldown);
                }
            }
        }

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        User user = User.fetch(player.getUniqueId());
        User damagerUser = User.fetch(damager.getUniqueId());

        Room room = user.getRoom();

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
