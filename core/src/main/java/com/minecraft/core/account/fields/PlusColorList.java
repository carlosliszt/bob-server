package com.minecraft.core.account.fields;

import com.minecraft.core.account.Account;
import com.minecraft.core.enums.PlusColor;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PlusColorList {

    private final Account account;
    @Getter
    private final List<PlusColor> plusColor = new ArrayList<>();

    public void loadPlusColor() {
        plusColor.clear();
        for (PlusColor color : PlusColor.getValues()) {
            if (account.hasTag(Tag.ULTRA_PLUS_3) || account.hasPermission(Rank.ADMINISTRATOR) || account.hasPlusColor(color) || color == PlusColor.GOLDEN) {
                plusColor.add(color);
            }
        }
    }

    public PlusColor getHighestPlusColor() {
        return getPlusColor().get(0);
    }

    public boolean hasPlusColor(PlusColor plusColor) {
        return getPlusColor().contains(plusColor);
    }

}