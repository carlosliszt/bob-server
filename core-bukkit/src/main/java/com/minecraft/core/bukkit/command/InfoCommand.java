package com.minecraft.core.bukkit.command;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.reflection.Info;
import com.minecraft.core.bukkit.util.variable.converter.VariableConverter;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.argument.TypeAdapter;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class InfoCommand<T> implements BukkitInterface {

    private final Class<?> infoClass;
    private final TypeAdapter<T> adapter;

    @Command(name = "info", usage = "{label} <target>", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS)
    public void handleCommand(Context<Player> context, Player target) {

        if (target == null) {
            context.info("target.not_found");
            return;
        }

        Location location = target.getLocation();

        context.sendMessage("§eInformações de §b" + target.getName() + "§e:");
        context.sendMessage(" §aCoordenadas §fX: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ());
        context.sendMessage(" §aPing §f" + target.spigot().getPing());
        context.sendMessage(" §aGamemode §f" + target.getGameMode().name());
        context.sendMessage(" §aVida §f" + Constants.SIMPLE_DECIMAL_FORMAT.format(target.getHealth()) + "/" + target.getMaxHealth());

        Object user = adapter.convert(target.getUniqueId().toString());

        for (Field field : infoClass.getDeclaredFields()) {

            field.setAccessible(true);

            if (!field.isAnnotationPresent(Info.class))
                continue;

            Info info = field.getAnnotation(Info.class);
            String name = info.fancyName().equals("...") ? StringUtils.capitalize(field.getName()) : info.fancyName();

            try {

                Object result = field.get(user);

                if (result == null)
                    continue;

                String value = VariableConverter.convertCurrentValueToName(result);

                if (value.trim().isEmpty())
                    value = "§c?";

                context.sendMessage(" §a" + name + " §f" + value);
            } catch (Exception e) {
                context.sendMessage("§cError whilst getting " + target.getName() + "'s " + field.getName().toLowerCase() + " §e(field)");
            }
        }


        for (Method method : infoClass.getDeclaredMethods()) {

            method.setAccessible(true);

            if (!method.isAnnotationPresent(Info.class))
                continue;

            Info info = method.getAnnotation(Info.class);
            String name = info.fancyName().equals("...") ? StringUtils.capitalize(method.getName()) : info.fancyName();

            try {
                Object result = method.invoke(user).toString();

                if (result == null)
                    continue;

                String value = VariableConverter.convertCurrentValueToName(result);

                if (value.trim().isEmpty())
                    value = "§c?";

                context.sendMessage(" §a" + name + " §f" + value);
            } catch (Exception e) {
                context.sendMessage("§cError whilst getting " + target.getName() + "'s " + method.getName().toLowerCase() + "§c(method)");
                e.printStackTrace();
            }
        }
    }

    @Completer(name = "info")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }
}
