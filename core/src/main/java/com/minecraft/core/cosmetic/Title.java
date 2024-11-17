package com.minecraft.core.cosmetic;

import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum Title {

    NONE("BARRIER", TitleCategory.NONE, TitleAnimation.NONE, "None", "§cNenhum", "§cNenhum", "", null, false, Rank.MEMBER),
    SINCE("PAPER", TitleCategory.GENERAL, TitleAnimation.NONE, "Since", "§eDesde §b{0}", "§eDesde §b2024", "title.since", Columns.FIRST_LOGIN,true, Rank.ELITE),
    CLAN("PAPER", TitleCategory.GENERAL, TitleAnimation.NONE, "Clan", "§eMembro de {0}", "§eMembro de {0}", "title.clan", Columns.CLAN, true, Rank.ELITE),;

    private String displayMaterial;
    private TitleCategory titleCategory;
    private TitleAnimation titleAnimation;
    private String name, content, lore, permission;
    private Columns requiredColumn;
    private boolean exclusive;
    private Rank rank;

    public List<Title> getByCategory(TitleCategory category) {
        return Arrays.stream(values()).filter(title -> title.getTitleCategory().equals(category)).collect(Collectors.toList());
    }

    public enum TitleAnimation {
        NONE,
        RAINBOW,
        FADE,
        NYAN;
    }

}
