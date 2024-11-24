/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.event.player;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.handler.AccountEvent;
import com.minecraft.core.enums.PrefixType;
import com.minecraft.core.enums.Tag;

public class PlayerUpdateTablistEvent extends AccountEvent {

    private final Tag tag;
    private final PrefixType prefixType;

    public PlayerUpdateTablistEvent(Account account, Tag tag, PrefixType prefixType) {
        super(account);
        this.tag = tag;
        this.prefixType = prefixType;
    }

    public Tag getTag() {
        return tag;
    }

    public PrefixType getPrefixType() {
        return prefixType;
    }

}