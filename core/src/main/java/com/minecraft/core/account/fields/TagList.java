/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.account.fields;

import com.minecraft.core.account.Account;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TagList {

    private final Account account;
    @Getter
    private final List<Tag> tags = new ArrayList<>();

    public void loadTags() {
        tags.clear();

        for (Tag tag : Tag.getValues()) {
            if (!tag.isDedicated() && account.hasPermission(tag.getDefaultRank()) || tag.isDedicated() && account.getRank().getDefaultTag().getId() == tag.getId() || tag.isDedicated() && account.hasPermission(Rank.ADMINISTRATOR) || account.hasTag(tag))
                tags.add(tag);
        }
    }

    public Tag getHighestTag() {
        return getTags().get(0);
    }

    public boolean hasTag(Tag tag) {
        return getTags().contains(tag);
    }

}
