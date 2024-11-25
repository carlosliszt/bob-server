package com.minecraft.core.party;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class Party {

    private final UUID uniqueId;

    @Getter
    @Setter
    private Set<UUID> members = new HashSet<>();

    private UUID owner;
    @Setter
    private boolean open;

    public void addMember(UUID member) {
        members.add(member);
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean isMember(UUID member) {
        return members.contains(member);
    }

    public boolean isOwner(UUID member) {
        return owner.equals(member);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

}
