package com.minecraft.lobby.duel.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.data.DataStorage;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.lobby.duel.inventory.enums.FactoryInventory;
import com.minecraft.lobby.duel.inventory.enums.FactorySettings;
import com.minecraft.lobby.duel.inventory.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryEditorCollection implements Listener, BukkitInterface {

    private static final List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16);
    protected boolean lock = false;

    protected Player player;
    protected Account account;
    protected Selector inventory;
    protected FactoryInventory type;

    public InventoryEditorCollection(Player player, Account account, FactoryInventory type) {
        this.player = player;
        this.account = account;
        this.type = type;
        this.inventory = build();
    }

    private void update() {
        HandlerList.unregisterAll(this);
        this.inventory = null;
        new InventoryEditorCollection(player, account, type).openInventory();
    }

    private Selector build() {
        Data data = null;
        Selector.Builder response = Selector.builder().withSize(type.getSlots()).withName(type.getTitle());
        List<ItemStack> items = new ArrayList<>();
        DataStorage dataStorage = account.getDataStorage();
        String jsonString = null;

        InteractableItem RESET = null;
        InteractableItem SAVE = null;

        InteractableItem RETURN = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("\n§eClique para voltar").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.closeInventory();
                new DuelsSettingsInventory(player, account, FactorySettings.MAIN).openInventory();
                return true;
            }
        });

        switch (type) {
            case MAIN:
                InteractableItem gladiator = new InteractableItem(new ItemFactory().setType(Material.IRON_FENCE).setName("§aGladiator").setDescription("\n§eClique para editar").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new InventoryEditorCollection(player, account, FactoryInventory.GLADIATOR).openInventory();
                        return true;
                    }
                });
                InteractableItem simulator = new InteractableItem(new ItemFactory().setType(Material.WEB).setName("§aSimulator").setDescription("\n§eClique para editar").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new InventoryEditorCollection(player, account, FactoryInventory.SIMULATOR).openInventory();
                        return true;
                    }
                });

                InteractableItem soup = new InteractableItem(new ItemFactory().setType(Material.MUSHROOM_SOUP).setName("§aSopa").setDescription("\n§eClique para editar").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new InventoryEditorCollection(player, account, FactoryInventory.SOUP).openInventory();
                        return true;
                    }
                });


                InteractableItem BACK = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("\n§eClique para voltar").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.closeInventory();
                        new DuelsSettingsInventory(player, account, FactorySettings.MAIN).openInventory();
                        return true;
                    }
                });
                response.withCustomItem(10, gladiator.getItemStack());
                response.withCustomItem(11, simulator.getItemStack());
                response.withCustomItem(12, soup.getItemStack());
                response.withCustomItem(31, BACK.getItemStack());
                break;
            case SIMULATOR:
                response.withSize(54);
                sync(() -> account.getDataStorage().loadIfUnloaded(Columns.DUELS_SIMULATOR_INVENTORY));

                data = dataStorage.getData(Columns.DUELS_SIMULATOR_INVENTORY);

                for (int i = 27; i < 36; i++) {
                    response.withCustomItem(i, new ItemFactory(Material.STAINED_GLASS_PANE).setName("§7 ").setDescription("§7⬆ Inventário", "§7⬇ Hotbar").getStack());
                }

                jsonString = data.getAsString();
                if (!jsonString.equals("...")) {
                    JsonArray jsonArray = Constants.GSON.fromJson(jsonString, JsonArray.class);;
                    for (var element : jsonArray) {
                        JsonObject itemJson = element.getAsJsonObject();
                        int slot = itemJson.get("slot").getAsInt();
                        String type = itemJson.get("type").getAsString();
                        int amount = itemJson.get("amount").getAsInt();

                        Material material;
                        try {
                            material = Material.valueOf(type);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        ItemStack item = new ItemStack(material, amount);
                        if (material.equals(Material.IRON_SWORD)) {
                            item = new ItemFactory(material).addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack();
                        }

                        if (slot >= 9) {
                            response.withCustomItem(slot - 9, item);
                        } else {
                            response.withCustomItem(slot + 36, item);
                        }
                    }
                } else {
                    for (SimulatorAttach attach : SimulatorAttach.values()) {
                        ItemStack stack = new ItemFactory(attach.getMaterial()).setAmount(attach.getQuantity()).getStack();

                        if (attach.getEnchantment() != null) {
                            stack = new ItemFactory(attach.getMaterial()).addEnchantment(attach.getEnchantment(), 1).setUnbreakable().setAmount(attach.getQuantity()).getStack();
                        }

                        for (int allocation : attach.getAllocations()) {
                            if (allocation >= 9) {
                                response.withCustomItem(allocation - 9, stack);
                            } else {
                                response.withCustomItem(allocation + 36, stack);
                            }
                        }
                    }
                }

                Data finalData1 = data;
                RESET = new InteractableItem(new ItemFactory(Material.getMaterial(159)).setDurability(14)
                        .setName("§cRedefinir inventário")
                        .setDescription("§7Clique para redefinir seu §finventário").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.sendMessage("§aRedefinindo...");
                        finalData1.setData("...");
                        async(() -> account.getDataStorage().saveColumn(finalData1.getColumn()));
                        player.sendMessage("§aVocê redefiniu o inventário do modo Simulator com sucesso.");

                        return true;
                    }
                });
                response.withCustomItem(48, RESET.getItemStack());

                SAVE = new InteractableItem(new ItemFactory(Material.getMaterial(159)).setDurability(5)
                        .setName("§aSalvar inventário")
                        .setDescription("§7Clique para salvar seu §finventário").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.sendMessage("§aSalvando...");
                        player.sendMessage("§aVocê salvou o inventário do modo Simulator com sucesso.");
                        return true;
                    }
                });
                response.withCustomItem(50, SAVE.getItemStack());
                response.withCustomItem(49, RETURN.getItemStack());
                break;
            case GLADIATOR:
                sync(() -> account.getDataStorage().loadIfUnloaded(Columns.DUELS_GLADIATOR_INVENTORY));
                data = account.getData(Columns.DUELS_GLADIATOR_INVENTORY);
                response.withSize(54);

                for (int i = 27; i < 36; i++) {
                    response.withCustomItem(i, new ItemFactory(Material.STAINED_GLASS_PANE).setName("§7 ").setDescription("§7⬆ Inventário", "§7⬇ Hotbar").getStack());
                }

                jsonString = data.getAsString();
                if (!jsonString.equals("...")) {
                    JsonArray jsonArray = Constants.GSON.fromJson(jsonString, JsonArray.class);;
                    for (var element : jsonArray) {
                        JsonObject itemJson = element.getAsJsonObject();
                        int slot = itemJson.get("slot").getAsInt();
                        String type = itemJson.get("type").getAsString();
                        int amount = itemJson.get("amount").getAsInt();

                        Material material;
                        try {
                            material = Material.valueOf(type);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        ItemStack item = new ItemStack(material, amount);
                        if (material.equals(Material.DIAMOND_SWORD)) {
                            item = new ItemFactory(material).addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack();
                        }

                        if (material.equals(Material.INK_SACK)) {
                            item = new ItemStack(material, amount, (short) 3);
                        }

                        if (slot >= 9) {
                            response.withCustomItem(slot - 9, item);
                        } else {
                            response.withCustomItem(slot + 36, item);
                        }
                    }
                } else {
                    for (GladiatorAttach attach : GladiatorAttach.values()) {
                        ItemStack stack = new ItemFactory(attach.getMaterial()).setAmount(attach.getQuantity()).getStack();

                        if (attach.getEnchantment() != null) {
                            stack = new ItemFactory(attach.getMaterial()).addEnchantment(attach.getEnchantment(), 1).setUnbreakable().setAmount(attach.getQuantity()).getStack();
                        }

                        if (attach.getMaterial() == Material.INK_SACK) {
                            stack = new ItemStack(attach.getMaterial(), attach.getQuantity(), (short) 3);
                        }

                        for (int allocation : attach.getAllocations()) {
                            if (allocation >= 9) {
                                response.withCustomItem(allocation - 9, stack);
                            } else {
                                response.withCustomItem(allocation + 36, stack);
                            }
                        }
                    }
                }

                Data finalData = data;
                RESET = new InteractableItem(new ItemFactory(Material.getMaterial(159)).setDurability(14)
                        .setName("§cRedefinir inventário")
                        .setDescription("§7Clique para redefinir seu §finventário").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.sendMessage("§aRedefinindo...");
                        finalData.setData("...");
                        async(() -> account.getDataStorage().saveColumn(finalData.getColumn()));
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5f, 1.5f);
                        player.sendMessage("§aVocê redefiniu o inventário do modo Gladiator com sucesso.");

                        return true;
                    }
                });
                response.withCustomItem(48, RESET.getItemStack());

                SAVE = new InteractableItem(new ItemFactory(Material.getMaterial(159)).setDurability(5)
                        .setName("§aSalvar inventário")
                        .setDescription("§7Clique para salvar seu §finventário").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.sendMessage("§aSalvando...");
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5f, 1.5f);
                        player.sendMessage("§aVocê salvou o inventário do modo Gladiator com sucesso.");
                        return true;
                    }
                });
                response.withCustomItem(50, SAVE.getItemStack());
                response.withCustomItem(49, RETURN.getItemStack());
                break;
            case SOUP:
                sync(() -> account.getDataStorage().loadIfUnloaded(Columns.DUELS_SOUP_INVENTORY));
                data = account.getData(Columns.DUELS_SOUP_INVENTORY);
                response.withSize(54);

                for (int i = 27; i < 36; i++) {
                    response.withCustomItem(i, new ItemFactory(Material.STAINED_GLASS_PANE).setName("§7 ").setDescription("§7⬆ Inventário", "§7⬇ Hotbar").getStack());
                }

                jsonString = data.getAsString();
                if (!jsonString.equals("...")) {
                    JsonArray jsonArray = Constants.GSON.fromJson(jsonString, JsonArray.class);;
                    for (var element : jsonArray) {
                        JsonObject itemJson = element.getAsJsonObject();
                        int slot = itemJson.get("slot").getAsInt();
                        String type = itemJson.get("type").getAsString();
                        int amount = itemJson.get("amount").getAsInt();

                        Material material;
                        try {
                            material = Material.valueOf(type);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        ItemStack item = new ItemStack(material, amount);
                        if (material.equals(Material.DIAMOND_SWORD)) {
                            item = new ItemFactory(material).addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack();
                        }

                        if (material.equals(Material.INK_SACK)) {
                            item = new ItemStack(material, amount, (short) 3);
                        }

                        if (slot >= 9) {
                            response.withCustomItem(slot - 9, item);
                        } else {
                            response.withCustomItem(slot + 36, item);
                        }
                    }
                } else {
                    for (SoupAttach attach : SoupAttach.values()) {
                        ItemStack stack = new ItemFactory(attach.getMaterial()).setAmount(attach.getQuantity()).getStack();

                        if (attach.getEnchantment() != null) {
                            stack = new ItemFactory(attach.getMaterial()).addEnchantment(attach.getEnchantment(), 1).setUnbreakable().setAmount(attach.getQuantity()).getStack();
                        }

                        if (attach.getMaterial() == Material.INK_SACK) {
                            stack = new ItemStack(attach.getMaterial(), attach.getQuantity(), (short) 3);
                        }

                        for (int allocation : attach.getAllocations()) {
                            if (allocation >= 9) {
                                response.withCustomItem(allocation - 9, stack);
                            } else {
                                response.withCustomItem(allocation + 36, stack);
                            }
                        }
                    }
                }

                Data finalData3 = data;
                RESET = new InteractableItem(new ItemFactory(Material.getMaterial(159)).setDurability(14)
                        .setName("§cRedefinir inventário")
                        .setDescription("§7Clique para redefinir seu §finventário").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.sendMessage("§aRedefinindo...");
                        finalData3.setData("...");
                        async(() -> account.getDataStorage().saveColumn(finalData3.getColumn()));
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5f, 1.5f);
                        player.sendMessage("§aVocê redefiniu o inventário do modo Sopa com sucesso.");

                        return true;
                    }
                });
                response.withCustomItem(48, RESET.getItemStack());

                SAVE = new InteractableItem(new ItemFactory(Material.getMaterial(159)).setDurability(5)
                        .setName("§aSalvar inventário")
                        .setDescription("§7Clique para salvar seu §finventário").getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.sendMessage("§aSalvando...");
                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5f, 1.5f);
                        player.sendMessage("§aVocê salvou o inventário do modo Sopa com sucesso.");
                        return true;
                    }
                });
                response.withCustomItem(50, SAVE.getItemStack());
                response.withCustomItem(49, RETURN.getItemStack());
                break;
        }
        return response.build();
    }

    public void openInventory() {
        if (lock)
            return;
        lock = true;
        inventory.open(player);
        Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Simulator")) {
            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            ItemStack[] items = event.getInventory().getContents();

            ItemStack targetItem = new ItemStack(Material.getMaterial(159), 1, (short) 5);
            if (currentItem != null && currentItem.getType() == targetItem.getType() && currentItem.getDurability() == targetItem.getDurability()) {
                JsonArray jsonArray = new JsonArray();

                for (int i = 0; i < 27; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("slot", i + 9);
                        itemJson.addProperty("type", item.getType().toString());
                        itemJson.addProperty("amount", item.getAmount());
                        jsonArray.add(itemJson);
                    }
                }
                for (int i = 36; i < 45; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("slot", i - 36);
                        itemJson.addProperty("type", item.getType().toString());
                        itemJson.addProperty("amount", item.getAmount());
                        jsonArray.add(itemJson);
                    }
                }

                String jsonData = Constants.GSON.toJson(jsonArray);
                Account target = Account.fetch(player.getUniqueId());
                target.getData(Columns.DUELS_SIMULATOR_INVENTORY).setData(jsonData.toString());
                async(() -> target.getDataStorage().saveColumn(Columns.DUELS_SIMULATOR_INVENTORY));
                player.closeInventory();
                event.setCancelled(true);
                return;
            }

            if (currentItem.getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }

            if (currentItem != null && isSimulatorAttachItem(currentItem)) {
                event.setCancelled(false);
                return;
            }

            if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
        }

        if (event.getView().getTitle().contains("Sopa")) {
            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            ItemStack[] items = event.getInventory().getContents();

            ItemStack targetItem = new ItemStack(Material.getMaterial(159), 1, (short) 5);
            if (currentItem != null && currentItem.getType() == targetItem.getType() && currentItem.getDurability() == targetItem.getDurability()) {
                JsonArray jsonArray = new JsonArray();

                for (int i = 0; i < 27; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("slot", i + 9);
                        itemJson.addProperty("type", item.getType().toString());
                        itemJson.addProperty("amount", item.getAmount());
                        jsonArray.add(itemJson);
                    }
                }
                for (int i = 36; i < 45; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("slot", i - 36);
                        itemJson.addProperty("type", item.getType().toString());
                        itemJson.addProperty("amount", item.getAmount());
                        jsonArray.add(itemJson);
                    }
                }

                String jsonData = Constants.GSON.toJson(jsonArray);
                Account target = Account.fetch(player.getUniqueId());
                target.getData(Columns.DUELS_SOUP_INVENTORY).setData(jsonData.toString());
                async(() -> target.getDataStorage().saveColumn(Columns.DUELS_SOUP_INVENTORY));
                player.closeInventory();
                event.setCancelled(true);
                return;
            }

            if (currentItem.getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }

            if (currentItem != null && isSimulatorAttachItem(currentItem)) {
                event.setCancelled(false);
                return;
            }

            if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
        }

        if (event.getView().getTitle().contains("Gladiator")) {
            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            ItemStack[] items = event.getInventory().getContents();

            ItemStack targetItem = new ItemStack(Material.getMaterial(159), 1, (short) 5);
            if (currentItem != null && currentItem.getType() == targetItem.getType() && currentItem.getDurability() == targetItem.getDurability()) {
                JsonArray jsonArray = new JsonArray();

                for (int i = 0; i < 27; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("slot", i + 9);
                        itemJson.addProperty("type", item.getType().toString());
                        itemJson.addProperty("amount", item.getAmount());
                        jsonArray.add(itemJson);
                    }
                }
                for (int i = 36; i < 45; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("slot", i - 36);
                        itemJson.addProperty("type", item.getType().toString());
                        itemJson.addProperty("amount", item.getAmount());
                        jsonArray.add(itemJson);
                    }
                }

                String jsonData = Constants.GSON.toJson(jsonArray);
                Account target = Account.fetch(player.getUniqueId());
                target.getData(Columns.DUELS_GLADIATOR_INVENTORY).setData(jsonData.toString());
                async(() -> target.getDataStorage().saveColumn(Columns.DUELS_GLADIATOR_INVENTORY));
                player.closeInventory();
                event.setCancelled(true);
                return;
            }

            if (currentItem.getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }

            if (currentItem != null && isGladiatorAttach(currentItem)) {
                event.setCancelled(false);
                return;
            }

            if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
        }
    }

    private boolean isSimulatorAttachItem(ItemStack item) {
        for (SimulatorAttach attach : SimulatorAttach.values()) {
            if (item.getType() == attach.getMaterial()) {
                return true;
            }
        }
        return false;
    }

    private boolean isGladiatorAttach(ItemStack item) {
        for (GladiatorAttach attach : GladiatorAttach.values()) {
            if (item.getType() == attach.getMaterial()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack[] reorganizarItens(ItemStack[] original) {
        ItemStack[] reorganized = new ItemStack[45];
        for (int i = 0; i <= 26; i++) {
            reorganized[i + 9] = original[i];
        }
        for (int i = 36; i <= 44; i++) {
            reorganized[i - 36] = original[i];
        }
        return reorganized;
    }
}
