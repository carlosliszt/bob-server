package com.minecraft.core.account.datas;

import com.minecraft.core.enums.Medal;
import com.minecraft.core.enums.PlusColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlusColorData {

    private final PlusColor plusColor;
    private final String addedBy;
    private long addedAt, expiration;

    public boolean hasExpired() {
        return !isPermanent() && expiration < System.currentTimeMillis();
    }

    public boolean isPermanent() {
        return expiration == -1;
    }

}
