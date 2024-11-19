package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.arcade.game.GameQuantity;
import com.minecraft.core.bukkit.arcade.game.GameType;
import com.minecraft.core.bukkit.arcade.route.GameRouteContext;
import com.minecraft.core.bukkit.arcade.route.JoinMode;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.payload.ServerRedirect;
import com.minecraft.core.server.Server;
import org.bukkit.command.CommandSender;

public class ArcadeDevCommand implements BukkitInterface {

    public ArcadeDevCommand() {
        BukkitFrame frame = BukkitGame.getEngine().getBukkitFrame();
        frame.registerAdapter(GameType.class, GameType::fetch);
        frame.registerAdapter(GameQuantity.class, GameQuantity::fetch);
    }

    @Command(name = "arcadedev", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<CommandSender> context, GameType gameType, GameQuantity gameQuantity, int mapId) {
        final Server server = Constants.getServerStorage().getServer("arcadedev");

        if (server == null || server.isDead()) {
            context.info("no_server_available", "arcadedev");
            return;
        }

        GameRouteContext gameRouteContext = GameRouteContext.builder().type(gameType).quantity(gameQuantity).mode(JoinMode.PLAYER).map(mapId).slots(1).build();
        context.getAccount().connect(new ServerRedirect(context.getUniqueId(), new ServerRedirect.Route(server, Constants.GSON.toJson(gameRouteContext))));

    }

}