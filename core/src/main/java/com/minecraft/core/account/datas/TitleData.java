package com.minecraft.core.account.datas;


import com.minecraft.core.cosmetic.Title;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TitleData {

    private final Title title;
    private final String addedBy;
    private long addedAt, updatedAt, expiration;

    public boolean hasExpired() {
        return !isPermanent() && expiration < System.currentTimeMillis();
    }

    public boolean isPermanent() {
        return expiration == -1;
    }

}
