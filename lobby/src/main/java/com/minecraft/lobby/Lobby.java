/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.lobby;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.accessory.list.emotions.list.EmotionCollector;
import com.minecraft.core.bukkit.accessory.list.gadget.BalloonGadgetAccessory;
import com.minecraft.core.bukkit.accessory.list.gadget.FossilGadgetAccessory;
import com.minecraft.core.bukkit.accessory.list.gadget.TrampolineGadgetAccessory;
import com.minecraft.core.bukkit.accessory.list.particles.list.ParticleCollector;
import com.minecraft.core.bukkit.accessory.list.title.list.TitleCollector;
import com.minecraft.core.bukkit.listener.AwayListener;
import com.minecraft.core.bukkit.server.BukkitServerStorage;
import com.minecraft.lobby.command.BuildCommand;
import com.minecraft.lobby.command.FlyCommand;
import com.minecraft.lobby.hall.Hall;
import com.minecraft.lobby.hall.HallStorage;
import com.minecraft.lobby.listener.BasicListener;
import com.minecraft.lobby.listener.UserLoader;
import com.minecraft.lobby.noteblock.SongPlayer;
import com.minecraft.lobby.user.UserStorage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Lobby extends BukkitGame {

    private UserStorage userStorage;

    private Hall hall;
    private final HashMap<String, ArrayList<SongPlayer>> playingSongs = new HashMap<>();
    private final HashMap<String, Byte> playerVolume = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.userStorage = new UserStorage();

        startServerDump();

        getEngine().getBukkitFrame().registerCommands(new BuildCommand(), new FlyCommand());

        getServer().getPluginManager().registerEvents(new UserLoader(), this);
        getServer().getPluginManager().registerEvents(new BasicListener(), this);
        getServer().getPluginManager().registerEvents(new AwayListener(), this);

        getAccessoryStorage().register(new FossilGadgetAccessory(), new BalloonGadgetAccessory(), new TrampolineGadgetAccessory());

        for (ParticleCollector collector : ParticleCollector.values()) {
            getAccessoryStorage().register(collector.getAccessory());
        }

        for (EmotionCollector collector : EmotionCollector.values()) {
            getAccessoryStorage().register(collector.getAccessory());
        }

        for(TitleCollector collector : TitleCollector.values()) {
            getAccessoryStorage().register(collector.getAccessory());
        }

        this.hall = loadHall();
        ((BukkitServerStorage) getServerStorage()).subscribeProxyCount();
    }

    public final Set<Block> blockSet = new HashSet<>();

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getWorlds().forEach(world -> delete(new File(world.getWorldFolder(), "playerdata")));
        blockSet.forEach(block -> block.setType(Material.SPONGE));
    }

    public static Lobby getLobby() {
        return Lobby.getPlugin(Lobby.class);
    }

    private Hall loadHall() {
        try {
            Hall hall = (Hall) HallStorage.getHall(getConfig().getString("hall.mode")).getConstructor(Lobby.class).newInstance(this);
            hall.loadVariables();
            getServer().getPluginManager().registerEvents(hall, this);
            hall.runTaskTimerAsynchronously(this, 1L, 1L);
            return hall;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.shutdown();
        }
        return null;
    }

    protected void delete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                delete(new File(file, child));
            }
        }
        if (file.exists())
            file.delete();
    }

    public boolean isReceivingSong(Player p) {
        return ((playingSongs.get(p.getName()) != null) && (!playingSongs.get(p.getName()).isEmpty()));
    }

    public void stopPlaying(Player p) {
        if (playingSongs.get(p.getName()) == null) {
            return;
        }
        for (SongPlayer s : playingSongs.get(p.getName())) {
            s.removePlayer(p);
        }
    }

    public static void setPlayerVolume(Player p, byte volume) {
        getLobby().getPlayerVolume().put(p.getName(), volume);
    }

    public byte getPlayerVolume(Player p) {
        Byte b = playerVolume.get(p.getName());
        if (b == null) {
            b = 100;
            playerVolume.put(p.getName(), b);
        }
        return b;
    }

}