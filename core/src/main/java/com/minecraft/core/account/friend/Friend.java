package com.minecraft.core.account.friend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Friend {

    private String name;
    private UUID uniqueId;
    private long addedAt;

}
