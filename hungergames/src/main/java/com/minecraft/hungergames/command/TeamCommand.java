package com.minecraft.hungergames.command;

import com.google.common.collect.ImmutableMap;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.variable.object.SimpleVariable;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.game.list.ClanxClan;
import com.minecraft.hungergames.game.team.Team;
import com.minecraft.hungergames.game.team.TeamStorage;
import com.minecraft.hungergames.util.constructor.Assistance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TeamCommand {

    @Command(name = "team", platform = Platform.PLAYER, rank = Rank.PARTNER_PLUS, aliases = "tm")
    public void teamCommand(Context<CommandSender> context) {

        String label = context.getLabel();

        if (context.argsCount() == 0) {
            context.sendMessage("§cUso do /" + context.getLabel() + ":");
            context.sendMessage("§c* /" + label + " new <color> [name]");
            context.sendMessage("§c* /" + label + " del <name>");
            context.sendMessage("§c* /" + label + " info <name>");
            context.sendMessage("§c* /" + label + " edit <name> member <clear/remove/add>");
            context.sendMessage("§c* /" + label + " edit <name> name <new name>");
            context.sendMessage("§c* /" + label + " edit <name> color <new color>");
            context.sendMessage("§c* /" + label + " list");
            context.sendMessage("§c* /" + label + " clear");
        } else {
            Argument argument = Argument.get(context.getArg(0));

            if (argument == null) {
                context.info("no_function", context.getArg(0).toLowerCase());
                return;
            }

            if (context.argsCount() < argument.getMinimumArgs()) {
                context.sendMessage("§cUso do /" + context.getLabel() + ":");
                context.sendMessage("§c* /" + label + " new <color> [name]");
                context.sendMessage("§c* /" + label + " del <name>");
                context.sendMessage("§c* /" + label + " info <name>");
                context.sendMessage("§c* /" + label + " edit <name> member <clear/remove/add>");
                context.sendMessage("§c* /" + label + " edit <name> name <new name>");
                context.sendMessage("§c* /" + label + " edit <name> color <new color>");
                context.sendMessage("§c* /" + label + " list");
                context.sendMessage("§c* /" + label + " clear");
                return;
            }
            argument.getExecutor().execute(context);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum Argument implements Assistance {

        NEW("new", 2, context -> {

            Map.Entry<ChatColor, Color> colorEntry = getEntry(context.getArg(1));

            if (colorEntry == null) {
                context.info("object.not_found", "Color");
                return;
            }

            String name = createArgs(2, context.getArgs(), "hg.teams." + colorEntry.getKey().name().toLowerCase(), false);
            String realName = context.getLanguage().translate(name);

            if (realName.length() < 3 || realName.length() > 16) {
                context.sendMessage("§cO nome do time deve ter no mínimo 3 caracteres e no máximo 16.");
                return;
            }


            TeamStorage teamStorage = ((ClanxClan) HungerGames.getInstance().getGame()).getTeamStorage();

            if (teamStorage.getTeam(name) != null || teamStorage.getTeam(realName) != null) {
                context.sendMessage("§cNão foi possível criar o time: IllegalStateException: Já existe um time com esse nome.");
                return;
            }

            try {
                Team team = new Team(teamStorage.teamCount(), name, colorEntry.getKey(), colorEntry.getValue());
                teamStorage.register(team);
                context.info("command.team.create_successful", team.getChatColor() + context.getLanguage().translate(team.getName()));
            } catch (IllegalStateException e) {
                context.sendMessage("§cNão foi possível criar o time: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }),

        DELETE("del", 2, context -> {

            String name = createArgs(1, context.getArgs(), "hg.teams." + context.getArg(0), false);

            TeamStorage teamStorage = ((ClanxClan) HungerGames.getInstance().getGame()).getTeamStorage();
            Team team = teamStorage.getTeam(name);

            if (team == null) {
                context.info("command.team.not_found");
                return;
            }

            teamStorage.delete(team);
            context.info("command.team.delete_successful", team.getChatColor() + context.getLanguage().translate(team.getName()));
        }),

        CLEAR("clear", 1, context -> {

            TeamStorage teamStorage = ((ClanxClan) HungerGames.getInstance().getGame()).getTeamStorage();

            List<Team> teams = new ArrayList<>(teamStorage.getTeams());

            for (Team team : teams) {
                teamStorage.delete(team);
            }

            teams.clear(); // Avoid memory leaks.

            context.info("command.team.clear_successful");
        }),

        INFO("info", 2, context -> {

            String name = context.getArg(1);

            TeamStorage teamStorage = ((ClanxClan) HungerGames.getInstance().getGame()).getTeamStorage();
            Team team = teamStorage.getTeam(name);
            SimpleVariable simpleVariable = BukkitGame.getEngine().getVariableLoader().getVariable("hg.team.friendly_fire");

            if (team == null) {
                context.info("command.team.not_found");
                return;
            }

            context.sendMessage("§aNome: §f" + context.getLanguage().translate(team.getName()) + " (" + team.getName() + ")");
            context.sendMessage("§aCor: §f" + StringUtils.capitalize(team.getChatColor() + team.getChatColor().name()));
            context.sendMessage("§aMembros: §f" + team.getMembers().size() + "/" + teamStorage.getMaxSlots() + ": " + team.printMembers());

            if (simpleVariable != null) {
                try {
                    context.sendMessage("§aFogo amigo: §f" + simpleVariable.getValue());
                } catch (Exception ignored) {

                }
            }

        });

        private final String argument;
        private final int minimumArgs;
        private final Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(c -> c.getArgument() != null && c.getArgument().equalsIgnoreCase(key)).findFirst().orElse(null);
        }

        public static String createArgs(final int index, final String[] args, final String defaultArgs, final boolean color) {
            final StringBuilder sb = new StringBuilder();
            for (int i = index; i < args.length; ++i) {
                sb.append(args[i]).append((i + 1 >= args.length) ? "" : " ");
            }
            if (sb.length() == 0) {
                sb.append(defaultArgs);
            }
            return color ? net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', sb.toString()) : sb.toString();
        }

        private static final ImmutableMap<ChatColor, Color> colorsMap = ImmutableMap.<ChatColor, Color>builder()
                .put(ChatColor.DARK_RED, Color.fromRGB(170, 0, 0))
                .put(ChatColor.DARK_PURPLE, Color.fromRGB(170, 0, 170))
                .put(ChatColor.GOLD, Color.fromRGB(255, 170, 0))
                .put(ChatColor.RED, Color.fromRGB(255, 85, 85))
                .put(ChatColor.LIGHT_PURPLE, Color.fromRGB(255, 85, 255))
                .put(ChatColor.YELLOW, Color.fromRGB(255, 255, 85))
                .put(ChatColor.GREEN, Color.fromRGB(85, 255, 85))
                .put(ChatColor.DARK_AQUA, Color.fromRGB(0, 170, 170))
                .put(ChatColor.DARK_BLUE, Color.fromRGB(0, 0, 170))
                .put(ChatColor.BLUE, Color.fromRGB(85, 85, 255))
                .put(ChatColor.WHITE, Color.fromRGB(255, 255, 255))
                .put(ChatColor.AQUA, Color.fromRGB(85, 255, 255)).build();

        private static Map.Entry<ChatColor, Color> getEntry(String name) {
            for (Map.Entry<ChatColor, Color> colors : colorsMap.entrySet()) {
                if (colors.getKey().name().equalsIgnoreCase(name))
                    return colors;
            }
            return null;
        }
    }


    private interface Executor {
        void execute(Context<CommandSender> context);
    }

}
