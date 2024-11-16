/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.arena;

import com.minecraft.hungergames.util.bo3.BO3;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
@NoArgsConstructor
public abstract class Arena {

    private int size;
    private int height;

    public Arena(int size, int height) {
        this.size = size;
        this.height = height;
    }

    public abstract void spawn(Location location, BO3.BlockHandle blockHandle);

    @Override
    public String toString() {
        return "Arena{" +
                "size=" + size +
                ", height=" + height +
                '}';
    }
}
