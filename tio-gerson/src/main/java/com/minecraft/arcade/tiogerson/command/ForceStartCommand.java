package com.minecraft.arcade.tiogerson.command;

import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.arcade.tiogerson.util.enums.RoomStage;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.entity.Player;

public class ForceStartCommand {

    @Command(name = "forcestart", rank = Rank.ADMINISTRATOR, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> ctx) {
        User user = User.fetch(ctx.getSender().getUniqueId());

        if (user.getRoom().getStage() != RoomStage.PLAYING) {
            ctx.sendMessage("§dWoosh!");
            user.getRoom().start();
        }
    }
}
