package com.minecraft.core.proxy.scheduler;

import net.md_5.bungee.BungeeCord;

import java.time.LocalTime;

public class UpdateCheckScheduler implements Runnable{
    @Override
    public void run() {
        if(isCertainHour(5, 30)) {
            BungeeCord.getInstance().stop();
        }
    }

    public static boolean isCertainHour(int hour, int min) {
        LocalTime now = LocalTime.now();
        return now.getHour() == hour && now.getMinute() == min;
    }
}
