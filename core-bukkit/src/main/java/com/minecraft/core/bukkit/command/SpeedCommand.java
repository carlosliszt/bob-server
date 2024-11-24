/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedCommand implements BukkitInterface {

    public SpeedCommand() {
        BukkitGame.getEngine().getBukkitFrame().registerAdapter(SpeedType.class, SpeedType::getType);
    }

    @Command(name = "speed", platform = Platform.PLAYER, rank = Rank.TRIAL_MODERATOR, usage = "speed <type> <value> [player]")
    public void handleCommand(Context<Player> context, SpeedType speedType, float value) {

        if (speedType == null) {
            context.info("object.not_found", "Type");
            return;
        }

        if (value < 0 || value > 1) {
            context.info("invalid_number", 0, 1);
            return;
        }

        Player target = context.getSender();

        if (context.argsCount() > 2) {
            target = Bukkit.getPlayer(context.getArg(2));

            if (target == null || !context.getSender().canSee(target)) {
                context.info("target.not_found");
                return;
            }
        }
        speedType.getApplier().apply(target, value);

        if (target == context.getSender())
            context.info("command.speed.changed_yourself", speedType.getId().toLowerCase(), value);
        else
            context.info("command.speed.changed_other", speedType.getId().toLowerCase(), value, target.getName());
    }

    @Completer(name = "speed")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            return Arrays.stream(SpeedType.values()).filter(argument -> argument.getId() != null && startsWith(argument.getId(), context.getArg(0))).map(SpeedType::getId).collect(Collectors.toList());
        }
        return getOnlineNicknames(context);
    }

    @AllArgsConstructor
    @Getter
    protected enum SpeedType {
        WALKING("walk", Player::setWalkSpeed),
        FLYING("fly", Player::setFlySpeed);

        private final String id;
        private final Applier applier;

        @Override
        public String toString() {
            return this.id;
        }

        public static SpeedType getType(String arg) {
            return Arrays.stream(values()).filter(c -> c.getId().equalsIgnoreCase(arg)).findFirst().orElse(null);
        }

        protected interface Applier {
            void apply(Player player, float value);
        }
    }

}