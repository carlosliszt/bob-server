package com.minecraft.core.proxy.util.chat;

import com.minecraft.core.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Broadcast {

    LANG("§eAltere seu idioma principal usando §b/idioma", "§eChange your primary language using §b/language"),
    TWITTER("§eAcompanhe novidades em nosso Twitter §b@BlazeMC_", "§eFollow news on our Twitter §b@BlazeMC_"),
    STATS("§eAcompanhe suas estatísticas usando §b/stats", "§eTrack your statistics using §b/stats"),
    DISCORD("§eVenha fazer parte de nossa comunidade! Acesse nosso discord: §b" + Constants.SERVER_DISCORD, "§eBe part of our community! Access our discord: §b" + Constants.SERVER_DISCORD),
    PREFERENCE("§eAltere sua experiência de jogo usando §b/preferencias", "§eChange your gaming experience using §b/preferences"),
    BETA("§eAdquira o exclusivo rank §1Beta §eem nossa loja §b" + Constants.SERVER_STORE, "§eBuy the exclusive rank §1Beta §ein our shop §b" + Constants.SERVER_STORE);

    private final String portuguese, english;

    public static Broadcast getRandomBroadcast() {
        return values()[Constants.RANDOM.nextInt(values().length)];
    }

}