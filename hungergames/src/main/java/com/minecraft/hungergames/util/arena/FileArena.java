/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.util.arena;

import com.minecraft.hungergames.util.bo3.BO3;
import com.minecraft.hungergames.util.bo3.BO3Reader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class FileArena extends Arena {

    @Getter
    private static final File directory = new File("/home/ubuntu/misc/hg/structures");

    @Getter
    private static final List<FileArena> loadedArenas = new ArrayList<>();

    private final String name;
    private final BO3 bO3;

    public FileArena(File file) {
        this.name = StringUtils.replace(file.getName(), ".bo3", "");
        this.bO3 = new BO3Reader(file).read();
    }

    @Override
    public void spawn(Location location, BO3.BlockHandle blockHandle) {
        getBO3().spawn(location, blockHandle);
    }

    public static FileArena getArena(String name) {
        return loadedArenas.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static void load(File directory) {

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files == null)
                return;

            for (File file : files) {

                if (file.isDirectory()) {
                    load(file);
                    continue;
                }


                loadedArenas.add(new FileArena(file));
            }
        } else if (directory.exists()) {
            loadedArenas.add(new FileArena(directory));
        }
    }

    @Override
    public String toString() {
        return "FileArena{" +
                "name='" + name + '\'' +
                ", bO3=" + bO3 +
                '}';
    }
}
