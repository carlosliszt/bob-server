/*
 * Copyright (C) YoloMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.proxy.ProxyGame;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ProxyTicksPerSecondCommand {

    @Command(name = "ptps", aliases = {"proxytps"}, rank = Rank.ADMINISTRATOR, async = true)
    public void handleCommand(Context<CommandSender> context) {

        final long usedMemory = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2) / 1048576L;
        final long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;

        context.sendMessage("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "----------------------------------------------");
        context.sendMessage(ChatColor.GREEN + "Online: " + ChatColor.WHITE + ProxyServer.getInstance().getOnlineCount() + "/" + ProxyServer.getInstance().getConfig().getPlayerLimit());
        context.sendMessage(ChatColor.GREEN + "Memory: " + ChatColor.WHITE + usedMemory + "/" + allocatedMemory + " MB");
        context.sendMessage(ChatColor.GREEN + "Uptime: " + ChatColor.WHITE + formatDiff(ProxyGame.getInstance().getStartTime()));
        context.sendMessage("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "----------------------------------------------");
    }

    private String formatDiff(long date) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        Calendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }

    private String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return "now";
        } else {
            if (toDate.after(fromDate)) {
                future = true;
            }

            StringBuilder sb = new StringBuilder();
            int[] types = new int[]{1, 2, 5, 11, 12, 13};
            String[] names = new String[]{"y", "y", "m", "m", "d", "d", "h", "h", "min", "min", "s", "s"};
            int accuracy = 0;

            for (int i = 0; i < types.length && accuracy <= 2; ++i) {
                int diff = dateDiff(types[i], fromDate, toDate, future);
                if (diff > 0) {
                    ++accuracy;
                    sb.append(" ").append(diff).append(names[i * 2 + (diff > 1 ? 1 : 0)]);
                }
            }

            return sb.length() == 0 ? "now" : sb.toString().trim();
        }
    }

    private int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int year = 1;
        int fromYear = fromDate.get(year);
        int toYear = toDate.get(year);
        if (Math.abs(fromYear - toYear) > 100000) {
            toDate.set(year, fromYear + (future ? 100000 : -100000));
        }

        int diff = 0;

        long savedDate;
        for (savedDate = fromDate.getTimeInMillis(); future && !fromDate.after(toDate) || !future && !fromDate.before(toDate); ++diff) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
        }

        --diff;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }


}
