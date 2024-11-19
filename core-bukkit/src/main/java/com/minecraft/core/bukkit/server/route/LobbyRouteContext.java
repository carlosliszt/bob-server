package com.minecraft.core.bukkit.server.route;

import com.minecraft.core.bukkit.server.lobby.LobbyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LobbyRouteContext {

    private UUID target;
    private PlayMode playMode;
    private LobbyType lobbyType;

    private boolean hasTarget() {
        return target != null;
    }

}
