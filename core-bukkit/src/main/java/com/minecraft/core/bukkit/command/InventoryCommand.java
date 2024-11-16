/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.InventoryWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.List;

public class InventoryCommand implements BukkitInterface {

    @Command(name = "inventory", aliases = {"inv", "invsee"}, usage = "{label} <target>", platform = Platform.PLAYER, rank = Rank.STREAMER_PLUS)
    public void handleCommand(Context<Player> context, Player target) {
        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        Inventory inv = target.getInventory();
        EntityPlayer player = ((CraftPlayer) sender).getHandle();
        IInventory iinventory = (inv instanceof CraftInventory) ? ((CraftInventory) inv).getInventory() : new InventoryWrapper(inv);

        if (player.activeContainer == player.defaultContainer) {
            Container container = new ContainerChest(player.inventory, iinventory, player);
            int containerCounter = player.nextContainerCounter();

            IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + target.getName() + "\"}");
            player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, "minecraft:container", titleComponent, iinventory.getSize()));
            player.activeContainer = container;

            player.activeContainer.windowId = containerCounter;
            player.activeContainer.addSlotListener(player);
            player.activeContainer.checkReachable = false;
        }

        Account account = context.getAccount();
        log(account, account.getDisplayName() + " abriu o inventário de " + target.getName());
    }

    @Completer(name = "inventory")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

}