package com.minecraft.core.proxy.command;

import com.minecraft.core.Constants;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.proxy.util.reward.GiftCode;
import com.minecraft.core.util.StringTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;

public class RewardCommand implements ProxyInterface {

    @Command(name = "resgatar", platform = Platform.PLAYER, aliases = {"rescue", "redeem"}, usage = "{label} <código>")
    public void rescueCommand(Context<CommandSender> context, String code) {

        GiftCode giftCode = ProxyGame.getInstance().getGiftCodeStorage().get(code);

        if (giftCode == null) {
            context.info("object.not_found", "Código-presente");
            return;
        }

        if (giftCode.isRedeemed()) {
            context.sendMessage("§cCódigo-presente já resgatado.");
            return;
        }

        giftCode.setRedeem(System.currentTimeMillis());
        giftCode.setRedeemer(context.getUniqueId());
        giftCode.update();
    }

    @Command(name = "reward", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, aliases = "giftcode")
    public void handleCommand(Context<CommandSender> context) {

        String[] args = context.getArgs();

        if (args.length == 0) {
            Argument.HELP.getExecutor().execute(context);
        } else {

            Argument argument = Argument.get(args[0]);

            if (argument == null || argument.getMinimumArgs() > context.argsCount()) {
                Argument.HELP.getExecutor().execute(context);
                return;
            }

            argument.getExecutor().execute(context);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Argument {

        HELP(null, -1, ctx -> {
            ctx.sendMessage("§cUso do /reward:");
            ctx.sendMessage("§c * /reward new <rank> [duration] <name>");
            ctx.sendMessage("§c * /reward info <key>");
            ctx.sendMessage("§c * /reward del <key>");
        }),

        NEW("new", 3, ctx -> {

            Rank rank = Rank.fromString(ctx.getArg(1));

            if (rank == null || rank == Rank.MEMBER) {
                ctx.info("object.not_found", "Rank");
                return;
            }

            if (rank.getId() > ctx.getAccount().getRank().getId()) {
                ctx.sendMessage("§cO rank inserido é maior que o seu.");
                return;
            }

            long millis = System.currentTimeMillis();
            String duration = ctx.getArg(2);

            long expiration;

            if (!duration.equalsIgnoreCase("n") && !duration.equalsIgnoreCase("never")) {
                try {
                    expiration = StringTimeUtils.parseDateDiff(duration, true);
                } catch (Exception e) {
                    ctx.info("invalid_time", "y,m,d,min,s");
                    return;
                }
            }

            String name = rank.getName() + "_giftcode";

            if (ctx.argsCount() > 3) {
                name = createArgs(3, ctx.getArgs(), "?", true);
            }

            GiftCode giftCode = new GiftCode();
            StringBuilder key = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                key.append(Constants.KEY(4, false).toLowerCase());

                if (i != 2)
                    key.append("-");
            }

            giftCode.setKey(key.toString());
            giftCode.setName(name);
            giftCode.setDuration(duration);
            giftCode.setCreator(ctx.getUniqueId());
            giftCode.setCreation(System.currentTimeMillis());
            giftCode.setRank(rank);

            if (!ProxyGame.getInstance().getGiftCodeStorage().push(giftCode)) {
                ctx.info("unexpected_error");
                return;
            }
            ctx.sendMessage("§eCódigo presente §b" + key + "§e de §b'" + rank.getName() + "' §ecriado com sucesso.");
            TextComponent copy = new TextComponent("§b§lCLIQUE AQUI");
            copy.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent("§eClique aqui!")}));
            copy.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, giftCode.getKey()));

            TextComponent message = new TextComponent("§r§e para copiar o código!");

           if(ctx.isPlayer()) {
               ctx.getSender().sendMessage(copy, message);
           }
        }),

        DELETE("del", 2, ctx -> {

            GiftCode giftCode = ProxyGame.getInstance().getGiftCodeStorage().get(ctx.getArg(1));

            if (giftCode == null) {
                ctx.info("object.not_found", "Código-presente");
                return;
            }

            giftCode.setRank(Rank.MEMBER);
            ProxyGame.getInstance().getGiftCodeStorage().delete(giftCode);
        }),

        INFO("info", 2, ctx -> {

            GiftCode giftCode = ProxyGame.getInstance().getGiftCodeStorage().get(ctx.getArg(1));

            if (giftCode == null) {
                ctx.info("object.not_found", "Código-presente");
                return;
            }

            String name = Constants.getMojangAPI().getNickname(giftCode.getCreator());

            boolean isRedeemed = giftCode.isRedeemed();

            ctx.sendMessage("");
            ctx.sendMessage(" §aRank: " + giftCode.getRank().getDefaultTag().getFormattedColor() + giftCode.getRank().getDisplayName());
            ctx.sendMessage(" §aDuração: §f" + giftCode.getDuration());
            ctx.sendMessage(" §aEmitido em: §f" + ctx.getAccount().getLanguage().getDateFormat().format(giftCode.getCreation()));
            ctx.sendMessage(" §aEmitido por: §f" + name);
            ctx.sendMessage(" §aResgatado: " + (isRedeemed ? "§a§lSIM" : "§c§lNÃO"));
            if (isRedeemed) {
                ctx.sendMessage(" §aResgatado em: §f" + ctx.getAccount().getLanguage().getDateFormat().format(giftCode.getRedeem()));
                ctx.sendMessage(" §aResgatado por: §f" + Constants.getMojangAPI().getNickname(giftCode.getRedeemer()));
            }
        });

        private final String argument;
        private final int minimumArgs;
        private final Argument.Executor executor;

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(c -> c.getArgument() != null && c.getArgument().equalsIgnoreCase(key)).findFirst().orElse(null);
        }

        private static String createArgs(final int index, final String[] args, final String defaultArgs, final boolean color) {
            final StringBuilder sb = new StringBuilder();
            for (int i = index; i < args.length; ++i) {
                sb.append(args[i]).append((i + 1 >= args.length) ? "" : " ");
            }
            if (sb.length() == 0) {
                sb.append(defaultArgs);
            }
            return color ? ChatColor.translateAlternateColorCodes('&', sb.toString()) : sb.toString();
        }

        private interface Executor {
            void execute(Context<CommandSender> context);
        }
    }
}
