package com.minecraft.core.account.friend.status;

import com.minecraft.core.account.friend.Friend;
import com.minecraft.core.account.friend.FriendStatus;
import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class FriendStatusUpdate {

    private Friend holder;
    private Friend friendUpdated;
    private FriendStatus status;
    private Update update;

    public enum Update {

        STATUS;

    }

}
