/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Optional;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SetmotdCommand implements ProxyInterface {

    @Command(name = "setmotd", rank = Rank.ADMINISTRATOR)
    public void handleCommand(Context<CommandSender> context, @Optional(def = "&a&lVENHA JOGAR!") String[] motd) {
        try {
            Configuration configuration = ProxyGame.getInstance().getConfiguration();
            configuration.set("motd", String.join(" ", motd).replace("&", "§"));
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(ProxyGame.getInstance().getDataFolder(), "config.yml"));
            context.info("command.setmotd.execution_successful");
        } catch (IOException e) {
            e.printStackTrace();
            context.info("command.setmotd.error");
        }
    }

}
