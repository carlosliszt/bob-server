/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.enums;

import com.minecraft.core.translation.Language;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

@Getter
public enum Effects {

    SPEED(PotionEffectType.SPEED, "Speed", "Velocidade"),
    SLOW(PotionEffectType.SLOW, "Slowness", "Lentidão"),
    FAST_DIGGING(PotionEffectType.FAST_DIGGING, "Haste", "Rapidez"),
    SLOW_DIGGING(PotionEffectType.SLOW_DIGGING, "Mining Fatigue", "Cansaço"),
    INCREASE_DAMAGE(PotionEffectType.INCREASE_DAMAGE, "Strenght", "Força"),
    HEAL(PotionEffectType.HEAL, "Instant Health", "Vida Instantânea"),
    HARM(PotionEffectType.HARM, "Instant Damage", "Dano Instantâneo"),
    JUMP(PotionEffectType.JUMP, "Jump Boost", "Super Pulo"),
    CONFUSION(PotionEffectType.CONFUSION, "Confusion", "Naúsea"),
    REGENERATION(PotionEffectType.REGENERATION, "Regeneration", "Regeneração"),
    DAMAGE_RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE, "Resistance", "Resistência"),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, "Fire Resistance", "Resistência ao fogo"),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING, "Water Breathing", "Respiração Aquatica"),
    INVISIBILITY(PotionEffectType.INVISIBILITY, "Invisibility", "Invisibilidade"),
    BLINDNESS(PotionEffectType.BLINDNESS, "Blindness", "Cegueira"),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION, "Night vision", "Visão Noturna"),
    HUNGER(PotionEffectType.HUNGER, "Hunger", "Fome"),
    WEAKNESS(PotionEffectType.WEAKNESS, "Weakness", "Fraqueza"),
    POISON(PotionEffectType.POISON, "Poison", "Veneno"),
    WITHER(PotionEffectType.WITHER, "Wither", "Decomposição"),
    HEALTH_BOOST(PotionEffectType.HEALTH_BOOST, "Health Boost", "Vida Extra"),
    ABSORPTION(PotionEffectType.ABSORPTION, "Absorption", "Absorção"),
    SATURATION(PotionEffectType.SATURATION, "Saturation", "Saturação");

    private final PotionEffectType effectType;
    private final String enName, ptName;

    Effects(PotionEffectType effectType, String enName, String ptName) {
        this.effectType = effectType;
        this.enName = enName;
        this.ptName = ptName;
    }

    public String getName(Language language) {
        return language == Language.PORTUGUESE ? getPtName() : getEnName();
    }

    public static Effects fromPotionEffectType(PotionEffectType effectType) {
        return Arrays.stream(values()).filter(effects -> effects.getEffectType().equals(effectType)).findFirst().orElse(null);
    }

}