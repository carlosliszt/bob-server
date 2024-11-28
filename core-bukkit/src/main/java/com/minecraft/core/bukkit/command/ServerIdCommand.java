package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import org.bukkit.command.CommandSender;

public class ServerIdCommand {

    @Command(name = "serverid")
    public void onCommand(Context<CommandSender> context) {
        context.sendMessage("§eNome: §b" + Constants.getServerStorage().getLocalServer().getName());
        context.sendMessage("§eCategoria: §b" + Constants.getServerStorage().getLocalServer().getServerCategory().getName());
        context.sendMessage("§eInstância: §b" + BukkitGame.getEngine().getInstanceId());
    }

}
