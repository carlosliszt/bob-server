package com.minecraft.core.bukkit.util.inventory;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.fields.Preference;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.command.ProfileCommand;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.InteractableItem;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.redis.Redis;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferencesInventory implements Listener, BukkitInterface {

    private static final List<Integer> allowedSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16);

    protected boolean lock = false;

    protected Player player;
    protected Account account;
    protected Selector inventory;
    protected boolean fromProfile;

    public PreferencesInventory(Player player, Account account, boolean fromProfile) {
        this.player = player;
        this.account = account;
        this.fromProfile = fromProfile;
        this.inventory = build();
    }

    private void update() {
        HandlerList.unregisterAll(this);
        this.inventory = null;
        new PreferencesInventory(player, account, fromProfile).openInventory();
    }

    private Selector build() {

        Selector.Builder response = Selector.builder().withAllowedSlots(allowedSlots).withSize(36).withName(this.account.getLanguage().translate("container.preferences.title")).withPreviousPageSlot(28).withNextPageSlot(35);

        List<ItemStack> items = new ArrayList<>();

        for (BukkitPreferences preference : BukkitPreferences.getValues()) {

            if (!account.hasPermission(preference.getPreference().getRank()))
                continue;

            boolean active = account.getPreference(preference.getPreference());
            String name = (active ? "§a" : "§c") + account.getLanguage().translate("container.preferences." + preference.name().toLowerCase());

            items.add(new ItemFactory(preference.getItemStack().getType()).setName(name).setDescription(account.getLanguage().translate("container.preferences." + preference.name().toLowerCase() + ".description")).getStack());
            response.withCustomItem(allowedSlots.get(items.size()) + 8, new ItemFactory(Material.INK_SACK).setName(name).setDurability(active ? 10 : 8).setDescription(active ? "§7Clique para desativar." : "§7Clique para ativar.").getStack());
        }

        if (fromProfile) {
            InteractableItem profile = new InteractableItem(new ItemFactory().setType(Material.ARROW).setName("§aVoltar").setDescription("Para Perfil").getStack(), new InteractableItem.Interact() {
                @Override
                public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, InteractableItem.InteractAction action) {
                    new ProfileCommand.ProfileMenu(player, account).open();
                    return true;
                }
            });
            response.withCustomItem(31, profile.getItemStack());
        }

        response.withItems(items);
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
    public void ListenerOnClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getEntityId() != this.player.getEntityId())
            return;

        if (!event.getInventory().getName().equals(this.inventory.getName()))
            return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null)
            return;

        for (BukkitPreferences preference : BukkitPreferences.getValues()) {

            if (!account.hasPermission(preference.getPreference().getRank()))
                continue;

            if (checkClick(preference, event.getCurrentItem()) || (event.getCurrentItem().getType() == Material.INK_SACK && checkClick(preference, event.getClickedInventory().getItem(event.getSlot() - 9)))) {
                account.setPreference(preference.getPreference(), !account.getPreference(preference.getPreference()));
                update();
                break;
            }
        }
    }

    @EventHandler
    public void ListenerOnClose(InventoryCloseEvent event) {
        if (event.getPlayer().getEntityId() != this.player.getEntityId())
            return;
        if (!event.getInventory().getName().equals(this.inventory.getName()))
            return;
        lock = false;
        account.getData(Columns.PREFERENCES).setData(account.getPreferences());

        async(() -> {
            account.getDataStorage().saveColumn(Columns.PREFERENCES);
            Constants.getRedis().publish(Redis.PREFERENCES_UPDATE_CHANNEL, account.getUniqueId() + ":" + account.getPreferences());
        });

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void ListenerOnQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getEntityId() == player.getEntityId())
            HandlerList.unregisterAll(this);
    }

    public boolean checkClick(BukkitPreferences preference, ItemStack itemStack) {
        return preference.getItemStack().getType() == itemStack.getType() && preference.getItemStack().getDurability() == itemStack.getDurability();
    }

    @AllArgsConstructor
    @Getter
    public enum BukkitPreferences {

        CHAT(Preference.CHAT, new ItemStack(Material.BOOK_AND_QUILL)),
        TELL(Preference.TELL, new ItemStack(Material.SIGN)),
        CLAN(Preference.CLAN, new ItemStack(Material.ITEM_FRAME)),
        EXTRA_INFO(Preference.EXTRA_INFO, new ItemStack(Material.PAPER)),
        STATISTICS(Preference.STATISTICS, new ItemStack(Material.HOPPER)),
        FRIENDS(Preference.FRIEND_REQUEST, new ItemStack(Material.COOKIE)),
        ;

        private final Preference preference;
        private final ItemStack itemStack;

        @Getter
        private static final BukkitPreferences[] values;

        static {
            values = values();
        }

    }

}