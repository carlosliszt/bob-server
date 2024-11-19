package com.minecraft.arcade.tiogerson.command;

import com.minecraft.arcade.tiogerson.ArcadeMain;
import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.command.CommandSender;

public class DemandCommand {

    @Command(name = "checkdemand", platform = Platform.BOTH, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<CommandSender> context) {

        context.sendMessage("§eTotal de salas: §b" + ArcadeMain.getInstance().getRoomStorage().getBusy().size() + "§e/§b" + ArcadeMain.getInstance().getRoomStorage().getRooms().size());
        for (Room room : ArcadeMain.getInstance().getRoomStorage().getRooms().values()) {
            context.sendMessage("§eSala: §b" + room.getCode() + " §e- Modo: §b" + room.getMode().getName() + " §e- Jogadores: §b" + room.getAlivePlayers().size() + "§e/§b" + room.getMaxPlayers() + " §e- Status: §b" + room.getStage().name());
        }

    }
}
