package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.command.CommandSender;

public class CensorCheckCommand implements BukkitInterface {

    @Command(name = "censorcheck", platform = Platform.BOTH, rank = Rank.PARTNER_PLUS, usage = "{label} <input>")
    public void handleCommand(Context<CommandSender> context, String[] input) {
        String output = BukkitGame.getEngine().getWordCensor().filter(createArgs(0, input, "null", false)).replace("*", "§c*§f");
        context.sendMessage("§8Output> §f" + output);
    }
}
