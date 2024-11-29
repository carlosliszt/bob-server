package com.minecraft.core.bukkit.accessory;

public enum AccessoryRarity {

    COMMON("§aComum"),
    UNCOMMON("§eIncomum"),
    RARE("§6Raro"),
    LEGENDARY("§5Lendário");

    private final String displayName;

    AccessoryRarity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
