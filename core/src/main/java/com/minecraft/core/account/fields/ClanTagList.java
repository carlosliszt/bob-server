/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.fields;

import com.minecraft.core.account.Account;
import com.minecraft.core.enums.Clantag;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ClanTagList {

    private final Account account;
    @Getter
    private final List<Clantag> clanTags = new ArrayList<>();

    public void loadClanTags() {
        clanTags.clear();
        for (Clantag clantag : Clantag.getValues()) {
            if (!clantag.isDedicated() && account.hasPermission(clantag.getRank()) || clantag.isDedicated() && account.hasPermission(Rank.ADMINISTRATOR) || account.hasClanTag(clantag))
                clanTags.add(clantag);
        }
    }

    public Clantag getHighestClanTag() {
        return getClanTags().get(0);
    }

    public boolean hasTag(Clantag clantag) {
        return getClanTags().contains(clantag);
    }

}
