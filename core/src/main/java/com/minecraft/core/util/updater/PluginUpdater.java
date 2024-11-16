/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util.updater;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class PluginUpdater {

    private static final File updaterDirectory = new File(System.getProperty("user.home"), "misc" + File.separator + "updater");

    private final File serverPluginFile, updaterPluginFile;

    @Getter
    private boolean updated;

    public PluginUpdater(File serverPluginFile) {
        this.serverPluginFile = serverPluginFile;
        this.updaterPluginFile = new File(updaterDirectory, serverPluginFile.getName());
    }

    private boolean hasUpdate() {
        return updaterPluginFile.isFile() &&
                updaterPluginFile.lastModified() > serverPluginFile.lastModified() &&
                this.validate(updaterPluginFile);
    }

    private void update(Runnable afterUpdate) {
        try {
            Files.copy(updaterPluginFile.toPath(),
                    serverPluginFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            updated = true;
            afterUpdate.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validate(File file) {
        try (JarFile jar = new JarFile(file)) {
            return jar.getJarEntry("plugin.yml") != null;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean verify(Runnable runnable) {
        Logger.getGlobal().info("Searching for updates...");
        if (hasUpdate()) {
            Logger.getGlobal().info("Update found!");
            update(runnable);
            return true;
        } else {
            Logger.getGlobal().info("No updates found!");
        }
        return false;
    }
}
