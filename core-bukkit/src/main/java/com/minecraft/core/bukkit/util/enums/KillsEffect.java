package com.minecraft.core.bukkit.util.enums;

import com.minecraft.core.enums.Rank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

@Getter @RequiredArgsConstructor
public enum KillsEffect {

    NONE(0, "Nenhuma", "", Rank.MEMBER, Material.BARRIER),
    FIREWORK(1, "Fogos de Artifício", "Solta fogos de artifício", Rank.VIP, Material.FIREWORK),
    TRAMPOLINE(2, "Trampoline", "Pule para sua vitória em seu trampolim.", Rank.BETA, Material.WOOL),
    EXPLOSION(3, "Explosão", "Boom!", Rank.VIP, Material.getMaterial(289));

    private final int id;
    private final String name;
    private final String description;
    private final Rank requires;
    private final Material item;

    @Getter
    private static final KillsEffect[] values;

    static {
        values = values();
    }

    public static KillsEffect fromId(int data) {
        return Arrays.stream(getValues()).filter(effect -> effect.getId() == data).findFirst().orElse(null);
    }
}
