package com.minecraft.arcade.tiogerson.mode.list;

import com.minecraft.arcade.tiogerson.mode.Mode;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Normal extends Mode {

    public Normal() {
        super(10);
    }

    @Override
    public void start(Room room) {
        super.start(room);

        room.getEnzo().getMembers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.clear();

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);

            player.updateInventory();

        });

        room.getTioGerson().getMembers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.clear();

            player.getInventory().setItem(0, new ItemFactory(Material.DIAMOND_SWORD).setName("§aPau").setDescription("§7PAU NELES!!!").addEnchantment(Enchantment.DAMAGE_ALL, 5).setUnbreakable().addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack());

            player.updateInventory();
        });
    }


}
