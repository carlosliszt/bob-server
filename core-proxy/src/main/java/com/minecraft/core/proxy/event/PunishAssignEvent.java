/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.event;

import com.minecraft.core.account.Account;
import com.minecraft.core.punish.Punish;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Event;

@Data
@ToString()
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class PunishAssignEvent extends Event {

    private Account account;
    private Punish punish;

}