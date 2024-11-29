package com.minecraft.lobby.accessory;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.emotions.EmotionsAccessory;
import com.minecraft.core.bukkit.accessory.list.emotions.list.EmotionCollector;
import com.minecraft.core.bukkit.accessory.list.particles.list.ParticleCollector;
import com.minecraft.core.bukkit.accessory.list.title.TitleAccessory;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.reflection.ClassHandler;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.accessory.title.TitleSelectionInventory;
import com.minecraft.lobby.duel.inventory.InventoryEditorCollection;
import com.minecraft.lobby.duel.inventory.enums.FactoryInventory;
import com.minecraft.lobby.duel.inventory.enums.FactorySettings;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AccessoryInventory implements Listener, BukkitInterface {
    protected Player player;
    protected Account account;
    protected Selector inventory;
    protected AccessoryType type;
    protected InteractableItem returnItem;

    public AccessoryInventory(Player player, Account account, AccessoryType type) {
        this.player = player;
        this.account = account;
        this.type = type;
        this.inventory = build();

        this.returnItem = null;
    }

    private Selector build() {
        Selector.Builder response = Selector.builder().withSize(45).withName("Acessórios");
        String value = Account.fetch(player.getUniqueId()).getData(Columns.ACCESSORY).getAsString();
        int indexSlot = 10;
        this.returnItem = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("\n§eClique para voltar").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                player.closeInventory();
                new AccessoryInventory(player, account, AccessoryType.NULL).openInventory();
                return true;
            }
        });

        switch (type) {
            case NULL:
                response.withSize(45);
                response.withName("Acessórios");

                for (Items i : Items.values()) {
                    InteractableItem item = new InteractableItem(i.getItem().replaceDescription("{unlocked}", i.unlocked(account, i.getType())).replaceDescription("{quantity}", i.total(i.getType())).replaceDescription("{percentage}", i.percentage(account, i.getType())).getStack(), new InteractableItem.Interact() {
                        @Override
                        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                            new AccessoryInventory(player, account, i.getType()).openInventory();
                            return true;
                        }
                    });

                    response.withCustomItem(i.getSlot(), item.getItemStack());
                }
                break;
            case HEAD:
                response.withName("Acessórios: Cabeças");
                response.withCustomItem(27, returnItem.getItemStack());
                break;
            case GADGET:
                response.withSize(36);
                response.withName("Acessórios: Engenhocas");
                indexSlot = 10;
                for (Accessory accessory : BukkitGame.getEngine().getAccessoryStorage().getAccessories(type)) {
                    InteractableItem item = new InteractableItem(
                            new ItemFactory()
                                    .setItemStack(accessory.getItemStack())
                                    .setName(title(account, accessory))
                                    .setDescription(
                                            "§7" + accessory.getDescription(),
                                            "",
                                            "§7Raridade: " + accessory.getRarity().getDisplayName(),
                                            "",
                                            "§c" + (!account.hasPermission(accessory.getRank()) ? "§cNÃO POSSUI" : value.equals(accessory.getName().toLowerCase()) ? "§aSELECIONADO" : "§eCLIQUE PARA SELECIONAR!")
                                    )
                                    .getStack(), new InteractableItem.Interact() {
                        @Override
                        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                            if (account.getVersion() < 47 && accessory.getName().equals("Fossil")) {
                                player.sendMessage("§r\n§4§lAVISO\n§4* §cLamentamos :(\n§4* §cPara conseguir usar este tipo de acessório é preciso estar em uma versão igual ou súperior à §e'1.8'§c.\n§r");
                                return true;
                            }

                            if (!accessory.hasActiveAccessory(player.getUniqueId())) {
                                accessory.remove(player);
                            }

                            if (value.equals(accessory.getName().toLowerCase())) {
                                player.closeInventory();
                                accessory.remove(player);
                                sync(() -> account.getDataStorage().saveColumn(Columns.ACCESSORY));
                                Account.fetch(player.getUniqueId()).getData(Columns.ACCESSORY).setData("...");
                                player.sendMessage("§cVocê removeu o acessório §e'" + accessory.getName().replace("_", " ") + "'§c §8(" + accessory.getType().getDisplayName() + ")§c.");
                            } else {
                                Account.fetch(player.getUniqueId()).getData(Columns.ACCESSORY).setData(accessory.getName().toLowerCase());
                                accessory.setPlayerAccessory(player.getUniqueId(), accessory);

                                accessory.give(player);

                                sync(() -> account.getDataStorage().saveColumn(Columns.ACCESSORY));

                                player.closeInventory();
                                player.sendMessage("§aVocê selecionou o acessório §e'" + accessory.getName().replace("_", " ") + "'§a §8(" + accessory.getType().getDisplayName() + ")§a.");
                            }
                            return true;
                        }
                    });

                    response.withCustomItem(indexSlot, item.getItemStack());
                    indexSlot++;
                }
                response.withCustomItem(27, returnItem.getItemStack());
                break;
            case TITLES:
                response.withName("Acessórios: Titulos");
                response.withCustomItem(27, returnItem.getItemStack());
                indexSlot = 10;
                for(TitleAccessory.TitleCategory category : TitleAccessory.TitleCategory.values()) {
                    InteractableItem item = new InteractableItem(
                            new ItemFactory().setType(category.getMaterial()).setName("§a" + category.getName()).setDescription(
                                    "",
                                    "§7Desbloqueados: §c-/- §8(0%)",
                                    "",
                                    "§eClique para ver mais!"
                            ).getStack(), new InteractableItem.Interact() {
                        @Override
                        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                            new TitleSelectionInventory(player, account, category).openInventory();
                            return true;
                        }
                    }

                    );
                    response.withCustomItem(indexSlot, item.getItemStack());
                    indexSlot++;
                }
                response.withCustomItem(27, returnItem.getItemStack());

                break;
            case PARTICLES:
                response.withSize(36);
                response.withName("Acessórios: Partículas");
                indexSlot = 10;
                for (ParticleCollector collector : ParticleCollector.values()) {
                    Accessory accessory = collector.getAccessory();
                    InteractableItem item = new InteractableItem(
                            new ItemFactory()
                                    .setType(accessory.getItemStack().getType())
                                    .setName(title(account, accessory))
                                    .setDescription(
                                            "§7" + accessory.getDescription(),
                                            "",
                                            "§7Raridade: " + accessory.getRarity().getDisplayName(),
                                            "",
                                            "§c" + (!account.hasPermission(accessory.getRank()) ? "§cNÃO POSSUI" : value.equals(accessory.getName().toLowerCase()) ? "§aSELECIONADO" : "§eCLIQUE PARA SELECIONAR!")
                                    )
                                    .getStack(), new InteractableItem.Interact() {
                        @Override
                        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {

                            if (!accessory.hasActiveAccessory(player.getUniqueId())) {
                                accessory.remove(player);
                            }

                            if (value.equals(accessory.getName().toLowerCase())) {
                                player.closeInventory();
                                accessory.remove(player);
                                sync(() -> account.getDataStorage().saveColumn(Columns.PARTICLE));
                                Account.fetch(player.getUniqueId()).getData(Columns.PARTICLE).setData("...");
                                player.sendMessage("§cVocê removeu o acessório §e'" + accessory.getName().replace("_", " ") + "'§c §8(" + accessory.getType().getDisplayName() + ")§c.");
                            } else {
                                Account.fetch(player.getUniqueId()).getData(Columns.PARTICLE).setData(accessory.getName().toLowerCase());
                                accessory.setPlayerAccessory(player.getUniqueId(), accessory);
                                sync(() -> account.getDataStorage().saveColumn(Columns.PARTICLE));

                                accessory.give(player);

                                player.closeInventory();
                                player.sendMessage("§aVocê selecionou o acessório §e'" + accessory.getName().replace("_", " ") + "'§a §8(" + accessory.getType().getDisplayName() + ")§a.");
                            }
                            return true;
                        }
                    });

                    response.withCustomItem(indexSlot, item.getItemStack());
                    indexSlot++;
                }
                response.withCustomItem(27, returnItem.getItemStack());
                break;
            case EMOTIONS:
                List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
                int slotIndex = 0;

                for (EmotionCollector collector : EmotionCollector.values()) {
                    Accessory accessory = collector.getAccessory();
                    InteractableItem item = new InteractableItem(
                            new ItemFactory().setSkullURL(collector.getUrl())
                                    .setName(title(account, accessory))
                                    .setDescription(
                                            "",
                                            "§7Raridade: " + accessory.getRarity().getDisplayName(),
                                            "",
                                            "§c" + (!account.hasPermission(accessory.getRank()) ? "§cNÃO POSSUI" : value.equals(accessory.getName().toLowerCase()) ? "§aSELECIONADO" : "§eCLIQUE PARA SELECIONAR!")
                                    )
                                    .getStack(),
                            new InteractableItem.Interact() {
                                @Override
                                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                                    if (!accessory.hasActiveAccessory(player.getUniqueId())) {
                                        accessory.remove(player);
                                    }

                                    if (value.equals(accessory.getName().toLowerCase())) {
                                        player.closeInventory();
                                        accessory.remove(player);
                                        sync(() -> account.getDataStorage().saveColumn(Columns.PARTICLE));
                                        Account.fetch(player.getUniqueId()).getData(Columns.PARTICLE).setData("...");
                                        player.sendMessage("§cVocê removeu o acessório §e'" + accessory.getName().replace("_", " ") + "'§c §8(" + accessory.getType().getDisplayName() + ")§c.");
                                    } else {
                                        Account.fetch(player.getUniqueId()).getData(Columns.PARTICLE).setData(accessory.getName().toLowerCase());
                                        accessory.setPlayerAccessory(player.getUniqueId(), accessory);
                                        sync(() -> account.getDataStorage().saveColumn(Columns.PARTICLE));

                                        accessory.give(player);

                                        player.closeInventory();
                                        player.sendMessage("§aVocê selecionou o acessório §e'" + accessory.getName().replace("_", " ") + "'§a §8(" + accessory.getType().getDisplayName() + ")§a.");
                                    }
                                    return true;
                                }
                            });

                    if (slotIndex < allowedSlots.size()) {
                        response.withCustomItem(allowedSlots.get(slotIndex), item.getItemStack());
                        slotIndex++;
                    }
                }
                response.withCustomItem(36, returnItem.getItemStack());
                break;
        }

        return response.build();
    }

    public void openInventory() {
        inventory.open(player);
        Bukkit.getPluginManager().registerEvents(this, BukkitGame.getEngine());
    }

    public String title(Account account, Accessory accessory) {
        if (account.hasPermission(accessory.getRank())) {
            return "§a" + accessory.getName().replace("_", " ") + " §8(Desbloqueado)";
        } else {
            return "§c" + accessory.getName().replace("_", " ") + " §8(Bloqueado)";
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum Items {

        HEAD(10, AccessoryType.HEAD, new ItemFactory().setSkullURL("33667731eb9f1a45cb2722e72d40e4bfe500d2d105767e9849f8ffb24775824d").setName("§aCabeças Customizadas").setDescription("§7Selecione qualquer tipo de", "§7cabeça disponível no servidor!", "", "§7Desbloqueados: §b{unlocked}/{quantity} §8({percentage}%)", "", "§eClique para ver mais!")),
        HATS(12, AccessoryType.HATS, new ItemFactory().setType(Material.GOLD_HELMET).setName("§aChapéus").setDescription("§7Saia estiloso com diversos", "§7chapéus que te dão uma cara", "§7nova.", "", "§7Desbloqueados: §b{unlocked}/{quantity} §8({percentage}%)", "", "§eClique para ver mais!")),
        GADGET(14, AccessoryType.GADGET, new ItemFactory().setType(Material.getMaterial(347)).setName("§aEngenhocas").setDescription("§7Tenha a possibilidade de usar", "§7diferentes engenhocas como um", "§7acessório diferenciado! Que tal?", "", "§7Desbloqueados: §b{unlocked}/{quantity} §8({percentage}%)", "", "§eClique para ver mais!")),
        PARTICLES(16, AccessoryType.PARTICLES, new ItemFactory().setType(Material.BLAZE_POWDER).setName("§aPartículas").setDescription("§7Exponha suas diversas partículas", "§7à outros jogadores pelos lobbies!", "", "§7Desbloqueados: §b{unlocked}/{quantity} §8({percentage}%)", "", "§eClique para ver mais!")),
        TITLES(22, AccessoryType.TITLES, new ItemFactory().setType(Material.NAME_TAG).setName("§aTitulos").setDescription("§7-/-")),
        EMOTIONS(28, AccessoryType.EMOTIONS, new ItemFactory().setSkullURL("45b0595a511c4b377437a516fefbea2ffcf35585ae53465b0e86024a215eb").setName("§aEmoções").setDescription("§7-/-")),
        FLAGS(30, AccessoryType.FLAGS, new ItemFactory().setType(Material.BANNER).setName("§aBandeiras").setDescription("§7-/-")),
        ARMORS(32, AccessoryType.ARMORS, new ItemFactory().setType(Material.LEATHER_CHESTPLATE).setColor(Color.AQUA).setName("§aArmaduras").setDescription("§7-/-")),
        WINGS(34, AccessoryType.WINGS, new ItemFactory().setType(Material.FEATHER).setName("§aAsas Personalizadas").setDescription("§7-/-"));

        private final int slot;
        private final AccessoryType type;
        private final ItemFactory item;

        public int total(AccessoryType type) {
            return (int) BukkitGame.getEngine()
                    .getAccessoryStorage()
                    .getAccessories(type)
                    .stream()
                    .filter(accessory -> accessory.getType() == type)
                    .count();
        }

        public int unlocked(Account account, AccessoryType type) {
            return (int) BukkitGame.getEngine()
                    .getAccessoryStorage()
                    .getAccessories(type)
                    .stream()
                    .filter(accessory -> account.hasPermission(accessory.getRank()))
                    .count();
        }

        public int percentage(Account account, AccessoryType type) {
            int totalAccessories = total(type);
            if (totalAccessories == 0) {
                return 0;
            }

            int unlockedAccessories = unlocked(account, type);
            return (int) Math.round((unlockedAccessories / (double) totalAccessories) * 100.0);
        }
    }
}
