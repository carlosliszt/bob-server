package com.minecraft.staff.build.util;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorldUtils {

    public static void zipWorld(String worldName) {
        String userHome = System.getProperty("user.home");
        File destinationDir = new File(userHome, "misc" + File.separator + "maps");

        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            System.out.println("Failed to create directory: " + destinationDir.getAbsolutePath());
            return;
        }

        File destination = new File(destinationDir, worldName + ".zip");

        try {
            if (destination.exists() && !destination.delete()) {
                System.out.println("Failed to delete existing file: " + destination.getAbsolutePath());
                return;
            }

            File sourceFolder = new File(worldName);
            if (!sourceFolder.exists()) {
                System.out.println("World folder not found: " + sourceFolder.getAbsolutePath());

            }

            try (FileOutputStream fos = new FileOutputStream(destination);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                zipFolder(sourceFolder, sourceFolder.getName(), zos);
                System.out.println("World successfully zipped to " + destination.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipFolder(File folder, String baseName, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(file, baseName + File.separator + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    String entryName = baseName + File.separator + file.getName();
                    zos.putNextEntry(new ZipEntry(entryName));

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }
            }
        }
    }
}