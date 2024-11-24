/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.proxy.util.language;

import com.minecraft.core.translation.Language;
import com.minecraft.core.translation.TranslationExecutor;
import com.minecraft.core.translation.TranslationInterface;
import net.md_5.bungee.api.ChatColor;

public class ProxyTranslationExecutor extends TranslationExecutor {

    public ProxyTranslationExecutor(TranslationInterface dataTranslation) {
        super();
        addTranslation(dataTranslation);
    }

    @Override
    public String translate(Language language, String key, Object... format) {
        return ChatColor.translateAlternateColorCodes('&', super.translate(language, key, format));
    }
}