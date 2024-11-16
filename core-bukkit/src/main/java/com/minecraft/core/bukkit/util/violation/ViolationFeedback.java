/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.violation;

public class ViolationFeedback {

    private final String packet;
    private final String explanation;
    private final boolean cancelOnly;
    private final boolean surelyExploit;

    public ViolationFeedback(final String packet, final String explanation, final boolean cancelOnly, final boolean surelyExploit) {
        this.packet = packet;
        this.explanation = explanation;
        this.cancelOnly = cancelOnly;
        this.surelyExploit = surelyExploit;
    }

    public boolean isCancelOnly() {
        return this.cancelOnly;
    }

    public boolean isSurelyExploit() {
        return surelyExploit;
    }

    @Override
    public String toString() {
        return "[" +
                "packet='" + packet + '\'' +
                ", explanation='" + explanation + '\'' +
                ", cancelOnly=" + cancelOnly +
                ", surelyExploit=" + surelyExploit +
                ']';
    }
}
