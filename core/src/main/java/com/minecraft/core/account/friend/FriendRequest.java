package com.minecraft.core.account.friend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class FriendRequest {

    private String receiverName;
    private UUID receiver;
    private Status status;
    private String senderName;
    private UUID senderUniqueId;

    @RequiredArgsConstructor
    @Getter
    public enum Status {

        PENDING("§fPendente"), DECLINED("§cRecusado"), ACCEPTED("§aAceito");

        private final String name;

    }

}
