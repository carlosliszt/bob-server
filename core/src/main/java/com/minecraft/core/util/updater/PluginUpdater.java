/*
 * Copyright (C) BlazeMC, All Rights Reserved
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
    private static final Logger LOGGER = Logger.getGlobal();

    private final File serverPluginFile;
    private final File updaterPluginFile;

    @Getter
    private boolean updated;

    public PluginUpdater(File serverPluginFile) {
        this.serverPluginFile = serverPluginFile;
        this.updaterPluginFile = new File(updaterDirectory, serverPluginFile.getName());
    }

    private boolean hasUpdate() {
        return updaterPluginFile.isFile() &&
                updaterPluginFile.lastModified() > serverPluginFile.lastModified() &&
                validate(updaterPluginFile);
    }

    private void update(Runnable afterUpdate) {
        try {
            LOGGER.info("Backing up the current plugin file...");
            File backupFile = new File(serverPluginFile.getParent(), serverPluginFile.getName() + ".backup");
            Files.copy(serverPluginFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("Attempting to replace plugin file...");
            if (tryReplaceLockedFile(serverPluginFile, updaterPluginFile)) {
                updated = true;
                afterUpdate.run();
                LOGGER.info("Update applied successfully!");
            } else {
                LOGGER.warning("Failed to replace the plugin file. File might be locked.");
            }
        } catch (IOException e) {
            LOGGER.severe("An error occurred during the update process: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean tryReplaceLockedFile(File targetFile, File sourceFile) {
        try {
            File tempFile = new File(targetFile.getParent(), targetFile.getName() + ".tmp");
            Files.move(targetFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("File replaced successfully!");
            Files.delete(tempFile.toPath()); // Clean up temporary file
            return true;
        } catch (IOException e) {
            LOGGER.severe("Error replacing file: " + e.getMessage());
            return false;
        }
    }

    private boolean validate(File file) {
        try (JarFile jar = new JarFile(file)) {
            return jar.getJarEntry("plugin.yml") != null;
        } catch (IOException e) {
            LOGGER.warning("Validation failed for file: " + file.getName());
            return false;
        }
    }

    public boolean verify(Runnable runnable) {
        LOGGER.info("Searching for updates...");
        if (hasUpdate()) {
            LOGGER.info("Update found!");
            update(runnable);
            return true;
        } else {
            LOGGER.info("No updates found!");
        }
        return false;
    }
}
