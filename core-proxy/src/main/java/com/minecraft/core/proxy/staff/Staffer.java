package com.minecraft.core.proxy.staff;

import com.minecraft.core.account.Account;
import com.minecraft.core.proxy.ProxyGame;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@Getter
public class Staffer {

    private final UUID uniqueId;
    private final String name;

    private final Account account;

    private ProxiedPlayer player;

    @Setter
    private String lastGo, current;

    public Staffer(Account account) {
        this.account = account;

        this.uniqueId = account.getUniqueId();
        this.name = account.getUsername();
    }

    public static Staffer fetch(UUID uuid) {
        return ProxyGame.getInstance().getStaffStorage().getUser(uuid);
    }

    public ProxiedPlayer getPlayer() {
        if (this.player == null)
            this.player = BungeeCord.getInstance().getPlayer(getUniqueId());
        return player;
    }

}
