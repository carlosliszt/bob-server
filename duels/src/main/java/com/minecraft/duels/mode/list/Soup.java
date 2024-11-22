package com.minecraft.duels.mode.list;

import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.duels.mode.Mode;
import com.minecraft.duels.room.Room;
import com.minecraft.duels.user.User;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Soup extends Mode {

    public Soup() {
        super(10, DuelType.SOUP_1V1, DuelType.SOUP_2V2);
        setWins(Columns.DUELS_SOUP_WINS);
        setLoses(Columns.DUELS_SOUP_LOSSES);
        setWinstreak(Columns.DUELS_SOUP_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_SOUP_MAX_WINSTREAK);
        setGames(Columns.DUELS_SOUP_GAMES);
        setRating(Columns.DUELS_SOUP_RATING);
    }

    @Override
    public void start(Room room) {
        super.start(room);

        room.getAlivePlayers().forEach(user -> {

            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            ItemStack SOUP = new ItemStack(Material.MUSHROOM_SOUP);

            playerInventory.setItem(0, new ItemFactory(Material.DIAMOND_SWORD).setUnbreakable().addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack());

            for (int i = 1; i < 9; i++) {
                playerInventory.setItem(i, SOUP);
            }

            playerInventory.setHelmet(new ItemStack(Material.IRON_HELMET));
            playerInventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            playerInventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            playerInventory.setBoots(new ItemStack(Material.IRON_BOOTS));
        });
    }

    @Override
    public boolean isCanBuild() {
        return false;
    }

    @Override
    public boolean isAllowDrops() {
        return false;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.DIAMOND_SWORD) {
            User user = User.fetch(event.getPlayer().getUniqueId());
            if (user.getRoom().getMode() == this) {
                event.setCancelled(true);
            }
        }
    }
}