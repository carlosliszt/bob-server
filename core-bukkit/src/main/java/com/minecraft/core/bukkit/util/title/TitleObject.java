package com.minecraft.core.bukkit.util.title;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@AllArgsConstructor
public class TitleObject {

    private Player owner;
    private String display;
    private List<EntityArmorStand> armorStands;

}
