/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account;

public abstract class AccountExecutor {
    public abstract void sendMessage(String message);

    public void sendPluginMessage(String channel, byte[] bytes) {
    }
}
