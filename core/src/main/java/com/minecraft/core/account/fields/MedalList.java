/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.fields;

import com.minecraft.core.account.Account;
import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MedalList {

    private final Account account;
    @Getter
    private final List<Medal> medals = new ArrayList<>();

    public void loadMedals() {
        medals.clear();
        for (Medal medal : Medal.getValues()) {
            if (!medal.isDedicated() && account.hasPermission(medal.getRank()) || medal.isDedicated() && account.hasPermission(Rank.ADMINISTRATOR) || account.hasMedal(medal))
                medals.add(medal);
        }
    }

    public Medal getHighestMedal() {
        return getMedals().get(0);
    }

    public boolean hasMedal(Medal medal) {
        return getMedals().contains(medal);
    }

}
