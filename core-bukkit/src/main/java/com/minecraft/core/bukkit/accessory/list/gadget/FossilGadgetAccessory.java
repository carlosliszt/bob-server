package com.minecraft.core.bukkit.accessory.list.gadget;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.disguise.UndeadDisguise;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.enums.Rank;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FossilGadgetAccessory extends Accessory {

    public FossilGadgetAccessory() {
        super("TtLv2",
                "Transformador",
                "gadget.fossil.accessory",
                Rank.PRO,
                new ItemFactory().setType(Material.BONE).setName("§aTransformador de Mortos-Vivos!").getStack(),
                "Tranforme outros jogadores em esqueletos!",
                false,
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
        int fossilSlot = player.getInventory().first(Material.BONE);
        if (fossilSlot != -1) {
            player.getInventory().setItem(fossilSlot, new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player))
            return;

        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();

        Account account = Account.fetch(player.getUniqueId());

        if (player.getItemInHand() != null && player.getItemInHand().getType().equals(Material.BONE)) {
            Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "gadget_cooldown");
            if (!player.isOnGround()) {
                player.sendMessage("§cVocê precisa estar no chão para utilizar o transformador!");
                return;
            }
            if (cooldown != null && !cooldown.expired()) {
                player.sendMessage("§cAguarde " + Constants.SIMPLE_DECIMAL_FORMAT.format(CooldownProvider.getGenericInstance().getCooldown(player.getUniqueId(), "gadget_cooldown").getRemaining()) + "s para usar novamente.");
                return;
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "gadget_cooldown", 30, false);

            player.sendMessage("§aVocê transformou " + target.getName() +  " em um fóssil vivo!");
            target.sendMessage("§aVocê foi transformado por " + player.getName() + " em um fóssil vivo!");
            target.playSound(target.getLocation(), Sound.WITHER_HURT, 3.0f, 3.0f);

            UndeadDisguise dis = new UndeadDisguise(UndeadDisguise.DisguiseType.SKELETON, target);
            dis.disguiseToAll();
            new BukkitRunnable() {

                @Override
                public void run() {
                    dis.removeDisguise();
                }
            }.runTaskLater(BukkitGame.getEngine(), 1*60*20L);
        }
    }
}
