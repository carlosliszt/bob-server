package com.minecraft.core.bukkit.accessory.list.gadget;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.gadget.runnable.BalloonObject;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BalloonGadgetAccessory extends Accessory {

    public BalloonGadgetAccessory() {
        super("PvKv9",
                "Lança-Balões",
                "gadget.balloon.accessory",
                Rank.PRO,
                new ItemFactory().setSkullProperty(new Property("textures", "eyJ0aW1lc3RhbXAiOjE1NDcxNDgxNTYxMTUsInByb2ZpbGVJZCI6IjQzYTgzNzNkNjQyOTQ1MTBhOWFhYjMwZjViM2NlYmIzIiwicHJvZmlsZU5hbWUiOiJTa3VsbENsaWVudFNraW42Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MmRkMTFkYTA0MjUyZjc2YjY5MzRiYzI2NjEyZjU0ZjI2NGYzMGVlZDc0ZGY4OTk0MTIwOWUxOTFiZWJjMGEyIn19fQ==", "vWUIKujtg38OHDdeA0CN/K68mFZ0F4VOmh76ioVWZ8ZN0/dg6fuvrwVBXICvxBionQH6ujoQAL04HVsaqARZ8RBpKfcF/L6XvEqi3dukExxtzOS2R2w/3va0LwT10Ssvyehx7dXrp8eFKA+gBhSamwatTUMo3atvCxqHsk283TERWGbV68p0gVFq6cMMUxbvOq+x3fcTDSza9WeIanjuivIfmUJgIlsabTA7wnvZjquJYSx4N9WCLvIn5gyzVk7VlK09UgBT9Ah80VjrT/xmGZ1l7y25NoORLEIhHPP9LEs47BQlh5XP3/oyxJkJlHr6bNKxrub8WbZOz9ro098OeF2fm+1FDB1JRSGr4FyUGyPuNr1fsASSLcAbvWvaiP6Zt5gZWdMOySF8Cm9Q2dsCpNzLSojDkZE6N1+qrrhBSclG2We8G/a8UaCRfpuP/dCHQj8Ux3Ic7vMd09VMn5WY72h6d3Tnmaty2VuaQk2AVt1t9+BV6inOotvzFTgrfTM1uWG4fVO0WVdicuxLbl2J2au6HReUb+wy6RX1/0edJZXQwexiZA50MUDGlYLETqcHhyBn0mEhFKsuJ/cNOFAMs+mDAX5HnreDqG1rH3sXdfRk4yICNcPiL4su8T4wOtXYOyNX+rPj0BTE1j2vzGKymWkx+KvNbVjpbhec1u4LkDU=")).setName("§aLança-Balões").getStack(),
                "Lance balões pelos ares dos lobbies!",
                true,
                AccessoryType.GADGET,
                AccessoryRarity.RARE);
    }

    @Override
    public void updateAsync(Player player, ServerHeartbeatEvent event) {}

    @Override
    public void give(Player player) {
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.5f, 1.5f);
        setPlayerAccessory(player.getUniqueId(), this);
        player.getInventory().addItem(getItemStack());
    }

    @Override
    public void remove(Player player) {
        int balloonSlot = player.getInventory().first(getItemStack());
        if (balloonSlot != -1) {
            player.getInventory().setItem(balloonSlot, new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand() != null && player.getItemInHand().equals(this.getItemStack())) {
            if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "gadget_cooldown");
            if (!player.isOnGround()) {
                player.sendMessage("§cVocê precisa estar no chão para utilizar os balões!");
                return;
            }
            if (cooldown != null && !cooldown.expired()) {
                player.sendMessage("§cAguarde " + Constants.SIMPLE_DECIMAL_FORMAT.format(CooldownProvider.getGenericInstance().getCooldown(player.getUniqueId(), "gadget_cooldown").getRemaining()) + "s para usar novamente.");
                return;
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "gadget_cooldown", 30, false);
            player.sendMessage("§aForam lançados os balões!");
            BalloonObject balloonObj = new BalloonObject(player.getLocation());
            balloonObj.spawn();
        }
    }

    @EventHandler
    public void damage(PlayerInteractAtEntityEvent e) {
        if(e.getRightClicked().hasMetadata("balloon")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent e) {
        if(e.getEntity().hasMetadata("balloon")) {
            e.setCancelled(true);
        }
    }
}
