package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SetplayerlimitCommand implements ProxyInterface {

    @Command(name = "setplayerlimit", usage = "setplayerlimit <maxPlayers>", platform = Platform.BOTH, rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<CommandSender> context, int maxPlayers) {
        try {
            ProxyServer proxyServer = ProxyGame.getInstance().getProxy();
            Class<?> configClass = proxyServer.getConfig().getClass();

            if (!configClass.getSuperclass().equals(Object.class)) {
                configClass = configClass.getSuperclass();
            }

            Field playerLimitField = configClass.getDeclaredField("playerLimit");
            playerLimitField.setAccessible(true);
            playerLimitField.setInt(proxyServer.getConfig(), maxPlayers);

            Method setMethod = proxyServer.getConfigurationAdapter().getClass().getDeclaredMethod("set", String.class, Object.class);
            setMethod.setAccessible(true);
            setMethod.invoke(proxyServer.getConfigurationAdapter(), "player_limit", maxPlayers);

            context.info("command.setplayerlimit.execution_successful", maxPlayers);

            ProxyGame.getInstance().getDiscord().log(new EmbedBuilder()
                    .setTitle("Limite de jogadores alterado")
                    .setDescription("O limite de jogadores foi alterado para " + maxPlayers)
                    .setColor(0x00FF00)
                    );

        } catch (Exception e) {
            e.printStackTrace();
            context.info("command.setplayerlimit.error");
        }
    }

}