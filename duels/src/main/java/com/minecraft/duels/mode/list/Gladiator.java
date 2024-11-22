package com.minecraft.duels.mode.list;

import com.minecraft.core.bukkit.server.duels.DuelType;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.duels.mode.Mode;
import com.minecraft.duels.room.Room;
import com.minecraft.duels.util.enums.RoomStage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Gladiator extends Mode implements VariableStorage {

    @Variable(name = "duels.gladiator.timer.clear_delay", permission = Rank.ADMINISTRATOR)
    public int clearDelay = 240;

    @Variable(name = "duels.gladiator.timer.wither_delay", permission = Rank.ADMINISTRATOR)
    public int witherDelay = 180;

    public Gladiator() {
        super(10, DuelType.GLADIATOR_1V1, DuelType.GLADIATOR_2V2);
        loadVariables();
        setWins(Columns.DUELS_GLADIATOR_WINS);
        setLoses(Columns.DUELS_GLADIATOR_LOSSES);
        setWinstreak(Columns.DUELS_GLADIATOR_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_GLADIATOR_MAX_WINSTREAK);
        setGames(Columns.DUELS_GLADIATOR_GAMES);
        setRating(Columns.DUELS_GLADIATOR_RATING);
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
            ItemStack BOWL = new ItemStack(Material.BOWL, 64);
            ItemStack COCOA = new ItemStack(Material.INK_SACK, 64, (short) 3);
            ItemStack SOUP = new ItemStack(Material.MUSHROOM_SOUP);

            playerInventory.setHelmet(HELMET);
            playerInventory.setChestplate(CHESTPLATE);
            playerInventory.setLeggings(LEGGINGS);
            playerInventory.setBoots(BOOTS);

            playerInventory.setItem(0, new ItemFactory(Material.DIAMOND_SWORD).setUnbreakable().addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack());
            playerInventory.setItem(1, new ItemStack(Material.WATER_BUCKET));
            playerInventory.setItem(2, LAVA);
            playerInventory.setItem(3, new ItemStack(Material.WOOD, 64));
            playerInventory.setItem(8, new ItemStack(Material.COBBLE_WALL, 64));

            playerInventory.setItem(9, HELMET);
            playerInventory.setItem(10, CHESTPLATE);
            playerInventory.setItem(11, LEGGINGS);
            playerInventory.setItem(12, BOOTS);
            playerInventory.setItem(13, BOWL);
            playerInventory.setItem(14, COCOA);
            playerInventory.setItem(15, COCOA);
            playerInventory.setItem(16, COCOA);
            playerInventory.setItem(17, new ItemStack(Material.STONE_AXE));

            playerInventory.setItem(18, HELMET);
            playerInventory.setItem(19, CHESTPLATE);
            playerInventory.setItem(20, LEGGINGS);
            playerInventory.setItem(21, BOOTS);
            playerInventory.setItem(22, BOWL);
            playerInventory.setItem(23, COCOA);
            playerInventory.setItem(24, COCOA);
            playerInventory.setItem(25, COCOA);
            playerInventory.setItem(26, new ItemStack(Material.STONE_PICKAXE));

            playerInventory.setItem(27, LAVA);
            playerInventory.setItem(28, LAVA);

            for (int i = 0; i < 12; i++)
                playerInventory.addItem(SOUP);

            player.updateInventory();
        });
    }

    @Override
    public void tick(Room room) {
        super.tick(room);

        if (room.getStage() == RoomStage.PLAYING) {

            int time = room.getTime();

            if (time % witherDelay == 0) {
                room.getAlivePlayers().forEach(c -> c.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 6), true));
            } else if (time % clearDelay == 0) {
                room.getRollback().forEach(block -> WorldEditAPI.getInstance().setBlock(block.getLocation(), Material.AIR, (byte) 0));
                room.getRollback().clear();
                room.getAlivePlayers().forEach(c -> c.getPlayer().removePotionEffect(PotionEffectType.WITHER));
            }
        }
    }
}