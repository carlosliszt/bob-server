/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.hungergames.user.kits.list;

import com.minecraft.core.Constants;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.hungergames.HungerGames;
import com.minecraft.hungergames.user.User;
import com.minecraft.hungergames.user.kits.Kit;
import com.minecraft.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class Surprise extends Kit {

    public Surprise(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.CAKE));
        setKitCategory(KitCategory.NONE);
    }

    @Override
    public void grant(Player player) {

        User user = getUser(player.getUniqueId());

        for (int i = 0; i < user.getKits().length; i++) {
            Kit slotKit = user.getKit(i);

            if (slotKit == this) {
                Kit kit = getRandomKit(user, user.getKits());
                user.setKit(i, kit);
                kit.grant(player);
                player.sendMessage(user.getAccount().getLanguage().translate("kit.surprise.kit_received", kit.getDisplayName()));
                break;
            }
        }
    }

    private Kit getRandomKit(User user, Kit[] playerKits) {
        List<Kit> kits = getPlugin().getKitStorage().getKits().stream().filter(kit -> {

            if (!kit.isActive())
                return false;

            if (!kit.isMultipleChoices() && kit.isUser(user))
                return false;

            if (kit.isNone())
                return false;

            for (Kit other : playerKits) {
                if (getPlugin().getKitStorage().isBlocked(other.getClass(), kit.getClass()))
                    return false;
            }

            return true;
        }).collect(Collectors.toList());
        return kits.get(Constants.RANDOM.nextInt(kits.size()));
    }
}
