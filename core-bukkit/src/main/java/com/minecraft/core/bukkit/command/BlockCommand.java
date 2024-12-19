package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.blocked.Blocked;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Tables;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.stream.Collectors;

public class BlockCommand {

    @Command(name = "block", platform = Platform.PLAYER, aliases = {"bloquear"})
    public void blockCommand(Context<Player> ctx) {
        Player player = ctx.getSender();

        if (ctx.argsCount() == 0) {
            Argument.HELP.execute(ctx);
        } else {
            Argument argument = Argument.fetch(ctx.getArg(0));

            if (argument == null) {
                ctx.getSender().performCommand("friend add " + ctx.getArg(0));
                return;
            }

            if (argument.getMinimumArgs() > ctx.argsCount()) {
                Argument.HELP.execute(ctx);
                return;
            }
            argument.execute(ctx);
        }
    }

    @Getter
    public enum Argument implements BukkitInterface {

        ADD(2, "add", "adicionar"){
            @Override
            public void execute(Context<Player> context) {
                Account account = context.getAccount();
                if(Bukkit.getPlayer(context.getArg(1)) == null) {
                    context.sendMessage("§cJogador não encontrado.");
                    return;
                }
                Account target = Account.fetch(Bukkit.getPlayer(context.getArg(1)).getUniqueId());

                if(account.isBlocked(target.getUniqueId())) {
                    context.sendMessage("§aEsse jogador já está bloqueado!");
                    return;
                }

                account.block(new Blocked(target.getDisplayName(), target.getUniqueId()));
                context.sendMessage("§aJogador bloqueado com sucesso!");

                async(() -> {
                    account.getDataStorage().saveTable(Tables.OTHER);
                });
            }
        },

        REMOVE(2, "remove", "remover"){
            @Override
            public void execute(Context<Player> context) {
                Account account = context.getAccount();

                if(account.getBlocked(context.getArg(1)) == null) {
                    context.sendMessage("§cJogador não encontrado.");
                    return;
                }

                Blocked blocked = account.getBlocked(context.getArg(1));

                account.unblock(blocked);
                context.sendMessage("§aJogador desbloqueado com sucesso!");

                async(() -> {
                    account.getDataStorage().saveTable(Tables.OTHER);
                });
            }
        },

        LIST(0, "list", "lista") {
            @Override
            public void execute(Context<Player> context) {

                Account account = context.getAccount();

                if(account.getBlockedUsers().isEmpty()) {
                    context.sendMessage("§cVocê não bloqueou nenhum jogador!");
                    return;
                }

                String list = account.getBlockedUsers().stream().map(Blocked::getName)
                        .collect(Collectors.joining(", "));

                context.sendMessage("§aJogadores bloqueados (" + account.getBlockedUsers().size() + "): " + list);

            }
        },

        HELP(0, "help", "ajuda") {
            @Override
            public void execute(Context<Player> context) {
                context.sendMessage("§6Uso do §a/" + context.getLabel() + "§6:");
                context.sendMessage("§e/" + context.getLabel() + " <jogador> §e- §bBloqueie um jogador.");
                context.sendMessage("§e/" + context.getLabel() + " remove <jogador> §e- §bDesbloqueie um jogador.");
                context.sendMessage("§e/" + context.getLabel() + " list §e- §bVeja todos os jogadores bloqueados.");

            }
        };

        private final int minimumArgs;
        private final String[] field;

        Argument(int minimumArgs, java.lang.String... strings) {
            this.minimumArgs = minimumArgs;
            this.field = strings;
        }

        public static Argument fetch(String s) {
            for (Argument arg : values()) {
                for (String key : arg.getField()) {
                    if (key.equalsIgnoreCase(s))
                        return arg;
                }
            }
            return null;
        }

        public abstract void execute(Context<Player> context);
    }

}
