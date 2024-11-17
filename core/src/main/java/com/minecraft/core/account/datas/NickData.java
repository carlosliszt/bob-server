package com.minecraft.core.account.datas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NickData {

    private String nickname;
    private long dateChanged, expiry;

    public boolean hasExpired() {
        return System.currentTimeMillis() > expiry;
    }

}
