package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import com.minecraft.core.proxy.util.command.ProxyInterface;
import com.minecraft.core.util.geodata.AddressData;
import com.minecraft.core.util.geodata.DataResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataServiceCommand implements ProxyInterface {

    private final Pattern addressPattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    @Command(name = "dataservice", aliases = "ds", platform = Platform.BOTH, rank = Rank.ADMINISTRATOR, usage = "{label} <address> <action> [parameters...]")
    public void onCommand(Context<CommandSender> context, String address, String action) {

        if (!addressPattern.matcher(address).matches()) {
            context.info("command.dataservice.invalid_address");
            return;
        }

        Argument argument = Argument.get(action);

        if (argument == null) {
            context.info("no_function", action.toLowerCase());
            return;
        }

        async(() -> {
            try {
                argument.getExecutor().execute(context, address);
            } catch (ExecutionException e) {
                context.info("unexpected_error");
                e.printStackTrace();
            }
        });
    }

    @Completer(name = "dataservice")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return BungeeCord.getInstance().getPlayers().stream().map(proxiedPlayer -> proxiedPlayer.getAddress().getHostString()).filter(str -> startsWith(str, context.getArg(0))).distinct().collect(Collectors.toList());
        else if (context.argsCount() == 2)
            return Arrays.stream(Argument.values()).map(Argument::getArgument).filter(argument -> startsWith(argument, context.getArg(1))).collect(Collectors.toList());
        return Collections.emptyList();
    }

    @AllArgsConstructor
    @Getter
    public enum Argument {

        GET("get", (ctx, address) -> {

            AddressData dataContext = DataResolver.getInstance().getData(address);

            if (dataContext != null) {
                dataContext.print(ctx);

                try {

                    Process process = Runtime.getRuntime().exec("sudo ipset test blacklist " + address);
                    String str;

                    InputStream stdIn = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(stdIn);
                    BufferedReader br = new BufferedReader(isr);

                    while ((str = br.readLine()) != null)
                        ctx.sendMessage("  §7Blacklist: %s", str);
                    process.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ctx.info("command.dataservice.no_results");
            }
        }),

        TRUST("trust", (ctx, address) -> {
            AddressData addressData = DataResolver.getInstance().getData(address);
            addressData.completelyTrust();
            ctx.info("command.account.trust.success", address);

            ProxyGame.getInstance().getDiscord().log(new EmbedBuilder()
                    .setTitle("Trust")
                    .setDescription("Address: " + address)
                    .addField("Organization", addressData.getOrganization(), false)
                    .addField("City", addressData.getCity(), false)
                    .addField("State", addressData.getState(), false)
                    .addField("Country", addressData.getCountry(), false)
                    .addField("Trusted", "Yes", false)
                    .setColor(0x00FF00));

        }),

        UNTRUST("untrust", (ctx, address) -> {
            try {
                Process process = Runtime.getRuntime().exec("sudo ipset add blacklist " + address);
                int exitValue = process.waitFor();
                process.destroy();
                System.out.println("Manually blacklisted " + address + " with code: " + exitValue);

            } catch (Exception var6) {
                System.out.println("Fail to blacklist " + address);
            }
            ctx.info("command.account.untrust.success", address);
            ProxyGame.getInstance().getDiscord().log(new EmbedBuilder()
                    .setTitle("Untrust")
                    .setDescription("Address: " + address)
                    .setColor(0xFF0000));
        }),

        ACCOUNTS("accounts", (ctx, address) -> {
            BungeeCord.getInstance().getPluginManager().dispatchCommand(ctx.getSender(), "playerfinder " + address);
        });

        private final String argument;
        private final Executor executor;

        interface Executor {
            void execute(Context<CommandSender> context, String address) throws ExecutionException;
        }

        public static Argument get(String key) {
            return Arrays.stream(values()).filter(c -> c.getArgument().equalsIgnoreCase(key)).findFirst().orElse(null);
        }
    }
}
