package com.minecraft.core.proxy.staff;

import com.minecraft.core.punish.PunishType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ShortcutRepository {

    @Getter
    private final List<Shortcut> shortcuts = new ArrayList<>();

    public ShortcutRepository() {
        loadShortcuts();
    }

    public void addShortcut(Shortcut shortcut) {
        shortcuts.add(shortcut);
    }

    public void removeShortcut(Shortcut shortcut) {
        shortcuts.remove(shortcut);
    }

    public Shortcut getShortcut(String shortcut) {
        for (Shortcut s : shortcuts) {
            if (s.getShortcut().equalsIgnoreCase(shortcut)) {
                return s;
            }
        }
        return null;
    }

    public boolean hasShortcut(String shortcut) {
        return getShortcut(shortcut) != null;
    }

    public void loadShortcuts() {
        shortcuts.clear();
        addShortcut(new Shortcut(PunishType.BAN, "ka", "KillAura", "p ban cheating n {0} Kill Aura"));
        addShortcut(new Shortcut(PunishType.BAN, "vl", "Velocity", "p ban cheating n {0} Velocity"));
        addShortcut(new Shortcut(PunishType.BAN, "ak", "Anti-KB", "p ban cheating n {0} Anti-KB"));
        addShortcut(new Shortcut(PunishType.BAN, "sc", "Scaffold", "p ban cheating n {0} Scaffold"));
        addShortcut(new Shortcut(PunishType.BAN, "sp", "Speed", "p ban cheating n {0} Speed"));
        addShortcut(new Shortcut(PunishType.BAN, "fl", "Flight", "p ban cheating n {0} Fly"));
        addShortcut(new Shortcut(PunishType.BAN, "bh", "BHop", "p ban cheating n {0} BHop"));
        addShortcut(new Shortcut(PunishType.BAN, "tm", "Timer", "p ban cheating n {0} Timer"));
        addShortcut(new Shortcut(PunishType.BAN, "am", "AutoArmor", "p ban cheating n {0} AutoArmor"));
        addShortcut(new Shortcut(PunishType.BAN, "iv", "InvMove", "p ban cheating n {0} InvMove"));
        addShortcut(new Shortcut(PunishType.BAN, "ch", "Chest Stealer", "p ban cheating n {0} Chest Stealer"));
        addShortcut(new Shortcut(PunishType.BAN, "sf", "Safe Walk", "p ban cheating n {0} Safe-Walk"));
        addShortcut(new Shortcut(PunishType.BAN, "ff", "Force-Field", "p ban cheating n {0} Force-Field"));
        addShortcut(new Shortcut(PunishType.BAN, "rh", "Reach", "p ban cheating n {0} Reach"));
        addShortcut(new Shortcut(PunishType.BAN, "ac", "AutoClicker", "p ban cheating n {0} AutoClicker"));
        addShortcut(new Shortcut(PunishType.BAN, "nw", "NoSlowdown", "p ban cheating n {0} NoSlowdown"));
        addShortcut(new Shortcut(PunishType.BAN, "bn", "BedNuker", "p ban cheating n {0} BedNuker"));
        addShortcut(new Shortcut(PunishType.BAN, "nf", "NoFall", "p ban cheating n {0} NoFall"));


        addShortcut(new Shortcut(PunishType.MUTE, "ofensa", "Ofensa", "p mute community 1d {0} Ofensa {1}"));
        addShortcut(new Shortcut(PunishType.MUTE, "spam", "Spam", "p mute community 4h {0} Spam {1}"));
        addShortcut(new Shortcut(PunishType.MUTE, "racismo", "Racismo", "p mute community n {0} Racismo {1}"));
        addShortcut(new Shortcut(PunishType.MUTE, "homofobia", "Homofobia", "p mute community n {0} Homofobia {1}"));
        addShortcut(new Shortcut(PunishType.MUTE, "machismo", "Machismo", "p mute community n {0} Machismo {1}"));
        addShortcut(new Shortcut(PunishType.MUTE, "senso", "Falta de bom senso", "p mute community 7d {0} Falta de bom senso {1}"));


    }

}
