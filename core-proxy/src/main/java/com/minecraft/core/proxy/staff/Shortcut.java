package com.minecraft.core.proxy.staff;

import com.minecraft.core.punish.PunishType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Shortcut {

    private PunishType punishType;
    private String shortcut, name, fullCommand;

}
