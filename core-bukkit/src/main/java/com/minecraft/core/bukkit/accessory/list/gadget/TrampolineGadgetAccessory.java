package com.minecraft.core.bukkit.accessory.list.gadget;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.gadget.runnable.TrampolineObject;
import com.minecraft.core.bukkit.accessory.list.gadget.runnable.TrampolineRunnable;
import com.minecraft.core.bukkit.arcade.map.area.Cuboid;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.cooldown.CooldownProvider;
import com.minecraft.core.bukkit.util.cooldown.type.Cooldown;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.npc.NPC;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.server.ServerCategory;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class TrampolineGadgetAccessory extends Accessory {

    public HashMap<UUID, TrampolineObject> trampolines = new HashMap<>();
    private TrampolineObject trampolineObjBlueprint = null;
    private final ItemFactory trampolineItemStack = new ItemFactory(Material.SLIME_BLOCK);

    public TrampolineGadgetAccessory() {
        super("PpKv9",
                "Pula-pula",
                "gadget.trampoline.accessory",
                Rank.PRO,
                new ItemStack(Material.SLIME_BLOCK),
                "Crie um pula-pula nos lobbies!",
                false,
                AccessoryType.GADGET,
                AccessoryRarity.LEGENDARY);

        this.trampolineObjBlueprint = new TrampolineObject();
    }

    @Override
    public void updateAsync(Player player, ServerHeartbeatEvent event) { }

    @Override
    public void give(Player player) {
        int slot = 2;
        player.getInventory().setItem(slot, new ItemStack(Material.AIR));
        trampolineItemStack.setName(getRarity().getDisplayName() + getName());
        player.getInventory().setItem(slot, trampolineItemStack.getStack());
    }

    @Override
    public void remove(Player player) {
        int trampolineSlot = player.getInventory().first(Material.SLIME_BLOCK);
        if (trampolineSlot != -1) {
            player.getInventory().setItem(trampolineSlot, new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.isBlockInHand()) {
            return;
        }
        if (!event.hasItem()) {
            return;
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.ARMOR_STAND)) {
            return;
        }
        if (!event.getItem().getType().equals(Material.SLIME_BLOCK) && !event.getItem().isSimilar(trampolineItemStack.getStack()) && !Constants.getServerCategory().equals(ServerCategory.LOBBY)) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && player.getItemInHand().getType().equals(Material.SLIME_BLOCK)) {
            Cooldown cooldown = CooldownProvider.getGenericInstance().getCooldown(event.getPlayer().getUniqueId(), "gadget_cooldown");
            if (!player.isOnGround()) {
                player.sendMessage("§cVocê precisa estar no chão para utilizar o trampolim!");
                return;
            }

            if (cooldown != null && !cooldown.expired()) {
                player.sendMessage("§cAguarde " + Constants.SIMPLE_DECIMAL_FORMAT.format(CooldownProvider.getGenericInstance().getCooldown(player.getUniqueId(), "gadget_cooldown").getRemaining()) + "s para usar novamente.");
                return;
            }

            if(trampolines.containsKey(player.getUniqueId())) {
                player.sendMessage("§cVocê deve esperar o pula-pula sumir para usar novamente.");
                return;
            }

            Location location1 = player.getLocation().clone().add(-3.0, 0, -3.0),
                    location2 = player.getLocation().clone().add(3.0, 0, 3.0);
            Block stairs1 = player.getLocation().clone().getBlock().getRelative(-4, 1, 0), stairs2 = player.getLocation().clone().getBlock().getRelative(-5, 0, 0);
            Cuboid cuboid = new Cuboid(location1, location2);
            boolean empty = true;
            for (Block block : cuboid.getBlocks(player.getWorld())) {
                if (!block.getType().equals(Material.AIR)) {
                    empty = false;
                    break;
                }
            }

            CooldownProvider.getGenericInstance().addCooldown(player.getUniqueId(), "gadget_cooldown", 30, false);
            if (empty && stairs1.getType().equals(Material.AIR) && stairs2.getType().equals(Material.AIR)) {
                Location loc = event.getClickedBlock().getRelative(BlockFace.UP).getLocation().subtract(0, 4, 0);

                TrampolineObject trampolineObj = trampolineObjBlueprint.clone();

                player.sendMessage("§aPula-pula spawnado!");
                trampolineObj.spawn(loc);
                trampolines.put(uuid, trampolineObj);

                long endTime = System.currentTimeMillis() + (60 * 1000L);
                new TrampolineRunnable(this, uuid, trampolineObj, endTime).runTaskTimer(BukkitGame.getEngine(), 0L, 3L);
            } else {
                player.sendMessage("§cPara spawnar o trampolin, precisa ser em uma área vazia!");
            }
        }
    }
}
