package com.minecraft.core.bukkit.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerUpdateTablistEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlusColorCommand implements BukkitInterface {

    @Command(name = "pluscolor", rank = Rank.BLAZE_PLUS, async = true, usage = "{labe} <pluscolor>", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        String[] args = context.getArgs();
        Player player = context.getSender();

        Account account = context.getAccount();

        account.getPlusColorList().loadPlusColor();

        if (args.length == 0) {
            int max = account.getPlusColorList().getPlusColor().size() * 2;

            TextComponent[] textComponents = new TextComponent[max];
            textComponents[0] = new TextComponent("§aSuas cores: ");

            int i = max - 1;

            for (PlusColor plusColor : account.getPlusColorList().getPlusColor()) {
                if (i < max - 1) {
                    textComponents[i] = new TextComponent("§f, ");
                    i -= 1;
                }

                String hoverDisplay = plusColor.getColor() + plusColor.getName() + "\n\n§eClique para selecionar!";

                TextComponent component = createTextComponent(plusColor.getColor() + plusColor.getName(), HoverEvent.Action.SHOW_TEXT, hoverDisplay, ClickEvent.Action.RUN_COMMAND, "/pluscolor " + plusColor.name());
                textComponents[i] = component;
                i -= 1;
            }

            context.getSender().sendMessage(textComponents);
        } else {
            PlusColor medal;

            try {
                medal = PlusColor.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                context.sendMessage("§cEssa cor não existe ou você não a possui.");
                return;
            }

            if (!account.getPlusColorList().hasPlusColor(medal)) {
                context.sendMessage("§cEssa cor não existe ou você não a possui.");
                return;
            }

            if (account.getProperty("account_pluscolor").getAs(PlusColor.class).equals(medal)) {
                context.sendMessage("§cVocê já está usando essa cor.");
                return;
            }

            account.setProperty("account_pluscolor", medal);
            account.getData(Columns.PLUSCOLOR).setData(medal.getUniqueCode());

            context.sendMessage("§aCor do plus alterada para " + medal.getColor() + medal.getName() + "§a com sucesso.");

            PlayerUpdateTablistEvent event = new PlayerUpdateTablistEvent(account, account.getProperty("account_tag").getAs(Tag.class), account.getProperty("account_prefix_type").getAs(PrefixType.class));
            Bukkit.getPluginManager().callEvent(event);

            async(() -> account.getDataStorage().saveColumn(Columns.PLUSCOLOR));
        }

    }

    @Completer(name = "pluscolor")
    public List<String> handleComplete(Context<Player> context) {
        ArrayList<String> list = new ArrayList<>();
        if (context.argsCount() == 1) {
            for (PlusColor plusColor : PlusColor.values()) {
                if (context.getAccount().getPlusColorList().hasPlusColor(plusColor)) {
                    list.add(plusColor.name());
                }
            }
        }
        return list;
    }

}
