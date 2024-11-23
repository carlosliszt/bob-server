package com.minecraft.core.proxy.staff;

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
        addShortcut(new Shortcut("ka", "KillAura", "p ban cheating n {0} Kill Aura"));
        addShortcut(new Shortcut("vl", "Velocity", "p ban cheating n {0} Velocity"));
        addShortcut(new Shortcut("sc", "Scaffold", "p ban cheating n {0} Scaffold"));
        addShortcut(new Shortcut("sp", "Speed", "p ban cheating n {0} Speed"));
        addShortcut(new Shortcut("fl", "Flight", "p ban cheating n {0} Fly"));
        addShortcut(new Shortcut("bh", "BHop", "p ban cheating n {0} BHop"));
        addShortcut(new Shortcut("tm", "Timer", "p ban cheating n {0} Timer"));
        addShortcut(new Shortcut("am", "AutoArmor", "p ban cheating n {0} AutoArmor"));
        addShortcut(new Shortcut("iv", "InvMove", "p ban cheating n {0} InvMove"));
        addShortcut(new Shortcut("ch", "Chest Stealer", "p ban cheating n {0} Chest Stealer"));
        addShortcut(new Shortcut("sf", "Safe Walk", "p ban cheating n {0} Safe-Walk"));
        addShortcut(new Shortcut("ff", "Force-Field", "p ban cheating n {0} Force-Field"));

    }

}
