package com.minecraft.core.party;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PartyInvite {

    private final UUID sender;
    private final UUID receiver;
    private final long timestamp;
    private Party party;

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 30000;
    }

}
