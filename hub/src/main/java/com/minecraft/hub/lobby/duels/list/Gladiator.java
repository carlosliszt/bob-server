package com.minecraft.hub.lobby.duels.list;

import com.minecraft.core.Constants;
import com.minecraft.core.server.ServerType;
import com.minecraft.hub.Hub;
import com.minecraft.hub.lobby.Lobby;
import com.minecraft.hub.user.User;
import org.bukkit.entity.Player;

public class Gladiator extends Lobby {

    public Gladiator(Hub hub) {
        super(hub, true);

        Constants.setServerType(ServerType.GLADIATOR_LOBBY);
        Constants.setLobbyType(ServerType.DUELS_LOBBY);

        setBossbar("§b§lGLADIATOR NO BOBMC.COM");
    }

    @Override
    public void handleScoreboard(User user, String displayName) {

    }

    @Override
    public void handleJoin(Player player) {

    }

}