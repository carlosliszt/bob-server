package com.minecraft.lobby.accessory.title;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.title.TitleAccessory;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Medal;
import com.minecraft.lobby.accessory.AccessoryInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TitleSelectionInventory implements Listener, BukkitInterface {

    protected Player player;
    protected Account account;
    protected Selector inventory;
    protected TitleAccessory.TitleCategory category;
    protected InteractableItem returnItem;

    public TitleSelectionInventory(Player player, Account account, TitleAccessory.TitleCategory category) {
        this.player = player;
        this.account = account;
        this.category = category;
        this.inventory = build();
        this.returnItem = null;
    }

    public Selector build() {
        String value = Account.fetch(player.getUniqueId()).getData(Columns.TITLE).getAsString();
        Selector.Builder response = Selector.builder().withSize(45).withName("§8Selecione um título");
        List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
        int slotIndex = 0;

        this.returnItem = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("\n§eClique para voltar").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.closeInventory();
                new AccessoryInventory(player, account, AccessoryType.TITLES).openInventory();
                return true;
            }
        });

        for(TitleAccessory title : TitleAccessory.getByCategory(this.category)) {

            ItemStack itemStack;
            String display = "";

            if(title.getName().equalsIgnoreCase("clan")) {
                display = "§eMembro de " + (account.getClan() == null ? "§cNenhum" : ChatColor.valueOf(account.getClan().getColor()) + "[" + account.getClan().getTag() + "]");
                itemStack = new ItemFactory(title.getItemStack().getType()).
                        setName("§a" + title.getName()).
                        setDescription("\n§7Prévia:\n\n§eMembro de " + display+ "\n\n§eClique para selecionar.").
                        getStack();
            } else if(title.getName().equalsIgnoreCase("desde")) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(account.getData(Columns.FIRST_LOGIN).getAsLong());
                int year = c.get(Calendar.YEAR);

                display = "§eDesde §b" + year;

                itemStack = new ItemFactory(title.getItemStack().getType()).
                        setName("§a" + title.getName()).
                        setDescription("\n§7Prévia:\n\n§eDesde §b" + year + "\n\n§eClique para selecionar.").
                        getStack();
            } else if(title.getName().contains("Medal")) {
                Medal selected = account.getProperty("account_medal").getAs(Medal.class);
                display = title.getDisplay().replace("{0}", "" + selected.getColor() + selected.getIcon());
                itemStack = new ItemFactory(title.getItemStack().getType()).
                        setName("§a" + title.getName()).
                        setDescription("\n§7Prévia:\n\n§e" + title.getDescription().replace("{0}",  selected.getColor() + selected.getIcon())+ "\n\n§eClique para selecionar.").
                        getStack();
            } else {
                display = title.getDisplay().replace("{0}", "" + account.getData(title.getData()).getAsInteger());
                itemStack = new ItemFactory(title.getItemStack().getType()).
                        setName("§a" + title.getName()).
                        setDescription("\n§7Prévia:\n\n§e" + title.getDescription().replace("{0}", "" + account.getData(title.getData()).getAsInteger())+ "\n\n§eClique para selecionar.").
                        getStack();
            }

            String finalDisplay = display;
            InteractableItem titleItem = new InteractableItem(itemStack, new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    if (!title.hasActiveAccessory(player.getUniqueId())) {
                        title.remove(player);
                    }

                    if (value.equals(title.getName().toLowerCase())) {
                        player.closeInventory();
                        title.remove(player);
                        sync(() -> account.getDataStorage().saveColumn(Columns.TITLE));
                        Account.fetch(player.getUniqueId()).getData(Columns.TITLE).setData("...");
                        player.sendMessage("§cVocê removeu o acessório §e'" + title.getName().replace("_", " ") + "'§c §8(" + title.getType().getDisplayName() + ")§c.");
                    } else {
                        Account.fetch(player.getUniqueId()).getData(Columns.TITLE).setData(title.getName().toLowerCase());
                        title.setPlayerAccessory(player.getUniqueId(), title);
                        sync(() -> account.getDataStorage().saveColumn(Columns.TITLE));

                        title.setDisplay(finalDisplay);

                        if(!Account.fetch(player.getUniqueId()).getData(Columns.TITLE).getAsString().equals("...")) {
                            title.remove(player);
                            title.give(player);
                        } else {
                            title.give(player);
                        }

                        player.closeInventory();
                        player.sendMessage("§aVocê selecionou o acessório §e'" + title.getName().replace("_", " ") + "'§a §8(" + title.getType().getDisplayName() + ")§a.");
                    }
                    return true;
                }
            });

            if (slotIndex < allowedSlots.size()) {
                response.withCustomItem(allowedSlots.get(slotIndex), titleItem.getItemStack());
                slotIndex++;
            }
        }

        response.withCustomItem(36, returnItem.getItemStack());
        return response.build();

    }

    public void openInventory() {
        inventory.open(player);
        Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
    }

}
