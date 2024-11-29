package com.minecraft.core.bukkit.accessory.list.title;

import com.minecraft.core.bukkit.accessory.Accessory;
import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.AccessoryType;
import com.minecraft.core.bukkit.accessory.list.title.list.TitleCollector;
import com.minecraft.core.bukkit.event.server.ServerHeartbeatEvent;
import com.minecraft.core.bukkit.util.title.TitleHandler;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TitleAccessory extends Accessory {

    @Getter
    private TitleCategory category;
    @Getter
    private TitleAnimation animation;
    @Getter @Setter
    private String display;
    @Getter
    private Columns data;

    public TitleAccessory(TitleCategory category, String uniqueCode, String display, String name, String permission, Rank rank, ItemStack itemStack, String description, AccessoryRarity rarity, TitleAnimation animation, Columns column) {
        super(uniqueCode, name, permission, rank, itemStack, description, true, AccessoryType.TITLES, rarity);
        this.category = category;
        this.animation = animation;
        this.display = display;
        this.data = column;
    }

    @Override
    public void updateAsync(Player player, ServerHeartbeatEvent event) {
    }

    @Override
    public void give(Player player) {
        TitleHandler.getInstance().setTitle(player, animation, display);
    }

    @Override
    public void remove(Player player) {
        TitleHandler.getInstance().removeTitle(player);
    }

    public static List<TitleAccessory> getByCategory(TitleAccessory.TitleCategory category) {
        List<TitleAccessory> accessories = new ArrayList<>();
        for(TitleCollector title : TitleCollector.values()) {
            if(title.getAccessory().getCategory().equals(category)) {
                accessories.add(title.getAccessory());
            }
        }
        return accessories;
    }

    @Getter
    @AllArgsConstructor
    public enum TitleCategory {
        GERAL("Geral", "{0}/{1}\n\n§eClique para ver mais.", Material.PAPER),
        BEDWARS("BedWars", "{0}/{1}\n\n§eClique para ver mais.", Material.BED),
        PVP("PvP", "{0}/{1}\n\n§eClique para ver mais.", Material.IRON_CHESTPLATE),
        SKYWARS("SkyWars", "{0}/{1}\n\n§eClique para ver mais.", Material.EYE_OF_ENDER),
        SPECIAL("Especial", "{0}/{1}\n\n§eClique para ver mais.", Material.BOOK),
        DUELS("Duels", "{0}/{1}\n\n§eClique para ver mais.", Material.DIAMOND_SWORD),
        THE_BRIDGE("The Bridge", "{0}/{1}\n\n§eClique para ver mais.", Material.CLAY),
        TIO_GERSON("Tio Gerson", "{0}/{1}\n\n§eClique para ver mais.", Material.STICK   );

        private String name;
        private String description;
        private Material material;
    }

    public enum TitleAnimation {
        NONE,
        FADE,
        RAINBOW,
        NYAN;
    }

}
