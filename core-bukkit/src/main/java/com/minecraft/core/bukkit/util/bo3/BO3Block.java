package com.minecraft.core.bukkit.util.bo3;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BO3Block {

    protected int x, y, z;
    protected Material material;
    protected byte data;

    public Material getType() {
        return material;
    }

}