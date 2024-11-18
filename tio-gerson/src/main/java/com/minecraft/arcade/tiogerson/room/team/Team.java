package com.minecraft.arcade.tiogerson.room.team;

import com.minecraft.arcade.tiogerson.room.Room;
import com.minecraft.arcade.tiogerson.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class Team {

    private final Set<User> members = new HashSet<>();
    private ChatColor chatColor;
    private Room holder;
    private int maxPlayers;

    public boolean isFull() {
        int max = maxPlayers / 2;
        return members.size() == max;
    }

    @Override
    public String toString() {
        return "Team{" +
                "members=" + members +
                ", isFull=" + isFull() +
                ", room=" + holder.getCode() +
                ", maxPlayers=" + maxPlayers +
                '}';
    }
}
