package com.minecraft.core.bukkit.accessory;

public enum AccessoryType {

    NULL("Acessórios"),
    GADGET("Engenhocas"),
    HEAD("Cabeças"),
    HATS("Chapéus"),
    PARTICLES("Partículas"),
    EMOTIONS("Emoções"),
    ARMORS("Armaduras"),
    FLAGS("Bandeiras"),
    WINGS("Asas Personalizadas"),
    TITLES("Títulos");

    private final String displayName;

    AccessoryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
