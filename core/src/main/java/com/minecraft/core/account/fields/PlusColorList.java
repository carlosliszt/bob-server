package com.minecraft.core.account.fields;

import com.minecraft.core.account.Account;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.PlusColor;
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
            if (account.hasTag(Tag.BLAZE_PLUS_3) || account.hasPlusColor(color) || account.getData(Columns.ULTRA_PLUS_MONTHS).getAsInt() >= color.getMonths() || color == PlusColor.GOLDEN) {
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