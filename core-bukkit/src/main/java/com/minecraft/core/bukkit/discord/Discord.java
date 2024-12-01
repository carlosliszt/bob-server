/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.discord;

import com.minecraft.core.Constants;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Getter
public class Discord {

    private JDA JDA;

    public Discord start(String token) {
         try {
             JDA = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS).setAutoReconnect(true).build();
            JDA.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            JDA.getPresence().setPresence(Activity.playing(Constants.SERVER_STORE), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void log(EmbedBuilder embedBuilder) {
        TextChannel txt = getJDA().getTextChannelById("1310230222506950676");
        if (txt != null)
            txt.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public void shutdown() {
        JDA.shutdown();
    }
}
