/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.generator;

import com.minecraft.generator.listener.GeneratorListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Generator extends JavaPlugin {

    @Override
    public void onLoad() {
        deleteDir(new File("world"));
    }

    @Override
    public void onEnable() {
        Listener listener = new GeneratorListener();
        Bukkit.getPluginManager().registerEvents(listener, getInstance());
    }

    public static Generator getInstance() {
        return getPlugin(Generator.class);
    }

    public void deleteDir(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                deleteDir(new File(file, child));
            }
        }
        file.delete();
    }
}