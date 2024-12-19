package com.minecraft.core.account.blocked;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Blocked {

    private String name;
    private UUID uniqueId;

}
