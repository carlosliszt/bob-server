/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.variable;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.variable.object.SimpleVariable;

public interface VariableStorage {

    default void loadVariables() {
        BukkitGame.getEngine().getVariableLoader().load(this);
    }

    default SimpleVariable getVariable(String name) {
        return BukkitGame.getEngine().getVariableLoader().getVariable(name);
    }

}
