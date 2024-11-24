package com.minecraft.arcade.pvp.listeners;

import com.minecraft.arcade.pvp.event.user.LivingUserInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener {

    @EventHandler
    public void onInteractSign(final LivingUserInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getBlockClicked() == null) return;

        if (event.getBlockClicked().getType() == Material.WALL_SIGN || event.getBlockClicked().getType() == Material.SIGN_POST) {
            final Sign sign = (Sign) event.getBlockClicked().getState();

            switch (sign.getLine(2)) {

                case "§0§lSOUP": {
                    Inventory inventory = Bukkit.createInventory(null, 27, "Soup");

                    ItemStack itemStack = new ItemStack(Material.MUSHROOM_SOUP);

                    for (int i = 0; i < 27; i++)
                        inventory.setItem(i, itemStack);

                    event.getUser().getPlayer().openInventory(inventory);
                }

                case "§0§lRECRAFT": {
                    Inventory inventory = Bukkit.createInventory(null, 27, "Recraft");

                    if (event.getUser().getGame().getId() == 3) {
                        ItemStack itemStack1 = new ItemStack(Material.RED_MUSHROOM, 64);
                        ItemStack itemStack2 = new ItemStack(Material.BROWN_MUSHROOM, 64);
                        ItemStack itemStack3 = new ItemStack(Material.BOWL, 64);

                        for (int i = 0; i < 9; i++) {
                            inventory.setItem(i, itemStack1);
                        }

                        for (int i = 9; i < 18; i++) {
                            inventory.setItem(i, itemStack2);
                        }

                        for (int i = 18; i < 27; i++) {
                            inventory.setItem(i, itemStack3);
                        }
                    } else {
                        ItemStack RED_MUSHROOM = new ItemStack(Material.RED_MUSHROOM, 64);
                        ItemStack BROWN_MUSHROOM = new ItemStack(Material.BROWN_MUSHROOM, 64);
                        ItemStack BOWL = new ItemStack(Material.BOWL, 64);
                        ItemStack INK_SACK = new ItemStack(Material.INK_SACK, 64, (short) 3);

                        inventory.setItem(1, BOWL);
                        inventory.setItem(2, BROWN_MUSHROOM);
                        inventory.setItem(3, RED_MUSHROOM);

                        inventory.setItem(5, BOWL);
                        inventory.setItem(6, INK_SACK);
                        inventory.setItem(7, INK_SACK);

                        inventory.setItem(10, BOWL);
                        inventory.setItem(11, BROWN_MUSHROOM);
                        inventory.setItem(12, RED_MUSHROOM);

                        inventory.setItem(14, BOWL);
                        inventory.setItem(15, INK_SACK);
                        inventory.setItem(16, INK_SACK);

                        inventory.setItem(19, BOWL);
                        inventory.setItem(20, BROWN_MUSHROOM);
                        inventory.setItem(21, RED_MUSHROOM);

                        inventory.setItem(23, BOWL);
                        inventory.setItem(24, INK_SACK);
                        inventory.setItem(25, INK_SACK);
                    }

                    event.getUser().getPlayer().openInventory(inventory);
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(final SignChangeEvent event) {
        final String line = event.getLine(0);

        if (line.equalsIgnoreCase("soup")) {
            event.setLine(0, "§c-§6-§e-§a-§b-");
            event.setLine(1, "§b§lBLAZE");
            event.setLine(2, "§0§lSOUP");
            event.setLine(3, "§c-§6-§e-§a-§b-");
        } else if (line.equalsIgnoreCase("recraft")) {
            event.setLine(0, "§c-§6-§e-§a-§b-");
            event.setLine(1, "§b§lBLAZE");
            event.setLine(2, "§0§lRECRAFT");
            event.setLine(3, "§c-§6-§e-§a-§b-");
        }
    }

}