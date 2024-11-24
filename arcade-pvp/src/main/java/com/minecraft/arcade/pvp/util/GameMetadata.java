/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.arcade.pvp.util;

import com.minecraft.arcade.pvp.PvP;
import org.bukkit.metadata.FixedMetadataValue;

public class GameMetadata extends FixedMetadataValue {

    public GameMetadata(Object value) {
        super(PvP.getInstance(), value);
    }

}