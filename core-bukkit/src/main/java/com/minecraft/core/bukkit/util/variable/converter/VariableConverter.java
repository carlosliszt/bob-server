/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.variable.converter;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.command.BukkitFrame;
import com.minecraft.core.bukkit.util.variable.object.SimpleVariable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VariableConverter {

    public static String convertParameterTypeToName(final SimpleVariable varData) {
        return convertParameterTypeToName(varData.getField().getType());
    }

    public static String convertParameterTypeToName(final Class<?> parameter) {
        if (parameter.getSimpleName().equals("boolean")) {
            return "true ou false";
        }
        if (parameter.getSimpleName().equals("int")) {
            return "número inteiro";
        }
        if (parameter.getSimpleName().equals("long")) {
            return "número inteiro";
        }
        if (parameter.getSimpleName().equals("float")) {
            return "número decimal";
        }
        if (parameter.getSimpleName().equals("double")) {
            return "número decimal";
        }
        if (parameter.equals(String.class)) {
            return "texto";
        }
        if (parameter.equals(Location.class)) {
            return "localização atual";
        }
        if (parameter.equals(Player.class)) {
            return "jogador";
        }
        if (parameter.equals(Account.class)) {
            return "jogador";
        }
        return parameter.getSimpleName();
    }

    public static String convertCurrentValueToName(final Object value) {
        if (value == null) {
            return "Vazio";
        }
        if (value.getClass().isAssignableFrom(Location.class)) {
            final Location location = (Location) value;
            return String.format("(%s, %s, %s)", location.getX(), location.getY(), location.getZ());
        }
        if (value.getClass().isAssignableFrom(Player.class)) {
            return ((Player) value).getName();
        }
        if (value.getClass().isAssignableFrom(Account.class)) {
            return ((Account) value).getDisplayName();
        }
        return value.toString();
    }

    public static Object convertToObject(final Player player, final Class<?> parameter, final String value) {
        BukkitFrame bukkitFrame = BukkitGame.getEngine().getBukkitFrame();
        if (bukkitFrame.getAdapterMap().containsKey(parameter)) {
            try {
                return bukkitFrame.getAdapterMap().get(parameter).convert(value);
            } catch (Exception e) {
                return null;
            }
        } else if (player != null && parameter.equals(Location.class)) {
            return player.getLocation();
        } else if (parameter.equals(Player.class)) {
            return Bukkit.getPlayerExact(value);
        }
        return null;
    }
}