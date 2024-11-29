package com.minecraft.core.bukkit.accessory.list.title.list;

import com.minecraft.core.bukkit.accessory.AccessoryRarity;
import com.minecraft.core.bukkit.accessory.list.title.TitleAccessory;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum TitleCollector {

    SINCE("Desde",
            new TitleAccessory(TitleAccessory.TitleCategory.GERAL, "fjre3", "§eDesde §b{0}",
                    "Desde", "title.since", Rank.BLAZE_PLUS, new ItemStack(TitleAccessory.TitleCategory.GERAL.getMaterial()),
                    "", AccessoryRarity.RARE, TitleAccessory.TitleAnimation.NONE, Columns.FIRST_LOGIN)),
    CLAN("Clan",
            new TitleAccessory(TitleAccessory.TitleCategory.GERAL, "fjre4", "§eMembro de {0}",
                    "Clan", "title.clan", Rank.BLAZE_PLUS, new ItemStack(TitleAccessory.TitleCategory.GERAL.getMaterial()),
                    "", AccessoryRarity.RARE, TitleAccessory.TitleAnimation.NONE, Columns.CLAN)),
    MEDAL("Sua medalha",
            new TitleAccessory(TitleAccessory.TitleCategory.GERAL, "fjre5", "{0}",
                    "Medalha", "title.medal", Rank.BLAZE_PLUS, new ItemStack(TitleAccessory.TitleCategory.GERAL.getMaterial()),
                    "", AccessoryRarity.RARE, TitleAccessory.TitleAnimation.NONE, Columns.MEDAL)),

    ;

    private String display;
    private TitleAccessory accessory;


}
