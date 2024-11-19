package com.minecraft.core.clan.member.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {

    OWNER(3, "§4Líder"),
    ADMINISTRATOR(2, "§5Gerente"),
    MEMBER(1, "§7Membro");

    private final int id;
    private final String display;

}
