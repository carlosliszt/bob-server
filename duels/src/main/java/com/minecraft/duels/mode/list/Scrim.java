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

public class Scrim extends Mode {

    public Scrim() {
        super(10, DuelType.SCRIM_1V1, DuelType.SCRIM_2V2);
        setWins(Columns.DUELS_SCRIM_WINS);
        setLoses(Columns.DUELS_SCRIM_LOSSES);
        setWinstreak(Columns.DUELS_SCRIM_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_SCRIM_MAX_WINSTREAK);
        setGames(Columns.DUELS_SCRIM_GAMES);
        setRating(Columns.DUELS_SCRIM_RATING);
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

            ItemStack BOWL = new ItemStack(Material.BOWL, 64);
            ItemStack COCOA = new ItemStack(Material.INK_SACK, 64, (short) 3);
            ItemStack SOUP = new ItemStack(Material.MUSHROOM_SOUP);

            playerInventory.setHelmet(HELMET);
            playerInventory.setChestplate(CHESTPLATE);
            playerInventory.setLeggings(LEGGINGS);
            playerInventory.setBoots(BOOTS);

            playerInventory.setItem(0, new ItemFactory(Material.DIAMOND_SWORD).setUnbreakable().addEnchantment(Enchantment.DAMAGE_ALL, 3).getStack());

            playerInventory.setItem(1, CHESTPLATE);
            playerInventory.setItem(8, LEGGINGS);

            playerInventory.setItem(9, HELMET);
            playerInventory.setItem(10, CHESTPLATE);
            playerInventory.setItem(11, LEGGINGS);
            playerInventory.setItem(12, BOOTS);

            playerInventory.setItem(13, BOWL);
            playerInventory.setItem(14, COCOA);
            playerInventory.setItem(15, COCOA);
            playerInventory.setItem(16, COCOA);
            playerInventory.setItem(17, COCOA);

            playerInventory.setItem(18, HELMET);
            playerInventory.setItem(19, CHESTPLATE);
            playerInventory.setItem(20, LEGGINGS);
            playerInventory.setItem(21, BOOTS);

            playerInventory.setItem(22, BOWL);
            playerInventory.setItem(23, COCOA);
            playerInventory.setItem(24, COCOA);
            playerInventory.setItem(25, COCOA);
            playerInventory.setItem(26, COCOA);

            playerInventory.setItem(27, HELMET);
            playerInventory.setItem(28, CHESTPLATE);
            playerInventory.setItem(29, LEGGINGS);
            playerInventory.setItem(30, BOOTS);

            playerInventory.setItem(31, CHESTPLATE);
            playerInventory.setItem(32, LEGGINGS);

            playerInventory.setItem(33, new ItemStack(Material.IRON_INGOT, 30));

            playerInventory.setItem(34, BOWL);
            playerInventory.setItem(35, COCOA);

            for (int i = 0; i < 7; i++)
                playerInventory.addItem(SOUP);

            player.updateInventory();

            sync(() -> player.setFireTicks(0));
        });
    }

    @Override
    public boolean isCanBuild() {
        return false;
    }
}
