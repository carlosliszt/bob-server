package com.minecraft.lobby.duel.inventory;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.enums.Effects;
import com.minecraft.core.bukkit.util.enums.KillsEffect;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.duel.inventory.enums.FactoryInventory;
import com.minecraft.lobby.duel.inventory.enums.FactorySettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelsSettingsInventory implements Listener, BukkitInterface {

    private static final List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16);
    protected boolean lock = false;

    protected Player player;
    protected Account account;
    protected Selector inventory;
    protected FactorySettings type;

    public DuelsSettingsInventory(Player player, Account account, FactorySettings type) {
        this.player = player;
        this.account = account;
        this.type = type;
        this.inventory = build();
    }

    private void update() {
        HandlerList.unregisterAll(this);
        this.inventory = null;
        new DuelsSettingsInventory(player, account, type).openInventory();
    }

    private Selector build() {
        Selector.Builder response = Selector.builder().withSize(type.getSlots()).withName(type.getTitle());
        List<ItemStack> items = new ArrayList<>();

        InteractableItem BACK = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("\n§eClique para voltar").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.closeInventory();
                new DuelsSettingsInventory(player, account, FactorySettings.MAIN).openInventory();
                return true;
            }
        });

        switch (type) {
            case MAIN:
                InteractableItem INVENTORY = new InteractableItem(new ItemFactory().setType(Material.ANVIL).setName("§aEditar inventários")
                        .setDescription(
                                "§7Customize os inventários",
                                "§7padrões dos modos.",
                                "",
                                "§eClique para ver mais."
                        )
                        .getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new InventoryEditorCollection(player, Account.fetch(player.getUniqueId()), FactoryInventory.MAIN).openInventory();
                        return true;
                    }
                });
                InteractableItem KILL_EFFECT = new InteractableItem(new ItemFactory().setType(Material.DIAMOND_SWORD).setName("§aEfeitos de Abates")
                        .setDescription(
                                "§8" + only(account) + "/" + KillsEffect.values().length,
                                "",
                                "§7Customize o seu efeito",
                                "§7de abate.",
                                "",
                                "§7Selecionado: §c" + KillsEffect.fromId(account.getDataStorage().getData(Columns.DUELS_KILL_EFFECT).getAsInt()).getName(),
                                "",
                                "§eClique para ver mais."
                        )
                        .getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new DuelsSettingsInventory(player, account, FactorySettings.KILL_EFFECT).openInventory();
                        return true;
                    }
                });

                InteractableItem PREFERENCES = new InteractableItem(new ItemFactory().setType(Material.REDSTONE_COMPARATOR).setName("§aPreferências")
                        .setDescription(
                                "§7Altere as preferências",
                                "§7do Duels.",
                                "",
                                "§eClique para ver mais."
                        )
                        .getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        new DuelsSettingsInventory(player, account, FactorySettings.PREFERENCES).openInventory();
                        return true;
                    }
                });
                response.withCustomItem(11, INVENTORY.getItemStack());
                response.withCustomItem(15, KILL_EFFECT.getItemStack());
                response.withCustomItem(22, PREFERENCES.getItemStack());
                break;
            case PREFERENCES:
                response.withSize(27);
                var item = new ItemFactory().setType(Material.STAINED_GLASS_PANE).setDurability(14).addItemFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE)
                        .setName("§cOps!")
                        .setDescription(
                                "§eAqui ainda está vázio!")
                        .getStack();
                response.withCustomItem(13, item);
                response.withCustomItem(18, BACK.getItemStack());
                break;
            case KILL_EFFECT:
                response.withSize(54);

                int data = account.getData(Columns.DUELS_KILL_EFFECT).getAsInt();

                int slotIndex = 10;

                for (KillsEffect effect : KillsEffect.values()) {
                    if (slotIndex > 16) break;

                    ItemStack stack = null;

                    if (data == effect.getId()) {
                        stack = new ItemFactory().setType(effect.getItem())
                                .addEnchantment(Enchantment.DAMAGE_ALL, 1)
                                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES).setName((data == effect.getId() ? "§a" : "§e") + effect.getName())
                                .setDescription(
                                        "§7" + effect.getDescription().toString(),
                                        "",
                                        (data == effect.getId() ? "§aJá selecionado." : "§eClique para selecionar.")
                                )
                                .getStack();
                    } else {
                        stack = new ItemFactory().setType(effect.getItem()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE).setName((data == effect.getId() ? "§a" : "§e") + effect.getName())
                                .setDescription(
                                        "§7" + effect.getDescription().toString(),
                                        "",
                                        (data == effect.getId() ? "§aJá selecionado." : "§eClique para selecionar.")
                                ).getStack();
                    }

                    InteractableItem E = new InteractableItem(stack, new InteractableItem.Interact() {
                        @Override
                        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                            if (account.hasPermission(effect.getRequires())) {
                                player.closeInventory();
                                player.sendMessage("§aVocê selecionou §f" + effect.getName() + "§a!");
                                updateData(account, effect.getId());
                                new DuelsSettingsInventory(player, account, FactorySettings.KILL_EFFECT).openInventory();
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.3F, 1.3F);
                            } else {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.1F, 1.1F);
                            }
                            return true;
                        }
                    });

                    response.withCustomItem(slotIndex++, E.getItemStack());
                }

                KillsEffect effect = KillsEffect.fromId(data);
                InteractableItem F = new InteractableItem(new ItemFactory().setType(effect.getItem())
                        .setName("§eSelecionado: §f" + effect.getName())
                        .setDescription("§7" + effect.getDescription().toString())
                        .getStack(), new InteractableItem.Interact() {
                    @Override
                    public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                        player.playSound(player.getLocation(), Sound.CLICK, 1.1F, 1.1F);
                        return true;
                    }
                });

                response.withCustomItem(50, F.getItemStack());

                response.withCustomItem(49, BACK.getItemStack());
                break;
        }
        return response.build();
    }

    public void updateData(Account account, int id) {
        try {
            Data dataObject = account.getData(Columns.DUELS_KILL_EFFECT);
            if (dataObject != null) {
                dataObject.setData(id);
                async(() -> account.getDataStorage().saveColumn(Columns.DUELS_KILL_EFFECT));
            } else {
                System.err.println("Erro: Dados de efeito de morte não encontrados para atualização.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openInventory() {
        if (lock)
            return;
        lock = true;
        inventory.open(player);
        Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
    }

    int only(Account account) {
        int value = 0;
        for (var effect : KillsEffect.values()) {
            if (account.hasRank(effect.getRequires())) {
                value++;
            }
        }
        return value;
    }
}
