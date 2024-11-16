/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.hologram;

import com.minecraft.core.bukkit.BukkitGame;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Hologram {

    private final List<HologramLine> hologramLines = new ArrayList<>();
    private final String[] text;
    private final Location location;
    private final double DISTANCE = 0.30D;
    private final Player target;

    private boolean hidden = true;
    private Interact interact;
    private long lastInteract;

    protected double cosFOV = Math.cos(Math.toRadians(60));

    public Hologram(Player p, Location location, String text) {
        this.target = p;
        this.location = location;
        this.text = getFormattedLore(25, text).toArray(new String[]{});
        create();
        BukkitGame.getEngine().getHologramProvider().getHologramsList().add(this);
    }

    public Hologram(Player p, Location location, String... text) {
        this.target = p;
        this.text = text;
        this.location = location;
        create();
        BukkitGame.getEngine().getHologramProvider().getHologramsList().add(this);
    }

    public void show() {
        hologramLines.forEach(HologramLine::show);
        setHidden(false);
    }

    public void hide() {
        hologramLines.forEach(HologramLine::hide);
        setHidden(true);
    }

    private void create() {
        Location locationClone = location.clone();
        for (String text : this.text) {

            if (text == null || text.isEmpty())
                continue;

            HologramLine hl = new HologramLine(getTarget(), locationClone, text);
            hl.build();
            hologramLines.add(hl);
            locationClone.subtract(0, this.DISTANCE, 0);
        }
    }

    public void updateText(int index, String text) {
        hologramLines.get(index).update(text);
    }

    public boolean inRangeOf(Player player) {
        if (player == null)
            return false;
        if (!player.getWorld().getUID().equals(location.getWorld().getUID())) {
            return false;
        }
        double distanceSquared = player.getLocation().distanceSquared(location);
        double bukkitRange = player.spigot().getViewDistance() << 4;
        return distanceSquared <= square(70) && distanceSquared <= square(bukkitRange);
    }

    public boolean inViewOf(Player player) {
        Vector dir = location.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getEyeLocation().getDirection()) >= cosFOV;
    }

    public HologramLine getLine(int i) {
        return hologramLines.get(i);
    }

    public String getText(int i) {
        return hologramLines.get(i).getText();
    }

    private double square(double val) {
        return val * val;
    }

    public interface Interact {

        void handle(Player player, Hologram hologram, int line, Interact.ClickType type);

        enum ClickType {
            RIGHT, LEFT;
        }
    }

    private static List<String> getFormattedLore(int limit, String text) {

        List<String> lore = new ArrayList<>();
        String[] split = text.split(" ");
        text = "";

        for (int i = 0; i < split.length; ++i) {
            if (ChatColor.stripColor(text).length() > limit || ChatColor.stripColor(text).endsWith(".")
                    || ChatColor.stripColor(text).endsWith("!")) {
                lore.add("§7" + text);
                if (text.endsWith(".") || text.endsWith("!")) {
                    lore.add("");
                }
                text = "";
            }
            String toAdd = split[i];
            if (toAdd.contains("\n")) {
                toAdd = toAdd.substring(0, toAdd.indexOf("\n"));
                split[i] = split[i].substring(toAdd.length() + 1);
                lore.add("§7" + text + ((text.length() == 0) ? "" : " ") + toAdd);
                text = "";
                --i;
            } else {
                text += ((text.length() == 0) ? "" : " ") + toAdd;
            }
        }
        lore.add("§7" + text);

        return lore;
    }

}