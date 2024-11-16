/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.bukkit.util.whitelist;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Whitelist {

    private boolean active = false;
    private Rank minimumRank = Rank.STREAMER_PLUS;
    private transient Set<WhitelistData> whitelistedPlayers = new HashSet<>();

    public boolean isWhitelisted(String name) {
        return getWhitelistedPlayers().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }

    public boolean isWhitelisted(UUID uuid) {
        return getWhitelistedPlayers().stream().anyMatch(c -> c.getUniqueId().equals(uuid));
    }

    public WhitelistData getData(String name) {
        return getWhitelistedPlayers().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void save() {
        File dataFolder = BukkitGame.getEngine().getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdir();
        File file = new File(dataFolder, "whitelist.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(Constants.GSON.toJson(this));
        } catch (IOException e) {
            System.out.println("-> Failed to save whitelist.json (" + e.getMessage() + ' ' + e.getCause() + ")");
        }
    }

    public static Whitelist load() {
        File file = new File(BukkitGame.getEngine().getDataFolder(), "whitelist.json");
        if (file.exists()) {
            try {
                return Constants.GSON.fromJson(new FileReader(file), Whitelist.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Whitelist();
    }


}