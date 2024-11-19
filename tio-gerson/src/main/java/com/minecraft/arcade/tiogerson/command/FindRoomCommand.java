package com.minecraft.arcade.tiogerson.command;

import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.core.bukkit.server.route.PlayMode;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import org.bukkit.entity.Player;

public class FindRoomCommand {

    @Command(name = "findroom", usage = "{label} <input>", rank = Rank.TRIAL_MODERATOR)
    public void handleCommand(Context<Player> ctx, String id) {
        if (ArcadeMain.getInstance().getRoomStorage().getRoomById(id) == null) {
            ctx.sendMessage("§cSala não encontrada.");
            return;
        }

        Room room = ArcadeMain.getInstance().getRoomStorage().getRoomById(id);
        ctx.sendMessage("§aConectando à sala " + room.getCode() + " de " + room.getMode().getName() + "...");

        room.join(User.fetch(ctx.getUniqueId()), PlayMode.VANISH, true);

    }

}
