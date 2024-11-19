/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.inventory.Selector;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.util.constructor.Assistance;
import com.minecraft.hungergames.util.templatekit.TemplateKit;
import com.minecraft.hungergames.util.templatekit.TemplateKitStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

public class TemplatekitCommand implements Assistance, BukkitInterface {

    @Command(name = "skit", aliases = {"templatekit", "tkit"}, rank = Rank.PARTNER_PLUS, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        String[] args = context.getArgs();

        if (args.length == 0) {
            Argument.INFO.getExecutor().execute(context);
            return;
        }

        Argument argument = Argument.get(args[0]);

        if (argument.getNecessaryArgs() > args.length) {
            Argument.INFO.getExecutor().execute(context);
            return;
        }

        argument.getExecutor().execute(context);
    }

    @Getter
    @AllArgsConstructor
    public enum Argument {

        INFO(null, -1, (context) -> {
            context.sendMessage("§cUso do /skit:");
            context.sendMessage("§c* /skit new <name> [-exp]");
            context.sendMessage("§c* /skit apply <all/player...>");
            context.sendMessage("§c* /skit info <name>");
            context.sendMessage("§c* /skit del <name>");
            context.sendMessage("§c* /skit list");
            context.sendMessage("§c* /skit clear");
        }),

        NEW("new", 2, (context) -> {

            String name = context.getArg(1);

            if (TemplateKitStorage.getTemplateKit(name) != null) {
                context.info("command.skit.templatekit_already_exists", name);
                return;
            }

            boolean experience = context.argsCount() > 2 && context.getArg(2).equalsIgnoreCase("-exp");

            Player player = context.getSender();

            TemplateKit templateKit = new TemplateKit(name);

            templateKit.setContents(Executor.getClone(player.getInventory().getContents()));
            templateKit.setArmorContents(Executor.getClone(player.getInventory().getArmorContents()));

            templateKit.setEffects(player.getActivePotionEffects());

            if (experience)
                templateKit.setExperience(player.getTotalExperience());

            TemplateKitStorage.registerTemplateKit(templateKit);
            context.info("command.skit.create_successful", templateKit.getName());
        }),

        APPLY("apply", 3, (context) -> {

            String name = context.getArg(1);

            TemplateKit templateKit = TemplateKitStorage.getTemplateKit(name);

            if (templateKit == null) {
                context.info("object.not_found", "Template kit");
                return;
            }

            Collection<User> targetCollection = Executor.getTarget(context.getArg(2));

            int amount = 0;

            for (User target : targetCollection) {
                Player player = target.getPlayer();

                if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof Selector.Holder))
                    player.getOpenInventory().getTopInventory().clear();

                player.getInventory().setContents(templateKit.getContents());
                player.getInventory().setArmorContents(templateKit.getArmorContents());

                for(Kit kit : target.getKits())
                    kit.grant(player);

                if (templateKit.getExperience() != -1)
                    player.setTotalExperience(templateKit.getExperience());

                if (templateKit.getEffects() != null) {
                    for (PotionEffect effect : player.getActivePotionEffects())
                        player.removePotionEffect(effect.getType());

                    player.addPotionEffects(templateKit.getEffects());
                }

                amount++;
            }

            context.info("command.forcekit.affected_users", templateKit.getName(), amount);
        }),

        DELETE("del", 2, (context) -> {

            String name = context.getArg(1);

            TemplateKit templateKit = TemplateKitStorage.getTemplateKit(name);

            if (templateKit == null) {
                context.info("object.not_found", "Template kit");
                return;
            }

            TemplateKitStorage.deleteTemplateKit(templateKit);

            context.info("command.skit.delete_successful", templateKit.getName());
        }),

        CLEAR("clear", 1, (context) -> {
            context.info("command.skit.clear_successful", TemplateKitStorage.clear());
        }),

        LIST("list", 1, (context) -> {

            StringBuilder stringBuilder = new StringBuilder();
            ArrayList<TemplateKit> templateKits = new ArrayList<>(TemplateKitStorage.getTemplateKits());

            for (int i = 0; i < templateKits.size(); i++) {
                TemplateKit whitelistData = templateKits.get(i);

                stringBuilder.append("§e").append(whitelistData.getName());

                if (i != templateKits.size() - 1)
                    stringBuilder.append("§f, ");
            }

            templateKits.clear();
            context.info("command.skit.list_templates", stringBuilder.toString());
        });

        private final String argument;
        private final int necessaryArgs;
        private final Argument.Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(c -> c.getArgument() != null && c.getArgument().equalsIgnoreCase(key)).findFirst().orElse(INFO);
        }

        private interface Executor {
            void execute(Context<Player> context);

            static Collection<User> getTarget(String raw) {
                List<User> list = new ArrayList<>();

                if (raw.equalsIgnoreCase("all"))
                    return HungerGames.getInstance().getUserStorage().getAliveUsers();

                String[] names = raw.split(",");

                for (String name : names) {
                    User user = User.getUser(name);
                    if (user == null || !user.isAlive())
                        continue;
                    list.add(user);
                }
                return list;
            }

            static ItemStack[] getClone(ItemStack[] itemStack) {
                ItemStack[] cloneable = new ItemStack[itemStack.length];
                for (int i = 0; i < itemStack.length; i++) {
                    ItemStack itemStackI = itemStack[i];
                    if (itemStackI != null)
                        cloneable[i] = itemStackI.clone();
                    else
                        cloneable[i] = null;
                }
                return cloneable;
            }
        }
    }

    @Completer(name = "skit")
    public List<String> handleComplete(Context<CommandSender> context) {

        String[] args = context.getArgs();

        if (args.length == 1) {
            return Arrays.stream(Argument.values()).filter(arg -> arg.getArgument() != null && startsWith(arg.getArgument(), args[0])).map(Argument::getArgument).sorted().collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("apply") || args[0].equalsIgnoreCase("del")) {
                return TemplateKitStorage.getTemplateKits().stream().map(TemplateKit::getName).filter(name -> startsWith(name, args[1])).collect(Collectors.toList());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("apply")) {
            List<String> response = new ArrayList<>();

            if (startsWith("all", context.getArg(0)))
                response.add("all");

            response.addAll(getOnlineNicknames(context));
            return response;
        }
        return Collections.emptyList();
    }
}
