package com.minecraft.core.bukkit.accessory.list.gadget.runnable;

import com.minecraft.core.bukkit.accessory.list.gadget.TrampolineGadgetAccessory;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@AllArgsConstructor
public class TrampolineRunnable extends BukkitRunnable {

    private TrampolineGadgetAccessory gadget;
    private UUID uuid;
    private TrampolineObject trampolineObj;
    private Long endTime = -1L;

    @Override
    public void run() {
        if(System.currentTimeMillis() >= endTime) {
            trampolineObj.destroy();
            gadget.trampolines.remove(uuid);
            cancel();
            return;
        }
        for(Entity entity : trampolineObj.getLoc().getWorld().getNearbyEntities(trampolineObj.getLoc(), 8D, 8D, 8D)) {
            if(!(entity instanceof Player))
                continue;

            Player p = (Player) entity;
            Block block = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if(block.getType() != Material.WOOL)
                continue;

            if(!trampolineObj.getWoolBlocks().contains(block))
                continue;

            p.setVelocity(p.getVelocity().setY(3.5D));

        }
    }

}
