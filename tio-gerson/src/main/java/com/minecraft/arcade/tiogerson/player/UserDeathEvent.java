package com.minecraft.arcade.tiogerson.player;

import com.minecraft.arcade.tiogerson.user.User;
import com.minecraft.core.bukkit.event.handler.ServerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeathEvent extends ServerEvent {

    private User user, killer;
    private boolean definitelyLeft;

}
