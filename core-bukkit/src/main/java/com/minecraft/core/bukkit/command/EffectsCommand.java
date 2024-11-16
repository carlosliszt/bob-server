/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.enums.Effects;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.translation.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EffectsCommand implements BukkitInterface {

    @Command(name = "effects", platform = Platform.PLAYER, rank = Rank.STREAMER_PLUS)
    public void handleCommand(Context<Player> context, Player target) {
        Player sender = context.getSender();

        if (target == null || !sender.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        Account account = context.getAccount();
        Language lang = account.getLanguage();

        Collection<PotionEffect> activePotionEffects = target.getActivePotionEffects();

        if (activePotionEffects.isEmpty()) {
            context.info("command.effects.empty");
            return;
        }

        sender.sendMessage("§a" + (lang == Language.PORTUGUESE ? "Efeitos ativos de " + target.getName() + ":" : target.getName() + "'s active effects:"));
        activePotionEffects.forEach(potionEffect -> {
            int time = potionEffect.getDuration() / 20;
            Effects effects = Effects.fromPotionEffectType(potionEffect.getType());
            sender.sendMessage(" §f- §7" + effects.getName(lang) + ' ' + numural((potionEffect.getAmplifier() + 1)) + ": §f" + (potionEffect.getDuration() > 2147000 ? "Infinito" : format(time)));
        });
    }

    @Completer(name = "effects")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

}