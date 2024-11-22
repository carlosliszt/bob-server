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
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class UHC extends Mode {

    protected final ItemStack HEAD = new ItemFactory().setSkullURL("f9878853654c3bb26fd33e80f8ed3cdf03ab3247f73678464e06da143dbd0c17").setAmount(3).setName("§6Golden Head").getStack();

    public UHC() {
        super(10, DuelType.UHC_1V1, DuelType.UHC_2V2);
        setWins(Columns.DUELS_UHC_WINS);
        setLoses(Columns.DUELS_UHC_LOSSES);
        setWinstreak(Columns.DUELS_UHC_WINSTREAK);
        setWinstreakRecord(Columns.DUELS_UHC_MAX_WINSTREAK);
        setGames(Columns.DUELS_UHC_GAMES);
        setRating(Columns.DUELS_UHC_RATING);
    }

    @Override
    public void start(Room room) {
        super.start(room);

        room.getAlivePlayers().forEach(user -> {
            Player player = user.getPlayer();
            PlayerInventory playerInventory = player.getInventory();

            playerInventory.setHelmet(new ItemFactory(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getStack());
            playerInventory.setChestplate(new ItemFactory(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2).getStack());
            playerInventory.setLeggings(new ItemFactory(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getStack());
            playerInventory.setBoots(new ItemFactory(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getStack());

            ItemStack LAVA = new ItemStack(Material.LAVA_BUCKET);
            ItemStack WATER = new ItemStack(Material.WATER_BUCKET);
            ItemStack WOOD = new ItemStack(Material.WOOD, 64);

            playerInventory.setItem(0, new ItemFactory(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 3).getStack());
            playerInventory.setItem(1, new ItemStack(Material.FISHING_ROD));
            playerInventory.setItem(2, new ItemFactory(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE, 2).getStack());
            playerInventory.setItem(3, new ItemStack(Material.DIAMOND_AXE));
            playerInventory.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 6));
            playerInventory.setItem(5, HEAD);
            playerInventory.setItem(6, WATER);
            playerInventory.setItem(7, LAVA);
            playerInventory.setItem(8, WOOD);

            playerInventory.setItem(9, new ItemStack(Material.ARROW, 16));

            playerInventory.setItem(33, WATER);
            playerInventory.setItem(34, LAVA);
            playerInventory.setItem(35, WOOD);

            player.updateInventory();
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        User user = User.fetch(player.getUniqueId());

        Room room = user.getRoom();

        if (room == null)
            return;

        if (room.getMode() != this)
            return;

        if (event.getBlockPlaced().getLocation().getY() > 17)
            event.setCancelled(true);
    }

}