package com.minecraft.core.account.fields;

import com.minecraft.core.account.Account;
import com.minecraft.core.cosmetic.Title;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TitleList {

    private final Account account;
    @Getter
    private final List<Title> titles = new ArrayList<>();

    public void loadTitles() {
        titles.clear();

        for (Title title : Title.values()) {
            if (!title.isExclusive() && account.hasPermission(title.getRank()) || title.isExclusive() && account.hasPermission(Rank.ADMINISTRATOR) /* || account.hasTitle(title) */)
                titles.add(title);
        }
    }

    public boolean hasTitle(Title title) {
        return getTitles().contains(title);
    }

}
