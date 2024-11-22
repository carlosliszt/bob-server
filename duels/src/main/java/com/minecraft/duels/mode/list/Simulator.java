package com.minecraft.duels.mode.list;

import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.duels.mode.Mode;
import com.minecraft.duels.room.Room;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Simulator extends Mode {

    public Simulator() {
        super(10, DuelType.SIMULATOR_1V1, DuelType.SIMULATOR_2V2);
        setWins(Columns.DUELS_SIMULATOR_WINS);
        setLoses(Columns.DUELS_SIMULATOR_LOSSES);
        setWinstreak(Columns.DUELS_SIMULATOR_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_SIMULATOR_MAX_WINSTREAK);
        setGames(Columns.DUELS_SIMULATOR_GAMES);
        setRating(Columns.DUELS_SIMULATOR_RATING);
    }

    @Override
    public void start(Room room) {
        super.start(room);

        room.getAlivePlayers().forEach(user -> {

            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            ItemStack HELMET = new ItemStack(Material.IRON_HELMET);
            ItemStack CHESTPLATE = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack LEGGINGS = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack BOOTS = new ItemStack(Material.IRON_BOOTS);

            ItemStack LAVA = new ItemStack(Material.LAVA_BUCKET);
            ItemStack SOUP = new ItemStack(Material.MUSHROOM_SOUP);

            playerInventory.setHelmet(HELMET);
            playerInventory.setChestplate(CHESTPLATE);
            playerInventory.setLeggings(LEGGINGS);
            playerInventory.setBoots(BOOTS);

            playerInventory.setItem(0, new ItemFactory(Material.IRON_SWORD).setUnbreakable().addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack());
            playerInventory.setItem(1, new ItemStack(Material.WATER_BUCKET));
            playerInventory.setItem(2, LAVA);
            playerInventory.setItem(3, new ItemStack(Material.WOOD, 64));

            playerInventory.setItem(7, new ItemStack(Material.WEB, 4));
            playerInventory.setItem(8, new ItemStack(Material.COBBLE_WALL, 64));

            playerInventory.setItem(9, HELMET);
            playerInventory.setItem(10, CHESTPLATE);
            playerInventory.setItem(11, LEGGINGS);
            playerInventory.setItem(12, BOOTS);

            playerInventory.setItem(13, new ItemStack(Material.BOWL, 64));
            playerInventory.setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
            playerInventory.setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));
            playerInventory.setItem(17, new ItemStack(Material.STONE_PICKAXE));

            playerInventory.setItem(26, new ItemStack(Material.STONE_AXE));
            playerInventory.setItem(35, LAVA);

            for (int i = 0; i < 20; i++)
                playerInventory.addItem(SOUP);
            player.updateInventory();
        });
    }


}