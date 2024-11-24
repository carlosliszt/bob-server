/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.datas.SkinData;
import com.minecraft.core.account.fields.Flag;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.disguise.PlayerDisguise;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.util.skin.Skin;
import com.minecraft.core.util.skin.SkinCategory;
import com.minecraft.core.util.skin.SkinSubcategory;
import com.minecraft.core.util.skin.util.CustomProperty;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SkinCommand implements BukkitInterface, Listener {

    private final Selector selector = Selector.builder().withSize(27).withName("Categorias de Skin")
            .withCustomItem(10, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.DEFAULT.getUrl()).setName("§a" + SkinCategory.DEFAULT.getName()).setDescription("§7" + SkinCategory.DEFAULT.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.DEFAULT);
                    return true;
                }
            }).getItemStack())
            .withCustomItem(11, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.ANIMES.getUrl()).setName("§a" + SkinCategory.ANIMES.getName()).setDescription("§7" + SkinCategory.ANIMES.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.ANIMES);
                    return true;
                }
            }).getItemStack())
            .withCustomItem(12, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.MOVIES.getUrl()).setName("§a" + SkinCategory.MOVIES.getName()).setDescription("§7" + SkinCategory.MOVIES.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.MOVIES);
                    return true;
                }
            }).getItemStack())
            .withCustomItem(13, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.HEROES.getUrl()).setName("§a" + SkinCategory.HEROES.getName()).setDescription("§7" + SkinCategory.HEROES.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.HEROES);
                    return true;
                }
            }).getItemStack())
            .withCustomItem(14, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.GAMES.getUrl()).setName("§a" + SkinCategory.GAMES.getName()).setDescription("§7" + SkinCategory.GAMES.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.GAMES);
                    return true;
                }
            }).getItemStack())
            .withCustomItem(15, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.MINECRAFT.getUrl()).setName("§a" + SkinCategory.MINECRAFT.getName()).setDescription("§7" + SkinCategory.MINECRAFT.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.MINECRAFT);
                    return true;
                }
            }).getItemStack())
            .withCustomItem(16, new InteractableItem(new ItemFactory().setSkullURL(SkinCategory.YOUTUBERS.getUrl()).setName("§a" + SkinCategory.YOUTUBERS.getName()).setDescription("§7" + SkinCategory.YOUTUBERS.getDescription() + "\n\n§eClique para ver mais!").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    openSubcategory(player, SkinCategory.YOUTUBERS);
                    return true;
                }
            }).getItemStack())
            .build();

    @Command(name = "skin", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        Account account = context.getAccount();

        if (account.getFlag(Flag.SKIN)) {
            context.info("flag.locked");
            return;
        }

       /* final boolean suffocated = ((CraftPlayer) context.getSender()).getHandle().inBlock();

        if (suffocated) {
            context.sendMessage("§cVocê não pode alterar sua skin agora.");
            return;
        } */

        if (context.argsCount() == 0)
            selector.open(context.getSender());
        else if (context.getArg(0).equalsIgnoreCase("reset")) {

            if (account.getSkinData().getSource() == SkinData.Source.ACCOUNT) {
                context.info("command.nick.no_skin_to_reset");
                return;
            }

            async(() -> {

                Property property;

                if (Constants.getCrackedUniqueId(account.getUsername()).equals(account.getUniqueId())) {
                    CustomProperty customProperty = Skin.getRandomSkin().getCustomProperty();
                    property = new Property(customProperty.getName(), customProperty.getValue(), customProperty.getSignature());
                } else {
                    property = Constants.getMojangAPI().getProperty(account.getUniqueId());
                }

                if (property == null) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                SkinData skinData = account.getSkinData();

                skinData.setName(account.getUsername());
                skinData.setValue(property.getValue());
                skinData.setSignature(property.getSignature());
                skinData.setSource(SkinData.Source.ACCOUNT);
                skinData.setUpdatedAt(System.currentTimeMillis());

                account.getData(Columns.SKIN).setData(skinData.toJson());
                account.getDataStorage().saveColumn(Columns.SKIN);

                sync(() -> {
                    PlayerDisguise.changeSkin(context.getSender(), property);
                    context.info("command.nick.skin_reset");
                });
                Constants.getRedis().publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + property.getValue() + ":" + property.getSignature());
            });
        } else if (account.hasPermission(Rank.PRO)) {

            if (!context.getAccount().hasPermission(Rank.PRIMARY_MOD) && CooldownProvider.getGenericInstance().hasCooldown(account.getUniqueId(), "command.skin")) {
                Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(context.getUniqueId(), "command.skin");
                if (cooldown != null && !cooldown.expired()) {
                    context.info("wait_generic", Constants.SIMPLE_DECIMAL_FORMAT.format(cooldown.getRemaining()));
                    return;
                }
            }

            async(() -> {

                String skin = context.getArg(0);

                if (!Constants.isValid(skin)) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                UUID uniqueId = Constants.getMojangAPI().getUniqueId(skin);

                if (uniqueId == null) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                Property property = Constants.getMojangAPI().getProperty(uniqueId);

                if (property == null) {
                    context.info("object.not_found", "Skin");
                    return;
                }

                SkinData skinData = account.getSkinData();

                skinData.setName(skin);
                skinData.setValue(property.getValue());
                skinData.setSignature(property.getSignature());
                skinData.setSource(SkinData.Source.CUSTOM);
                skinData.setUpdatedAt(System.currentTimeMillis());

                account.getData(Columns.SKIN).setData(skinData.toJson());
                account.getDataStorage().saveColumn(Columns.SKIN);

                sync(() -> {
                    context.info("command.nick.skin_change", skin);
                    if (account.getVersion() < 47)
                        context.info("command.nick.skin_change.legacy_version.warning");
                    PlayerDisguise.changeSkin(context.getSender(), property);
                    CooldownProvider.getGenericInstance().addCooldown(context.getUniqueId(), "command.skin", 60, false);
                });
                Constants.getRedis().publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + property.getValue() + ":" + property.getSignature());
            });
        } else {
            context.info("command.skin.no_permission", Constants.SERVER_STORE);
        }
    }

    protected final ItemStack itemStack = new InteractableItem(new ItemFactory(Material.ARROW).setName("§aVoltar").setDescription("§7Para Categorias de Skin").getStack(), new InteractableItem.Interact() {
        @Override
        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
            selector.open(player);
            return true;
        }
    }).getItemStack();

    protected void openSubcategory(final Player player, final SkinCategory skinCategory) {
        List<ItemStack> itemStacks = new ArrayList<>();
        SkinSubcategory.getSkinSubcategories(skinCategory).forEach(skinSubcategory -> itemStacks.add(new ItemFactory().setSkullURL(skinSubcategory.getUrl()).setName("§a" + skinSubcategory.getName()).setDescription("§7" + skinSubcategory.getDescription() + "\n\n" + "§eClique para ver mais!").getStack()));

        Selector selector = Selector.builder().withSize(45).withName("Subcategorias: " + skinCategory.getName()).withPreviousPageSlot(39).withNextPageSlot(41).withAllowedSlots(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33, 34)).withItems(itemStacks).withCustomItem(40, itemStack).build();
        selector.open(player);
    }

    protected void openSkins(final Player player, final SkinSubcategory subCategory) {
        Account account = Account.fetch(player.getUniqueId());
        List<ItemStack> itemStacks = new ArrayList<>();

        com.minecraft.core.util.skin.Skin.getSkins(subCategory).forEach(skin -> {
            boolean has = account.hasPermission(skin.getPermission());
            Rank permission = skin.getPermission();

            ItemFactory itemFactory = new ItemFactory();

            if (has) {
                itemFactory.setSkullURL(skin.getUrl());
            } else {
                itemFactory.setItemStack(new ItemStack(Material.INK_SACK, 1, (short) 8));
            }

            itemFactory.setName((has ? "§a" : "§c") + skin.getName());

            List<String> stringList = new ArrayList<>();

            if (permission.getId() != Rank.MEMBER.getId()) {
                stringList.add(" ");
                stringList.add("§7Exclusivo para " + permission.getDefaultTag().getColor() + permission.getName());
                stringList.add(" ");
            }

            stringList.add(has ? "§eClique para selecionar." : "§cVocê não possui essa skin.");

            itemFactory.setDescription(stringList);

            itemStacks.add(itemFactory.getStack());
        });

        InteractableItem item = new InteractableItem(new ItemFactory(Material.ARROW).setName("§aVoltar").setDescription("§7Para Subcategorias").getStack(), new InteractableItem.Interact() {
            @Override
            public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                openSubcategory(player, subCategory.getCategory());
                return true;
            }
        });

        Selector selector = Selector.builder().withName("Skins: " + subCategory.getName()).withPreviousPageSlot(39).withNextPageSlot(41).withAllowedSlots(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)).withSize(45).withItems(itemStacks).withCustomItem(40, item.getItemStack()).build();
        selector.open(player);
    }

    @EventHandler
    public void Interact(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        final Player player = (Player) event.getWhoClicked();
        final Account account = Account.fetch(player.getUniqueId());

        if (account.getFlag(Flag.SKIN)) return;

        if (event.getInventory().getName().contains("Subcategorias")) {
            if (event.getCurrentItem() == null)
                return;
            if (event.getCurrentItem().getType() == Material.AIR)
                return;
            if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
                return;
            event.setCancelled(true);
            SkinSubcategory skinSubcategory = SkinSubcategory.getSkinSubCategory(event.getCurrentItem().getItemMeta().getDisplayName().replace("§a", ""));
            openSkins(player, skinSubcategory);
        } else if (event.getInventory().getName().contains("Skins:")) {
            if (event.getCurrentItem() == null)
                return;
            if (event.getCurrentItem().getType() == Material.AIR)
                return;
            event.setCancelled(true);
            if (event.getCurrentItem().getType() != Material.SKULL_ITEM)
                return;
            com.minecraft.core.util.skin.Skin skin = com.minecraft.core.util.skin.Skin.getSkin(event.getCurrentItem().getItemMeta().getDisplayName().replace("§a", "").replace("§c", ""));
            if (skin == null)
                return;

            player.closeInventory();
            player.sendMessage("§aVocê alterou a sua skin para " + skin.getName() + ".");
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 3F, 3F);

            PlayerDisguise.changeSkin(player, new Property("textures", skin.getCustomProperty().getValue(), skin.getCustomProperty().getSignature()));

            SkinData skinData = account.getSkinData();

            skinData.setName(skin.getName());
            skinData.setValue(skin.getCustomProperty().getValue());
            skinData.setSignature(skin.getCustomProperty().getSignature());
            skinData.setSource(SkinData.Source.LIBRARY);
            skinData.setUpdatedAt(System.currentTimeMillis());

            async(() -> {
                account.getData(Columns.SKIN).setData(skinData.toJson());
                account.getDataStorage().saveColumn(Columns.SKIN);
                Constants.getRedis().publish(Redis.SKIN_CHANGE_CHANNEL, account.getUniqueId() + ":" + skin.getCustomProperty().getValue() + ":" + skin.getCustomProperty().getSignature());
            });
        }
    }

    public SkinCommand() {
        BukkitGame.getEngine().getBukkitFrame().unregisterCommand("skin");
        BukkitGame.getEngine().getBukkitFrame().registerCommands(this);
        HandlerList.unregisterAll(this);
        BukkitGame.getEngine().getServer().getPluginManager().registerEvents(this, BukkitGame.getEngine());
    }
}