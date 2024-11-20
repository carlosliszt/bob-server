package com.minecraft.core.clan.communication;

import com.minecraft.core.clan.member.Member;
import lombok.*;

@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class ClanIntegrationMessage {

    private Member target;
    private int index = -1;
    private String clanName = "?";
    private String clanTag = "?";
    private int cost;
    private String color;
    private MessageCause messageCause;

    public enum MessageCause {

        CREATION, DISBAND, MEMBER_JOIN,
        MEMBER_LEFT, TRANSFER;

    }
}
