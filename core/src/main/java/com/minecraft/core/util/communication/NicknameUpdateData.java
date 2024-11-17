package com.minecraft.core.util.communication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class NicknameUpdateData {

    private final UUID uniqueId;
    private final String nickname;
    private final long changedAt, expiry;

}
