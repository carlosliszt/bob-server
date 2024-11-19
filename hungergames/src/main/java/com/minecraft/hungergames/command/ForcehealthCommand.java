package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class ForcehealthCommand implements Assistance, BukkitInterface {

    @Command(name = "forcehealth", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS, usage = "{label} <target...> <health>")
    public void forcehealthCommand(Context<Player> context, String rawUsers, double health) {

        if (health < 0.5) {
            context.sendMessage("§cO parâmetro 'health' deve ser maior que 0.5");
            return;
        }

        if (health > 10)
            health = 10;

        AtomicInteger affected = new AtomicInteger();
        final double final_health = health * 2;

        getUsers(",", rawUsers).forEach(user -> {
            user.getPlayer().setHealth(final_health);
            affected.getAndIncrement();
        });

        context.info("command.forcehealth.affected_users", health, affected.get());
    }

}