package com.minecraft.skywars.game;

import com.minecraft.core.bukkit.server.skywars.GameMode;
import com.minecraft.core.bukkit.server.skywars.GameStage;
import com.minecraft.core.bukkit.server.skywars.GameType;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.actionbar.ActionBar;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.World;
import org.github.paperspigot.Title;

@RequiredArgsConstructor
@Getter
@Setter
public abstract class Game implements BukkitInterface {

    private final int id;
    private final GameType type;

    private World world;

    private GameStage stage;
    private GameMode mode;

    public void sendMessage(final String message) {
        this.world.getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void sendActionBar(final String actionBar) {
        this.world.getPlayers().forEach(player -> ActionBar.sendActionBar(player, actionBar));
    }

    public void sendTitle(final Title title) {
        this.world.getPlayers().forEach(player -> player.sendTitle(title));
    }

    public void playSound(final Sound sound, final float var, final float var1) {
        this.world.getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, var, var1));
    }

    public int getMaxPlayers() {
        return this.type.getMaxPlayers();
    }

}